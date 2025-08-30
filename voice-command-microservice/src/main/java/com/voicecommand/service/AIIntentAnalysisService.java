package com.voicecommand.service;

import com.voicecommand.model.IntentAnalysisResult;
import com.voicecommand.model.FunctionInfo;
import com.voicecommand.client.AIServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.time.LocalDate;

/**
 * AI意图分析服务
 *
 * 使用主项目的Gemini AI分析用户语音命令的意图
 * 自动识别用户想要执行的功能和参数
 * 当主项目AI服务不可用时，使用本地模拟分析
 *
 * @author AI Assistant
 * @version 1.0.0
 */
@Service
@Slf4j
public class AIIntentAnalysisService {
    
    @Autowired
    private AIServiceClient aiServiceClient;
    
    @Autowired
    private FunctionKnowledgeService functionKnowledgeService;
    
    @Autowired
    private ContactLookupService contactLookupService;
    
    /**
     * 分析用户意图
     *
     * @param userText 用户输入的文本
     * @param context 上下文信息
     * @return 意图分析结果
     */
    public IntentAnalysisResult analyzeIntent(String userText, Map<String, Object> context) {
        log.info("开始分析用户意图: {}", userText);
        
        try {
            // 首先尝试调用真正的AI服务进行分析
            log.info("尝试调用AI服务进行意图分析...");
            String analysisPrompt = buildAnalysisPrompt(userText, context);
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("message", analysisPrompt);
            
            try {
                Map<String, Object> aiResponse = aiServiceClient.chatWithGemini(aiRequest);
                IntentAnalysisResult result = parseAIResponse(aiResponse, userText);
                log.info("AI意图分析完成: 功能={}, 置信度={}", result.getFunctionName(), result.getConfidence());
                return result;
            } catch (Exception e) {
                log.warn("AI服务调用失败，使用本地模拟分析作为备选方案: {}", e.getMessage());
                // 使用本地模拟分析作为备选方案
                return performLocalIntentAnalysis(userText, context);
            }
        } catch (Exception e) {
            log.error("意图分析过程中发生错误，使用本地备选方案", e);
            return performLocalIntentAnalysis(userText, context);
        }
    }

    // 本地模拟意图分析（当主项目AI服务不可用时使用）
    private IntentAnalysisResult performLocalIntentAnalysis(String userText, Map<String, Object> context) {
        log.info("使用本地模拟分析用户意图: {}", userText);
        
        // 简单的关键词匹配逻辑
        String lowerText = userText.toLowerCase();
        
        // 邮件发送意图检测 - 支持中英文关键词
        if (lowerText.contains("邮件") || lowerText.contains("email") || 
            lowerText.contains("发送") || lowerText.contains("发邮件") || lowerText.contains("send") ||
            lowerText.contains("mail") || lowerText.contains("message") ||
            (lowerText.contains("给") && (lowerText.contains("发") || lowerText.contains("发送"))) ||
            (lowerText.contains("to") && (lowerText.contains("send") || lowerText.contains("mail"))) ||
            lowerText.contains("使用发送邮件功能") || lowerText.contains("send email")) {
            
            Map<String, Object> params = new HashMap<>();
            
            // 智能提取邮件主题和内容
            String subject = extractEmailSubject(userText);
            String content = extractEmailContent(userText);
            
            if (content == null || content.isEmpty()) {
                log.warn("无法提取有效的邮件内容");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("本地分析：无法识别有效的邮件内容")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // 智能提取收件人信息（姓名或邮箱）
            String recipient = extractRecipient(userText);
            String email = extractEmailAddress(userText);
            
            if (email != null && !email.isEmpty()) {
                // 有邮箱地址，直接使用
                params.put("toEmail", email);
                params.put("content", content);
                params.put("subject", subject);
                
                // 添加用户信息用于邮件个性化
                String userName = extractUserNameFromContext(context);
                params.put("userName", userName);
                params.put("userId", extractUserIdFromContext(context));
                
                log.info("本地分析结果 - 功能: send_email, 收件人邮箱: {}, 内容: {}, 主题: {}, 用户名: {}", email, content, subject, userName);
                
                return IntentAnalysisResult.builder()
                    .functionName("send_email")
                    .confidence(0.9)
                    .parameters(params)
                    .reasoning("基于关键词'邮件'、'发送'等识别为邮件发送意图，包含邮箱地址、主题和内容")
                    .knowledgeUsed("本地关键词匹配分析")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            } else {
                // 没有邮箱地址，但有收件人姓名和内容，尝试从数据库查询
                log.info("尝试从数据库查询联系人邮箱: {}", recipient);
                
                // 尝试查询联系人邮箱地址
                String emailFromDB = contactLookupService.lookupEmailByName(recipient, null);
                
                if (emailFromDB != null && !emailFromDB.isEmpty()) {
                    // 从数据库找到了邮箱地址
                    params.put("toEmail", emailFromDB);
                    params.put("content", content);
                    params.put("subject", subject);
                    params.put("recipientName", recipient);
                    params.put("source", "database");
                    
                    // 添加用户信息用于邮件个性化
                    String userName = extractUserNameFromContext(context);
                    params.put("userName", userName);
                    params.put("userId", extractUserIdFromContext(context));
                    
                    log.info("从数据库找到联系人邮箱: {} -> {}, 用户名: {}, 主题: {}", recipient, emailFromDB, userName, subject);
                    
                    return IntentAnalysisResult.builder()
                        .functionName("send_email")
                        .confidence(0.95)
                        .parameters(params)
                        .reasoning("基于关键词'邮件'、'发送'等识别为邮件发送意图，包含收件人姓名、主题和内容，并从数据库自动查询到邮箱地址")
                        .knowledgeUsed("本地关键词匹配分析 + 数据库联系人查询")
                        .originalText(userText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("local-fallback")
                        .build();
                } else {
                    // 数据库中没有找到邮箱地址，生成提示
                    params.put("recipientName", recipient);
                    params.put("content", content);
                    params.put("subject", subject);
                    params.put("needsEmail", true);
                    params.put("message", "Email sending intent detected, but recipient email address is required");
                    params.put("searchedDatabase", true);
                    
                    // 添加用户信息用于邮件个性化
                    String userName = extractUserNameFromContext(context);
                    params.put("userName", userName);
                    params.put("userId", extractUserIdFromContext(context));
                    
                    log.info("本地分析结果 - 功能: send_email, 收件人姓名: {}, 内容: {}, 主题: {}, 用户名: {}, 数据库查询无结果", recipient, content, subject, userName);
                    
                    return IntentAnalysisResult.builder()
                        .functionName("send_email")
                        .confidence(0.8)
                        .parameters(params)
                        .reasoning("基于关键词'邮件'、'发送'等识别为邮件发送意图，包含收件人姓名、主题和内容，已尝试数据库查询但未找到邮箱地址")
                        .knowledgeUsed("本地关键词匹配分析 + 数据库联系人查询")
                        .originalText(userText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("local-fallback")
                        .build();
                }
            }
        }
        
        // 重要日期管理意图检测 - 支持中英文关键词（优先级更高）
        if (lowerText.contains("重要日期") || lowerText.contains("important date") || 
            lowerText.contains("生日") || lowerText.contains("birthday") ||
            lowerText.contains("纪念日") || lowerText.contains("anniversary") ||
            lowerText.contains("节日") || lowerText.contains("holiday") ||
            (lowerText.contains("添加") || lowerText.contains("add") || lowerText.contains("设置") || lowerText.contains("set")) &&
            (lowerText.contains("生日") || lowerText.contains("birthday") || lowerText.contains("纪念日") || lowerText.contains("anniversary"))) {
            
            Map<String, Object> params = new HashMap<>();
            
            // 智能提取重要日期信息
            String title = extractImportantDateTitle(userText);
            String date = extractImportantDateDate(userText);
            String type = extractImportantDateType(userText);
            String description = extractImportantDateDescription(userText);
            
            if (title == null || title.isEmpty()) {
                log.warn("无法提取有效的重要日期标题");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("本地分析：无法识别有效的重要日期标题")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // 设置参数
            params.put("title", title);
            if (date != null && !date.isEmpty()) {
                params.put("date", date);
            }
            if (type != null && !type.isEmpty()) {
                params.put("type", type);
            }
            if (description != null && !description.isEmpty()) {
                params.put("description", description);
            }
            
            // 添加用户信息
            String userName = extractUserNameFromContext(context);
            params.put("userName", userName);
            params.put("userId", extractUserIdFromContext(context));
            
            log.info("本地分析结果 - 功能: add_important_date, 标题: {}, 日期: {}, 类型: {}, 用户名: {}", 
                title, date, type, userName);
            
            return IntentAnalysisResult.builder()
                .functionName("add_important_date")
                .confidence(0.9)
                .parameters(params)
                .reasoning("基于关键词'重要日期'、'生日'、'纪念日'等识别为重要日期管理意图，包含标题、日期、类型等信息")
                .knowledgeUsed("本地关键词匹配分析")
                .originalText(userText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("local-fallback")
                .build();
        }
        
        // 日程管理意图检测 - 支持中英文关键词
        if (lowerText.contains("日程") || lowerText.contains("schedule") || 
            lowerText.contains("安排") || lowerText.contains("添加") || lowerText.contains("add") ||
            lowerText.contains("设置") || lowerText.contains("set") || lowerText.contains("预约") ||
            lowerText.contains("提醒") || lowerText.contains("reminder") ||
            (lowerText.contains("明天") || lowerText.contains("后天") || lowerText.contains("下周")) ||
            (lowerText.contains("tomorrow") || lowerText.contains("next") || lowerText.contains("schedule"))) {
            
            Map<String, Object> params = new HashMap<>();
            
            // 智能提取日程信息
            String title = extractScheduleTitle(userText);
            String date = extractScheduleDate(userText);
            String time = extractScheduleTime(userText);
            String category = extractScheduleCategory(userText);
            String description = extractScheduleDescription(userText);
            String priority = extractSchedulePriority(userText);
            
            if (title == null || title.isEmpty()) {
                log.warn("无法提取有效的日程标题");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("本地分析：无法识别有效的日程标题")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // 设置参数
            params.put("title", title);
            if (date != null && !date.isEmpty()) {
                params.put("date", date);
            }
            if (time != null && !time.isEmpty()) {
                params.put("time", time);
            }
            if (category != null && !category.isEmpty()) {
                params.put("category", category);
            }
            if (description != null && !description.isEmpty()) {
                params.put("description", description);
            }
            if (priority != null && !priority.isEmpty()) {
                params.put("priority", priority);
            }
            
            // 添加用户信息
            String userName = extractUserNameFromContext(context);
            params.put("userName", userName);
            params.put("userId", extractUserIdFromContext(context));
            
            log.info("本地分析结果 - 功能: add_schedule, 标题: {}, 日期: {}, 时间: {}, 类别: {}, 用户名: {}", 
                title, date, time, category, userName);
            
            return IntentAnalysisResult.builder()
                .functionName("add_schedule")
                .confidence(0.9)
                .parameters(params)
                .reasoning("基于关键词'日程'、'安排'、'添加'等识别为日程管理意图，包含标题、日期、时间等信息")
                .knowledgeUsed("本地关键词匹配分析")
                .originalText(userText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("local-fallback")
                .build();
        }
        

        
        // 其他意图可以在这里添加...
        
        return IntentAnalysisResult.builder()
            .functionName("unknown")
            .confidence(0.3)
            .reasoning("无法识别用户意图，建议使用文本输入")
            .originalText(userText)
            .analysisTimestamp(System.currentTimeMillis())
            .aiModel("local-fallback")
            .build();
    }
    
    // 智能提取邮箱地址
    private String extractEmailAddress(String text) {
        log.info("开始提取邮箱地址，输入文本: {}", text);
        
        // 使用更宽松的正则表达式匹配邮箱格式
        String emailPattern = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String email = matcher.group();
            log.info("提取到邮箱地址: {}", email);
            return email;
        }
        
        // 如果正则表达式失败，尝试简单的@符号匹配
        if (text.contains("@")) {
            String[] parts = text.split("@");
            if (parts.length == 2) {
                String localPart = parts[0];
                String domainPart = parts[1];
                
                // 从后往前找到域名部分
                int dotIndex = domainPart.lastIndexOf('.');
                if (dotIndex > 0) {
                    String domain = domainPart.substring(0, dotIndex);
                    String tld = domainPart.substring(dotIndex);
                    
                    // 构建邮箱地址
                    String email = localPart + "@" + domain + tld;
                    log.info("通过@符号匹配提取到邮箱地址: {}", email);
                    return email;
                }
            }
        }
        
        log.warn("未找到有效的邮箱地址格式");
        return null;
    }
    
    // 智能提取邮件内容
    private String extractEmailContent(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试多种模式提取内容
        String content = null;
        
        // 模式1: "内容是[内容]" 或 "content is [内容]"
        if (lowerText.contains("内容是") || lowerText.contains("content is")) {
            int start;
            if (lowerText.contains("内容是")) {
                start = text.indexOf("内容是") + 4;
            } else {
                start = text.indexOf("content is") + 12;
            }
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                // 移除邮箱地址部分
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("模式1提取内容: {}", content);
                    return content;
                }
            }
        }
        
        // 模式2: "发送[内容]" 或 "send [内容]"
        if (lowerText.contains("发送") || lowerText.contains("send")) {
            int start = lowerText.indexOf("发送");
            if (start == -1) start = lowerText.indexOf("send");
            start += (lowerText.contains("发送") ? 2 : 4);
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                // 移除邮箱地址部分
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("模式2提取内容: {}", content);
                    return content;
                }
            }
        }
        
        // 模式3: "说[内容]" 或 "say [内容]"
        if (lowerText.contains("说") || lowerText.contains("say")) {
            int start = lowerText.indexOf("说");
            if (start == -1) start = lowerText.indexOf("say");
            start += (lowerText.contains("说") ? 1 : 3);
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("模式3提取内容: {}", content);
                    return content;
                }
            }
        }
        
        // 模式4: 提取邮箱后的所有内容
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            int emailEnd = matcher.end();
            if (emailEnd < text.length()) {
                content = text.substring(emailEnd).trim();
                // 移除"发送"等关键词
                content = content.replaceAll("发送", "").replaceAll("发邮件", "").trim();
                if (!content.isEmpty()) {
                    log.info("模式4提取内容: {}", content);
                    return content;
                }
            }
        }
        
        log.warn("无法提取有效的邮件内容");
        return null;
    }
    
    // 智能提取邮件主题
    private String extractEmailSubject(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试提取主题
        String subject = null;
        
        // 模式1: "主题是[主题]" 或 "subject is [主题]"
        if (lowerText.contains("主题是") || lowerText.contains("subject is")) {
            int start;
            if (lowerText.contains("主题是")) {
                start = text.indexOf("主题是") + 4;
            } else {
                start = text.indexOf("subject is") + 12;
            }
            
            if (start < text.length()) {
                // 找到下一个关键词的位置
                int end = text.length();
                if (lowerText.contains("内容是") || lowerText.contains("content is")) {
                    end = text.indexOf("内容是");
                    if (end == -1) end = text.indexOf("content is");
                } else if (lowerText.contains("内容") || lowerText.contains("content")) {
                    end = text.indexOf("内容");
                    if (end == -1) end = text.indexOf("content");
                }
                
                if (start < end) {
                    subject = text.substring(start, end).trim();
                    // 清理主题内容
                    subject = subject.replaceAll("[,，。.]+$", "").trim();
                    if (!subject.isEmpty()) {
                        log.info("模式1提取主题: {}", subject);
                        return subject;
                    }
                }
            }
        }
        
        // 如果没有找到主题，返回默认主题
        log.info("未找到明确的主题，使用默认主题");
        return "Message from AI Assistant";
    }
    
    // 从内容中移除邮箱地址和无关关键词
    private String removeEmailFromContent(String content) {
        if (content == null) return "";
        
        // 移除邮箱地址
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        content = content.replaceAll(emailPattern, "");
        
        // 移除邮件相关的无关关键词（更智能的移除）
        content = content.replaceAll("邮件", "");
        content = content.replaceAll("发送", "");
        content = content.replaceAll("发邮件", "");
        content = content.replaceAll("给", "");
        
        // 不要移除"主题是"和"内容是"，因为我们需要这些信息
        // content = content.replaceAll("主题是", "");
        // content = content.replaceAll("内容是", "");
        
        // 智能移除"到"，但保留有意义的"到"
        // 例如："我快到了" -> 保留；"到邮箱" -> 移除
        if (content.contains("到") && !content.matches(".*[你我他她它].*到.*")) {
            content = content.replaceAll("到", "");
        }
        
        // 移除多余的空格和标点
        content = content.replaceAll("\\s+", " ").trim();
        content = content.replaceAll("^[,，。.]+", "").replaceAll("[,，。.]+$", "");
        
        // 如果内容为空或只包含空格，返回null
        if (content.trim().isEmpty()) {
            return null;
        }
        
        return content;
    }
    
    /**
     * 构建AI分析提示
     */
    private String buildAnalysisPrompt(String userText, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个智能助手，需要分析用户的语音命令并确定要执行的功能。\n\n");
        prompt.append("用户输入：").append(userText).append("\n\n");
        
        prompt.append("以下是你可以调用的功能知识库：\n");
        prompt.append("请仔细阅读每个功能的描述、参数要求和示例，然后分析用户意图。\n\n");
        
        // 使用功能知识库服务获取功能信息
        if (functionKnowledgeService.isKnowledgeBaseLoaded()) {
            List<FunctionInfo> functions = functionKnowledgeService.getAllFunctions();
            for (FunctionInfo function : functions) {
                prompt.append("功能名称：").append(function.getName()).append("\n");
                prompt.append("功能描述：").append(function.getDescription()).append("\n");
                prompt.append("参数要求：\n");
                
                function.getParameters().forEach((paramName, paramInfo) -> {
                    prompt.append("- ").append(paramName).append(": ").append(paramInfo.getDescription());
                    if (paramInfo.isRequired()) {
                        prompt.append("（必需）");
                    } else {
                        prompt.append("（可选）");
                    }
                    prompt.append("\n");
                });
                
                prompt.append("使用示例：\n");
                for (String example : function.getExamples()) {
                    prompt.append("- ").append(example).append("\n");
                }
                prompt.append("\n");
            }
        } else {
            // 如果知识库未加载，使用硬编码的邮件功能信息作为fallback
            prompt.append("功能名称：send_email\n");
            prompt.append("功能描述：发送电子邮件功能\n");
            prompt.append("参数要求：\n");
            prompt.append("- toEmail: 收件人邮箱地址（必需）\n");
            prompt.append("- subject: 邮件主题（必需）\n");
            prompt.append("- content: 邮件内容（必需）\n");
            prompt.append("- fromEmail: 发件人邮箱（可选，默认使用系统邮箱）\n");
            prompt.append("使用示例：\n");
            prompt.append("- 发送邮件给张三，主题是会议提醒，内容是明天下午3点开会\n");
            prompt.append("- 给李四发邮件，告诉他明天开会\n");
            prompt.append("- 写邮件给王五，内容是关于项目进度\n\n");
        }
        
        prompt.append("请基于以上知识库，分析用户意图并返回JSON格式：\n");
        prompt.append("{\n");
        prompt.append("  \"functionName\": \"要调用的功能名称\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"parameters\": {\"param1\": \"value1\"},\n");
        prompt.append("  \"reasoning\": \"基于知识库的分析原因\",\n");
        prompt.append("  \"knowledgeUsed\": \"使用了知识库中的哪些信息\"\n");
        prompt.append("}\n\n");
        
        prompt.append("重要要求：\n");
        prompt.append("1. 仔细分析用户输入，理解真实意图\n");
        prompt.append("2. 参考知识库中的功能描述和示例\n");
        prompt.append("3. 如果用户意图不明确，请询问澄清\n");
        prompt.append("4. 确保参数提取的准确性\n");
        prompt.append("5. 只返回JSON格式，不要其他文字\n");
        prompt.append("6. 必须严格按照JSON格式返回，不要添加任何解释或说明\n");
        prompt.append("7. 如果识别为邮件功能，请提取邮箱地址和邮件内容\n");
        prompt.append("8. 确保JSON格式完全正确，可以被直接解析\n");
        prompt.append("9. 不要添加任何前缀、后缀或解释文字\n");
        prompt.append("10. 如果用户说\"给xxx发送邮件内容\"，功能名称必须是\"send_email\"\n");
        prompt.append("11. 如果用户说\"给xxx发送邮件内容\"，参数必须包含toEmail和content\n");
        prompt.append("12. 如果用户说\"给xxx发送邮件内容\"，subject可以设置为\"来自AI助手的消息\"\n");
        prompt.append("13. 示例：用户输入\"给15510399391@163.com发送邮件我快到了\"，应该返回：\n");
        prompt.append("{\n");
        prompt.append("  \"functionName\": \"send_email\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"parameters\": {\n");
        prompt.append("    \"toEmail\": \"15510399391@163.com\",\n");
        prompt.append("    \"subject\": \"来自AI助手的消息\",\n");
        prompt.append("    \"content\": \"我快到了\"\n");
        prompt.append("  },\n");
        prompt.append("  \"reasoning\": \"用户明确表示要发送邮件，包含收件人邮箱和内容\",\n");
        prompt.append("  \"knowledgeUsed\": \"邮件功能知识库\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * 解析AI响应
     */
    private IntentAnalysisResult parseAIResponse(Map<String, Object> aiResponse, String originalText) {
        try {
            log.info("开始解析AI响应，原始响应: {}", aiResponse);
            
            // 检查是否有错误
            if (aiResponse.containsKey("error") || aiResponse.containsKey("status")) {
                String errorMsg = "";
                if (aiResponse.containsKey("error")) {
                    errorMsg = String.valueOf(aiResponse.get("error"));
                }
                if (aiResponse.containsKey("status") && "error".equals(aiResponse.get("status"))) {
                    errorMsg += " AI服务状态错误";
                }
                
                log.error("AI服务返回错误: {}", errorMsg);
                
                // 如果AI服务出错，直接使用本地分析
                log.info("AI服务出错，切换到本地分析模式");
                return performLocalIntentAnalysis(originalText, new HashMap<>());
            }
            
            // 尝试多种方式获取AI响应内容
            String responseText = null;
            
            // 方式1: 尝试获取 "response" 字段
            if (aiResponse.containsKey("response")) {
                responseText = (String) aiResponse.get("response");
                log.info("从'response'字段获取响应文本: {}", responseText);
            }
            
            // 方式2: 尝试获取 "content" 字段
            if ((responseText == null || responseText.trim().isEmpty()) && aiResponse.containsKey("content")) {
                responseText = (String) aiResponse.get("content");
                log.info("从'content'字段获取响应文本: {}", responseText);
            }
            
            // 方式3: 尝试获取 "message" 字段
            if ((responseText == null || responseText.trim().isEmpty()) && aiResponse.containsKey("message")) {
                responseText = (String) aiResponse.get("message");
                log.info("从'message'字段获取响应文本: {}", responseText);
            }
            
            // 方式4: 如果都没有，记录所有字段并尝试整个响应
            if (responseText == null || responseText.trim().isEmpty()) {
                log.warn("AI响应文本为空，记录所有字段...");
                for (Map.Entry<String, Object> entry : aiResponse.entrySet()) {
                    log.info("字段 {}: {}", entry.getKey(), entry.getValue());
                }
                
                // 尝试整个响应
                responseText = aiResponse.toString();
                log.info("使用整个响应作为文本: {}", responseText);
            }
            
            // 检查响应是否包含错误信息
            if (responseText != null && (responseText.contains("error") || responseText.contains("Error") || 
                responseText.contains("failed") || responseText.contains("Failed") ||
                responseText.contains("technical issues") || responseText.contains("404"))) {
                log.error("AI响应包含错误信息: {}", responseText);
                log.info("AI服务出错，切换到本地分析模式");
                return performLocalIntentAnalysis(originalText, new HashMap<>());
            }
            
            // 尝试从AI响应中提取JSON
            Map<String, Object> parsedData = extractJSONFromResponse(responseText);
            
            if (parsedData != null && !parsedData.isEmpty()) {
                log.info("成功解析AI响应，功能: {}, 置信度: {}", 
                    parsedData.get("functionName"), parsedData.get("confidence"));
                
                return IntentAnalysisResult.builder()
                    .functionName((String) parsedData.get("functionName"))
                    .confidence(Double.parseDouble(String.valueOf(parsedData.get("confidence"))))
                    .parameters((Map<String, Object>) parsedData.get("parameters"))
                    .reasoning((String) parsedData.get("reasoning"))
                    .knowledgeUsed((String) parsedData.get("knowledgeUsed"))
                    .originalText(originalText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("gemini")
                    .build();
            } else {
                log.warn("无法解析AI响应，使用智能推断...");
                
                // 智能推断功能类型
                String inferredFunction = inferFunctionFromText(responseText);
                if (inferredFunction != null) {
                    log.info("智能推断功能: {}", inferredFunction);
                    return IntentAnalysisResult.builder()
                        .functionName(inferredFunction)
                        .confidence(0.8)
                        .parameters(new HashMap<>())
                        .reasoning("基于AI响应文本智能推断")
                        .knowledgeUsed("AI响应分析")
                        .originalText(originalText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("gemini")
                        .build();
                }
                
                // 如果无法解析JSON，使用默认分析
                log.warn("AI响应解析失败，响应内容: {}", responseText);
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("无法解析AI响应: " + responseText)
                    .originalText(originalText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("gemini")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("解析AI响应失败", e);
            
            return IntentAnalysisResult.builder()
                .functionName("unknown")
                .confidence(0.0)
                .reasoning("解析AI响应失败: " + e.getMessage())
                .originalText(originalText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("gemini")
                .build();
        }
    }
    
    /**
     * 从AI响应中提取JSON数据
     */
    private Map<String, Object> extractJSONFromResponse(String responseText) {
        try {
            // 简单的JSON提取逻辑
            if (responseText.contains("{") && responseText.contains("}")) {
                int start = responseText.indexOf("{");
                int end = responseText.lastIndexOf("}") + 1;
                String jsonText = responseText.substring(start, end);
                
                // 这里应该使用JSON解析器，简化处理
                // 实际项目中建议使用Jackson或Gson
                return parseSimpleJSON(jsonText);
            }
        } catch (Exception e) {
            log.error("提取JSON失败", e);
        }
        return null;
    }
    
    /**
     * 智能JSON解析 - 提取关键信息
     */
    private Map<String, Object> parseSimpleJSON(String jsonText) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始解析AI返回的JSON: {}", jsonText);
            
            // 提取functionName
            if (jsonText.contains("\"functionName\"")) {
                String functionName = extractValue(jsonText, "functionName");
                if (functionName != null && !functionName.isEmpty()) {
                    result.put("functionName", functionName);
                    log.info("提取到功能名称: {}", functionName);
                }
            }
            
            // 提取confidence
            if (jsonText.contains("\"confidence\"")) {
                String confidenceStr = extractValue(jsonText, "confidence");
                if (confidenceStr != null) {
                    try {
                        double confidence = Double.parseDouble(confidenceStr);
                        result.put("confidence", confidence);
                        log.info("提取到置信度: {}", confidence);
                    } catch (NumberFormatException e) {
                        log.warn("置信度解析失败: {}", confidenceStr);
                        result.put("confidence", 0.8); // 默认值
                    }
                }
            }
            
            // 提取parameters
            if (jsonText.contains("\"parameters\"")) {
                Map<String, Object> params = extractParameters(jsonText);
                if (params != null && !params.isEmpty()) {
                    result.put("parameters", params);
                    log.info("提取到参数: {}", params);
                }
            }
            
            // 提取reasoning
            if (jsonText.contains("\"reasoning\"")) {
                String reasoning = extractValue(jsonText, "reasoning");
                if (reasoning != null && !reasoning.isEmpty()) {
                    result.put("reasoning", reasoning);
                    log.info("提取到推理过程: {}", reasoning);
                }
            }
            
            // 提取knowledgeUsed
            if (jsonText.contains("\"knowledgeUsed\"")) {
                String knowledgeUsed = extractValue(jsonText, "knowledgeUsed");
                if (knowledgeUsed != null && !knowledgeUsed.isEmpty()) {
                    result.put("knowledgeUsed", knowledgeUsed);
                    log.info("提取到使用的知识: {}", knowledgeUsed);
                }
            }
            
            // 如果没有提取到functionName，尝试智能推断
            if (!result.containsKey("functionName")) {
                String inferredFunction = inferFunctionFromText(jsonText);
                if (inferredFunction != null) {
                    result.put("functionName", inferredFunction);
                    log.info("智能推断功能: {}", inferredFunction);
                }
            }
            
            // 设置默认值
            if (!result.containsKey("confidence")) {
                result.put("confidence", 0.8);
            }
            if (!result.containsKey("parameters")) {
                result.put("parameters", new HashMap<>());
            }
            if (!result.containsKey("reasoning")) {
                result.put("reasoning", "基于AI分析");
            }
            if (!result.containsKey("knowledgeUsed")) {
                result.put("knowledgeUsed", "AI意图分析");
            }
            
            log.info("JSON解析完成，结果: {}", result);
            
        } catch (Exception e) {
            log.error("JSON解析过程中发生错误", e);
        }
        
        return result;
    }
    
    /**
     * 从JSON文本中提取指定字段的值
     */
    private String extractValue(String jsonText, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonText);
            
            if (m.find()) {
                return m.group(1);
            }
            
            // 尝试提取非字符串值（如数字）
            pattern = "\"" + fieldName + "\"\\s*:\\s*([^,\\s}]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(jsonText);
            
            if (m.find()) {
                return m.group(1);
            }
            
        } catch (Exception e) {
            log.warn("提取字段 {} 失败: {}", fieldName, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 提取参数信息
     */
    private Map<String, Object> extractParameters(String jsonText) {
        Map<String, Object> params = new HashMap<>();
        
        try {
            // 查找parameters部分的开始和结束
            int start = jsonText.indexOf("\"parameters\"");
            if (start == -1) return params;
            
            // 找到parameters对象的开始
            start = jsonText.indexOf("{", start);
            if (start == -1) return params;
            
            // 找到对应的结束括号
            int braceCount = 0;
            int end = start;
            for (int i = start; i < jsonText.length(); i++) {
                if (jsonText.charAt(i) == '{') braceCount++;
                if (jsonText.charAt(i) == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        end = i + 1;
                        break;
                    }
                }
            }
            
            if (end > start) {
                String paramsText = jsonText.substring(start, end);
                log.info("提取到参数文本: {}", paramsText);
                
                // 简单解析参数
                if (paramsText.contains("\"toEmail\"")) {
                    String toEmail = extractValue(paramsText, "toEmail");
                    if (toEmail != null) params.put("toEmail", toEmail);
                }
                if (paramsText.contains("\"content\"")) {
                    String content = extractValue(paramsText, "content");
                    if (content != null) params.put("content", content);
                }
                if (paramsText.contains("\"subject\"")) {
                    String subject = extractValue(paramsText, "subject");
                    if (subject != null) params.put("subject", subject);
                }
            }
            
        } catch (Exception e) {
            log.warn("提取参数失败: {}", e.getMessage());
        }
        
        return params;
    }
    
    /**
     * 从文本中智能推断功能
     */
    private String inferFunctionFromText(String text) {
        String lowerText = text.toLowerCase();
        
        if (lowerText.contains("send_email") || lowerText.contains("邮件") || 
            lowerText.contains("email") || lowerText.contains("发送")) {
            return "send_email";
        }
        
        return null;
    }
    
    /**
     * 智能提取收件人信息（姓名或邮箱）
     */
    private String extractRecipient(String text) {
        log.info("开始提取收件人信息，输入文本: {}", text);
        
        // 首先尝试提取邮箱地址
        String email = extractEmailAddress(text);
        if (email != null && !email.isEmpty()) {
            return email;
        }
        
        // 如果没有邮箱地址，尝试提取姓名
        // 模式1: "给[姓名]发送邮件" 或 "to [姓名] send email"
        if (text.contains("给") || text.toLowerCase().contains("to")) {
            int start = -1;
            if (text.contains("给")) {
                start = text.indexOf("给") + 1;
            } else {
                start = text.toLowerCase().indexOf("to") + 2;
            }
            
            if (start < text.length()) {
                String afterGiving = text.substring(start);
                // 找到下一个关键词的位置
                int end = afterGiving.length();
                if (afterGiving.contains("发送") || afterGiving.toLowerCase().contains("send")) {
                    end = afterGiving.indexOf("发送");
                    if (end == -1) end = afterGiving.toLowerCase().indexOf("send");
                } else if (afterGiving.contains("发邮件") || afterGiving.toLowerCase().contains("email")) {
                    end = afterGiving.indexOf("发邮件");
                    if (end == -1) end = afterGiving.toLowerCase().indexOf("email");
                } else if (afterGiving.contains("邮件") || afterGiving.toLowerCase().contains("mail")) {
                    end = afterGiving.indexOf("邮件");
                    if (end == -1) end = afterGiving.toLowerCase().indexOf("mail");
                }
                
                if (end < afterGiving.length()) {
                    String name = afterGiving.substring(0, end).trim();
                    if (!name.isEmpty()) {
                        log.info("模式1提取收件人姓名: {}", name);
                        return name;
                    }
                }
            }
        }
        
        // 模式2: "发送邮件给[姓名]"
        if (text.contains("发送邮件给")) {
            int start = text.indexOf("发送邮件给") + 6;
            if (start < text.length()) {
                String afterTo = text.substring(start);
                // 找到下一个关键词的位置
                int end = afterTo.length();
                if (afterTo.contains("，")) {
                    end = afterTo.indexOf("，");
                } else if (afterTo.contains("，")) {
                    end = afterTo.indexOf("，");
                }
                
                if (end < afterTo.length()) {
                    String name = afterTo.substring(0, end).trim();
                    if (!name.isEmpty()) {
                        log.info("模式2提取收件人姓名: {}", name);
                        return name;
                    }
                }
            }
        }
        
        log.info("未找到有效的收件人信息");
        return "未知收件人";
    }
    
    /**
     * 从上下文中提取用户名
     */
    private String extractUserNameFromContext(Map<String, Object> context) {
        if (context == null) {
            return "用户";
        }
        
        // 尝试从不同字段获取用户名
        String userName = null;
        
        if (context.containsKey("userName")) {
            userName = (String) context.get("userName");
        } else if (context.containsKey("name")) {
            userName = (String) context.get("name");
        } else if (context.containsKey("displayName")) {
            userName = (String) context.get("displayName");
        } else if (context.containsKey("userId")) {
            // 如果只有用户ID，可以尝试查询用户信息
            String userId = String.valueOf(context.get("userId"));
            userName = "用户" + userId;
        }
        
        return userName != null && !userName.trim().isEmpty() ? userName.trim() : "用户";
    }
    
    /**
     * 从上下文中提取用户ID
     */
    private String extractUserIdFromContext(Map<String, Object> context) {
        if (context == null) {
            return null;
        }
        
        if (context.containsKey("userId")) {
            return String.valueOf(context.get("userId"));
        }
        
        return null;
    }
    
    // ==================== 日程管理相关方法 ====================
    
    /**
     * 智能提取日程标题
     */
    private String extractScheduleTitle(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试多种模式提取标题
        String title = null;
        
        // 模式1: "添加[标题]日程" 或 "add [标题] schedule"
        if (lowerText.contains("添加") || lowerText.contains("add")) {
            int start;
            if (lowerText.contains("添加")) {
                start = lowerText.indexOf("添加") + 2;
            } else {
                start = lowerText.indexOf("add") + 3;
            }
            
            if (start < text.length()) {
                // 找到下一个关键词的位置
                int end = text.length();
                if (lowerText.contains("日程") || lowerText.contains("schedule")) {
                    end = lowerText.indexOf("日程");
                    if (end == -1) end = lowerText.indexOf("schedule");
                } else if (lowerText.contains("安排") || lowerText.contains("安排")) {
                    end = lowerText.indexOf("安排");
                } else if (lowerText.contains("明天") || lowerText.contains("tomorrow")) {
                    end = lowerText.indexOf("明天");
                    if (end == -1) end = lowerText.indexOf("tomorrow");
                } else if (lowerText.contains("后天") || lowerText.contains("next")) {
                    end = lowerText.indexOf("后天");
                    if (end == -1) end = lowerText.indexOf("next");
                }
                
                if (start < end) {
                    title = text.substring(start, end).trim();
                    if (!title.isEmpty()) {
                        log.info("模式1提取日程标题: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // 模式2: "安排[标题]" 或 "schedule [标题]"
        if (lowerText.contains("安排") || lowerText.contains("schedule")) {
            int start;
            if (lowerText.contains("安排")) {
                start = lowerText.indexOf("安排") + 2;
            } else {
                start = lowerText.indexOf("schedule") + 8;
            }
            
            if (start < text.length()) {
                title = text.substring(start).trim();
                if (!title.isEmpty()) {
                    log.info("模式2提取日程标题: {}", title);
                    return title;
                }
            }
        }
        
        log.warn("无法提取有效的日程标题");
        return null;
    }
    
    /**
     * 智能提取日程日期
     */
    private String extractScheduleDate(String text) {
        String lowerText = text.toLowerCase();
        
        // 获取当前日期
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // 尝试提取相对日期
        if (lowerText.contains("明天") || lowerText.contains("tomorrow")) {
            String date = today.plusDays(1).toString();
            log.info("提取到相对日期: 明天 -> {}", date);
            return date;
        } else if (lowerText.contains("后天") || lowerText.contains("next day")) {
            String date = today.plusDays(2).toString();
            log.info("提取到相对日期: 后天 -> {}", date);
            return date;
        } else if (lowerText.contains("下周") || lowerText.contains("next week")) {
            // 下周对应的是7天后
            String date = today.plusDays(7).toString();
            log.info("提取到相对日期: 下周 -> {}", date);
            return date;
        } else if (lowerText.contains("下周") || lowerText.contains("next week")) {
            // 下周对应的是7天后
            String date = today.plusDays(7).toString();
            log.info("提取到相对日期: 下周 -> {}", date);
            return date;
        }
        
        // 如果没有找到相对日期，返回今天的日期
        log.info("未找到明确的日期，使用今天: {}", today.toString());
        return today.toString();
    }
    
    /**
     * 智能提取日程时间
     */
    private String extractScheduleTime(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试提取时间
        String time = null;
        
        // 模式1: "下午3点" 或 "3pm"
        if (lowerText.contains("下午") || lowerText.contains("pm")) {
            if (lowerText.contains("下午")) {
                int start = lowerText.indexOf("下午") + 2;
                if (start < text.length()) {
                    String afterAfternoon = text.substring(start);
                    // 提取数字
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                    java.util.regex.Matcher matcher = pattern.matcher(afterAfternoon);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour + 12);
                            log.info("模式1提取时间: 下午{}点 -> {}", hour, time);
                            return time;
                        }
                    }
                }
            } else if (lowerText.contains("pm")) {
                int start = lowerText.indexOf("pm") - 1;
                if (start >= 0) {
                    // 向前查找数字
                    String beforePm = text.substring(0, start + 1);
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*pm");
                    java.util.regex.Matcher matcher = pattern.matcher(beforePm);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour + 12);
                            log.info("模式1提取时间: {}pm -> {}", hour, time);
                            return time;
                        }
                    }
                }
            }
        }
        
        // 模式2: "早上8点" 或 "8am"
        if (lowerText.contains("早上") || lowerText.contains("am")) {
            if (lowerText.contains("早上")) {
                int start = lowerText.indexOf("早上") + 2;
                if (start < text.length()) {
                    String afterMorning = text.substring(start);
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                    java.util.regex.Matcher matcher = pattern.matcher(afterMorning);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour);
                            log.info("模式2提取时间: 早上{}点 -> {}", hour, time);
                            return time;
                        }
                    }
                }
            } else if (lowerText.contains("am")) {
                int start = lowerText.indexOf("am") - 1;
                if (start >= 0) {
                    String beforeAm = text.substring(0, start + 1);
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*am");
                    java.util.regex.Matcher matcher = pattern.matcher(beforeAm);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour);
                            log.info("模式2提取时间: {}am -> {}", hour, time);
                            return time;
                        }
                    }
                }
            }
        }
        
        // 如果没有找到时间，返回默认时间
        log.info("未找到明确的时间，使用默认时间: 09:00");
        return "09:00";
    }
    
    /**
     * 智能提取日程类别
     */
    private String extractScheduleCategory(String text) {
        String lowerText = text.toLowerCase();
        
        // 根据时间或关键词判断类别
        if (lowerText.contains("早上") || lowerText.contains("am") || lowerText.contains("morning")) {
            return "morning";
        } else if (lowerText.contains("下午") || lowerText.contains("pm") || lowerText.contains("afternoon")) {
            return "afternoon";
        } else if (lowerText.contains("晚上") || lowerText.contains("evening") || lowerText.contains("night")) {
            return "evening";
        } else if (lowerText.contains("吃药") || lowerText.contains("medicine") || lowerText.contains("medication")) {
            return "medication";
        }
        
        // 如果没有找到明确类别，根据时间推断
        String time = extractScheduleTime(text);
        if (time != null) {
            int hour = Integer.parseInt(time.split(":")[0]);
            if (hour >= 6 && hour < 12) {
                return "morning";
            } else if (hour >= 12 && hour < 18) {
                return "afternoon";
            } else {
                return "evening";
            }
        }
        
        // 默认类别
        log.info("未找到明确的类别，使用默认类别: afternoon");
        return "afternoon";
    }
    
    /**
     * 智能提取日程描述
     */
    private String extractScheduleDescription(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试提取描述
        String description = null;
        
        // 模式1: "内容是[描述]" 或 "content is [描述]"
        if (lowerText.contains("内容是") || lowerText.contains("content is")) {
            int start;
            if (lowerText.contains("内容是")) {
                start = text.indexOf("内容是") + 4;
            } else {
                start = text.indexOf("content is") + 12;
            }
            
            if (start < text.length()) {
                description = text.substring(start).trim();
                if (!description.isEmpty()) {
                    log.info("模式1提取日程描述: {}", description);
                    return description;
                }
            }
        }
        
        // 如果没有找到描述，返回null
        log.info("未找到明确的日程描述");
        return null;
    }
    
    /**
     * 智能提取日程优先级
     */
    private String extractSchedulePriority(String text) {
        String lowerText = text.toLowerCase();
        
        // 根据关键词判断优先级
        if (lowerText.contains("重要") || lowerText.contains("紧急") || lowerText.contains("important") || lowerText.contains("urgent")) {
            return "high";
        } else if (lowerText.contains("一般") || lowerText.contains("普通") || lowerText.contains("normal") || lowerText.contains("regular")) {
            return "low";
        }
        
        // 默认优先级
        log.info("未找到明确的优先级，使用默认优先级: medium");
        return "medium";
    }
    
    // ==================== 重要日期参数提取方法 ====================
    
    /**
     * 智能提取重要日期标题
     */
    private String extractImportantDateTitle(String text) {
        String lowerText = text.toLowerCase();
        
        // 模式1: "添加[标题]的生日" 或 "add [title] birthday"
        if (lowerText.contains("的生日") || lowerText.contains(" birthday")) {
            int end;
            if (lowerText.contains("的生日")) {
                end = text.indexOf("的生日");
            } else {
                end = text.indexOf(" birthday");
            }
            
            if (end > 0) {
                // 向前查找标题开始位置
                int start = 0;
                if (lowerText.contains("添加")) {
                    start = text.indexOf("添加") + 2;
                } else if (lowerText.contains("add")) {
                    start = text.indexOf("add") + 3;
                } else if (lowerText.contains("设置")) {
                    start = text.indexOf("设置") + 2;
                } else if (lowerText.contains("set")) {
                    start = text.indexOf("set") + 3;
                }
                
                if (start < end) {
                    String title = text.substring(start, end).trim();
                    if (!title.isEmpty()) {
                        log.info("模式1提取重要日期标题: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // 模式2: "添加[标题]纪念日" 或 "add [title] anniversary"
        if (lowerText.contains("纪念日") || lowerText.contains(" anniversary")) {
            int end;
            if (lowerText.contains("纪念日")) {
                end = text.indexOf("纪念日");
            } else {
                end = text.indexOf(" anniversary");
            }
            
            if (end > 0) {
                int start = 0;
                if (lowerText.contains("添加")) {
                    start = text.indexOf("添加") + 2;
                } else if (lowerText.contains("add")) {
                    start = text.indexOf("add") + 3;
                }
                
                if (start < end) {
                    String title = text.substring(start, end).trim();
                    if (!title.isEmpty()) {
                        log.info("模式2提取重要日期标题: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // 模式3: 直接提取标题（如果没有特定关键词）
        String[] keywords = {"添加", "add", "设置", "set"};
        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                int start = text.indexOf(keyword) + keyword.length();
                if (start < text.length()) {
                    String title = text.substring(start).trim();
                    // 移除日期和时间部分
                    if (title.contains("是") || title.contains("is")) {
                        int dateIndex = title.indexOf("是");
                        if (dateIndex > 0) {
                            title = title.substring(0, dateIndex).trim();
                        }
                    }
                    if (!title.isEmpty()) {
                        log.info("模式3提取重要日期标题: {}", title);
                        return title;
                    }
                }
            }
        }
        
        log.warn("无法提取重要日期标题");
        return null;
    }
    
    /**
     * 智能提取重要日期日期
     */
    private String extractImportantDateDate(String text) {
        String lowerText = text.toLowerCase();
        
        // 模式1: "是[日期]" 或 "is [date]"
        if (lowerText.contains("是") || lowerText.contains("is")) {
            int start;
            if (lowerText.contains("是")) {
                start = text.indexOf("是") + 1;
            } else {
                start = text.indexOf("is") + 2;
            }
            
            if (start < text.length()) {
                String datePart = text.substring(start).trim();
                
                // 尝试解析日期
                String date = parseDateFromText(datePart);
                if (date != null) {
                    log.info("模式1提取重要日期日期: {}", date);
                    return date;
                }
            }
        }
        
        // 模式2: 直接查找日期格式
        String date = parseDateFromText(text);
        if (date != null) {
            log.info("模式2提取重要日期日期: {}", date);
            return date;
        }
        
        log.warn("无法提取重要日期日期");
        return null;
    }
    
    /**
     * 智能提取重要日期类型
     */
    private String extractImportantDateType(String text) {
        String lowerText = text.toLowerCase();
        
        // 根据关键词判断类型
        if (lowerText.contains("生日") || lowerText.contains("birthday")) {
            return "birthday";
        } else if (lowerText.contains("纪念日") || lowerText.contains("anniversary")) {
            return "anniversary";
        } else if (lowerText.contains("节日") || lowerText.contains("holiday")) {
            return "holiday";
        } else if (lowerText.contains("自定义") || lowerText.contains("custom")) {
            return "custom";
        }
        
        // 默认类型
        log.info("未找到明确的类型，使用默认类型: custom");
        return "custom";
    }
    
    /**
     * 智能提取重要日期描述
     */
    private String extractImportantDateDescription(String text) {
        String lowerText = text.toLowerCase();
        
        // 尝试提取描述
        String description = null;
        
        // 模式1: "描述是[内容]" 或 "description is [内容]"
        if (lowerText.contains("描述是") || lowerText.contains("description is")) {
            int start;
            if (lowerText.contains("描述是")) {
                start = text.indexOf("描述是") + 4;
            } else {
                start = text.indexOf("description is") + 15;
            }
            
            if (start < text.length()) {
                description = text.substring(start).trim();
                if (!description.isEmpty()) {
                    log.info("模式1提取重要日期描述: {}", description);
                    return description;
                }
            }
        }
        
        // 如果没有找到描述，返回null
        log.info("未找到明确的重要日期描述");
        return null;
    }
    
    /**
     * 从文本中解析日期
     */
    private String parseDateFromText(String text) {
        try {
            // 移除多余的空格
            text = text.trim();
            
            // 尝试解析各种日期格式
            if (text.matches("\\d{1,2}月\\d{1,2}日")) {
                // 中文格式：12月25日
                String[] parts = text.split("[月日]");
                if (parts.length >= 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    // 假设是明年
                    int year = LocalDate.now().getYear() + 1;
                    return String.format("%d-%02d-%02d", year, month, day);
                }
            } else if (text.matches("\\d{1,2}/\\d{1,2}")) {
                // 英文格式：12/25
                String[] parts = text.split("/");
                if (parts.length >= 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = LocalDate.now().getYear() + 1;
                    return String.format("%d-%02d-%02d", year, month, day);
                }
            } else if (text.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                // 标准格式：2025-12-25
                return text;
            }
            
            // 尝试解析相对日期
            if (text.contains("明天")) {
                return LocalDate.now().plusDays(1).toString();
            } else if (text.contains("后天")) {
                return LocalDate.now().plusDays(2).toString();
            } else if (text.contains("下周")) {
                return LocalDate.now().plusWeeks(1).toString();
            }
            
        } catch (Exception e) {
            log.error("解析日期失败: {}", e.getMessage());
        }
        
        return null;
    }
}
