package com.voicecommand.service;

import com.voicecommand.model.IntentAnalysisResult;
import com.voicecommand.model.FunctionExecutionResult;
import com.voicecommand.client.EmailServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 功能路由服务
 * 
 * 根据AI意图分析结果，自动路由到相应的功能执行器
 * 支持邮件发送、日程管理等功能的自动调用
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Service
@Slf4j
public class FunctionRouterService {
    
    @Autowired
    private EmailServiceClient emailServiceClient;
    
    @Autowired
    private ScheduleManagementService scheduleManagementService;
    
    @Autowired
    private ImportantDateManagementService importantDateManagementService;
    
    /**
     * 执行功能
     * 
     * @param intent AI意图分析结果
     * @return 功能执行结果
     */
    public FunctionExecutionResult executeFunction(IntentAnalysisResult intent) {
        log.info("开始执行功能: {}", intent.getFunctionName());
        
        FunctionExecutionResult result = FunctionExecutionResult.builder()
            .functionName(intent.getFunctionName())
            .startTime(System.currentTimeMillis())
            .status(FunctionExecutionResult.ExecutionStatus.RUNNING)
            .build();
        
        try {
            switch (intent.getFunctionName()) {
                case "send_email":
                    result = executeSendEmail(intent);
                    break;
                    
                case "add_schedule":
                    result = executeAddSchedule(intent);
                    break;
                    
                case "add_important_date":
                    result = executeAddImportantDate(intent);
                    break;
                    
                case "health_check":
                    result = executeHealthCheck(intent);
                    break;
                    
                case "pet_interaction":
                    result = executePetInteraction(intent);
                    break;
                    
                default:
                    result.setSuccess(false);
                    result.setErrorMessage("未知功能：" + intent.getFunctionName());
                    result.setStatus(FunctionExecutionResult.ExecutionStatus.FAILED);
                    break;
            }
            
        } catch (Exception e) {
            log.error("执行功能时出错", e);
            result.setSuccess(false);
            result.setErrorMessage("执行功能时出错：" + e.getMessage());
            result.setStatus(FunctionExecutionResult.ExecutionStatus.FAILED);
        }
        
        result.setEndTime(System.currentTimeMillis());
        result.setExecutionTime(result.getEndTime() - result.getStartTime());
        
        if (result.isSuccess()) {
            result.setStatus(FunctionExecutionResult.ExecutionStatus.COMPLETED);
        }
        
        log.info("功能执行完成: 功能={}, 成功={}, 耗时={}ms", 
                intent.getFunctionName(), result.isSuccess(), result.getExecutionTime());
        
        return result;
    }
    
    /**
     * 执行邮件发送功能
     */
    private FunctionExecutionResult executeSendEmail(IntentAnalysisResult intent) {
        log.info("执行邮件发送功能");
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // 提取邮件参数
            String toEmail = extractEmailParameter(params, "toEmail", intent.getOriginalText());
            String subject = extractEmailParameter(params, "subject", intent.getOriginalText());
            String content = extractEmailParameter(params, "content", intent.getOriginalText());
            
            // 设置默认发件人
            String fromEmail = "system@elderly-companion.com";
            String senderName = "智能助手";
            
            // 获取用户信息，用于邮件个性化
            String userId = extractEmailParameter(params, "userId", intent.getOriginalText());
            String userName = extractEmailParameter(params, "userName", intent.getOriginalText());
            
            // 如果参数中没有用户信息，尝试从context中获取
            if (userName == null || userName.trim().isEmpty()) {
                userName = "用户"; // 默认用户名
            }
            
            // 个性化邮件内容，保持原标题 - 使用英文模板
            String personalizedContent = String.format("Hello!\n\n%s\n\nThis email was sent by %s through AI Assistant.\n\nBest regards!", 
                content, userName);
            
            // 验证必要参数
            if (toEmail == null || subject == null || content == null) {
                return FunctionExecutionResult.builder()
                    .functionName("send_email")
                    .success(false)
                    .errorMessage("缺少必要参数：收件人、主题或内容")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            // 调用主项目邮件服务
            com.voicecommand.model.EmailResponse emailResponse = emailServiceClient.sendEmail(
                fromEmail, toEmail, subject, personalizedContent, userName);
            
            if (emailResponse.isSuccess()) {
                // 构建成功反馈 - 使用英文
                String feedbackText = String.format(
                    "Email sent successfully! Recipient: %s, Subject: %s, Content: %s", 
                    toEmail, subject, content);
                
                return FunctionExecutionResult.builder()
                    .functionName("send_email")
                    .success(true)
                    .resultData(emailResponse)
                    .feedbackText(feedbackText)
                    .status(FunctionExecutionResult.ExecutionStatus.COMPLETED)
                    .build();
                    
            } else {
                return FunctionExecutionResult.builder()
                    .functionName("send_email")
                    .success(false)
                    .errorMessage("邮件发送失败：" + emailResponse.getErrorMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("邮件发送功能执行失败", e);
            return FunctionExecutionResult.builder()
                .functionName("send_email")
                .success(false)
                .errorMessage("邮件发送功能执行失败：" + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
    
    /**
     * 从参数或原始文本中提取邮件参数
     */
    private String extractEmailParameter(Map<String, Object> params, String paramName, String originalText) {
        // 首先尝试从AI解析的参数中获取
        if (params != null && params.containsKey(paramName)) {
            return (String) params.get(paramName);
        }
        
        // 如果参数中没有，尝试从原始文本中智能提取
        return extractParameterFromText(originalText, paramName);
    }
    
    /**
     * 从原始文本中智能提取参数
     */
    private String extractParameterFromText(String text, String paramName) {
        switch (paramName) {
            case "toEmail":
                // 提取收件人（简化处理，实际应该更智能）
                if (text.contains("给") && text.contains("发邮件")) {
                    int start = text.indexOf("给") + 1;
                    int end = text.indexOf("发邮件");
                    if (start < end) {
                        return text.substring(start, end).trim();
                    }
                }
                break;
                
            case "subject":
                // 提取主题
                if (text.contains("主题是") || text.contains("主题：")) {
                    int start = text.indexOf("主题是");
                    if (start == -1) start = text.indexOf("主题：");
                    if (start != -1) {
                        start += 3;
                        int end = text.indexOf("，", start);
                        if (end == -1) end = text.indexOf("，内容", start);
                        if (end == -1) end = text.length();
                        return text.substring(start, end).trim();
                    }
                }
                break;
                
            case "content":
                // 提取内容
                if (text.contains("内容是") || text.contains("内容：")) {
                    int start = text.indexOf("内容是");
                    if (start == -1) start = text.indexOf("内容：");
                    if (start != -1) {
                        start += 3;
                        return text.substring(start).trim();
                    }
                }
                break;
        }
        
        return null;
    }
    
    /**
     * 从参数或原始文本中提取日程参数
     */
    private String extractScheduleParameter(Map<String, Object> params, String paramName, String originalText) {
        // 首先尝试从AI解析的参数中获取
        if (params != null && params.containsKey(paramName)) {
            return (String) params.get(paramName);
        }
        
        // 如果参数中没有，尝试从原始文本中智能提取
        return extractScheduleParameterFromText(originalText, paramName);
    }
    
    /**
     * 从原始文本中智能提取日程参数
     */
    private String extractScheduleParameterFromText(String text, String paramName) {
        switch (paramName) {
            case "title":
                // 提取标题（简化处理）
                if (text.contains("添加") || text.contains("add")) {
                    int start = text.indexOf("添加");
                    if (start == -1) start = text.indexOf("add");
                    start += (text.contains("添加") ? 2 : 3);
                    
                    int end = text.length();
                    if (text.contains("日程") || text.contains("schedule")) {
                        end = text.indexOf("日程");
                        if (end == -1) end = text.indexOf("schedule");
                    }
                    
                    if (start < end) {
                        return text.substring(start, end).trim();
                    }
                }
                break;
                
            case "date":
                // 提取日期（简化处理）
                if (text.contains("明天")) {
                    return java.time.LocalDate.now().plusDays(1).toString();
                } else if (text.contains("后天")) {
                    return java.time.LocalDate.now().plusDays(2).toString();
                } else if (text.contains("下周")) {
                    return java.time.LocalDate.now().plusDays(7).toString();
                }
                break;
                
            case "time":
                // 提取时间（简化处理）
                if (text.contains("下午") || text.contains("pm")) {
                    if (text.contains("下午")) {
                        int start = text.indexOf("下午") + 2;
                        if (start < text.length()) {
                            String afterAfternoon = text.substring(start);
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                            java.util.regex.Matcher matcher = pattern.matcher(afterAfternoon);
                            if (matcher.find()) {
                                int hour = Integer.parseInt(matcher.group(1));
                                return String.format("%02d:00", hour + 12);
                            }
                        }
                    }
                } else if (text.contains("早上") || text.contains("am")) {
                    if (text.contains("早上")) {
                        int start = text.indexOf("早上") + 2;
                        if (start < text.length()) {
                            String afterMorning = text.substring(start);
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                            java.util.regex.Matcher matcher = pattern.matcher(afterMorning);
                            if (matcher.find()) {
                                int hour = Integer.parseInt(matcher.group(1));
                                return String.format("%02d:00", hour);
                            }
                        }
                    }
                }
                break;
                
            case "category":
                // 提取类别
                if (text.contains("早上") || text.contains("morning")) {
                    return "morning";
                } else if (text.contains("下午") || text.contains("afternoon")) {
                    return "afternoon";
                } else if (text.contains("晚上") || text.contains("evening")) {
                    return "evening";
                } else if (text.contains("吃药") || text.contains("medication")) {
                    return "medication";
                }
                break;
        }
        
        return null;
    }
    
    /**
     * 执行添加日程功能
     */
    private FunctionExecutionResult executeAddSchedule(IntentAnalysisResult intent) {
        log.info("执行添加日程功能");
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // 提取日程参数
            String title = extractScheduleParameter(params, "title", intent.getOriginalText());
            String date = extractScheduleParameter(params, "date", intent.getOriginalText());
            String time = extractScheduleParameter(params, "time", intent.getOriginalText());
            String category = extractScheduleParameter(params, "category", intent.getOriginalText());
            String description = extractScheduleParameter(params, "description", intent.getOriginalText());
            String priority = extractScheduleParameter(params, "priority", intent.getOriginalText());
            String userId = extractScheduleParameter(params, "userId", intent.getOriginalText());
            
            // 验证必要参数
            if (title == null || title.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_schedule")
                    .success(false)
                    .errorMessage("缺少必要参数：日程标题")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            // 构建日程数据
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("title", title);
            scheduleData.put("date", date != null ? date : java.time.LocalDate.now().toString());
            scheduleData.put("time", time != null ? time : "09:00");
            scheduleData.put("category", category != null ? category : "afternoon");
            if (description != null && !description.trim().isEmpty()) {
                scheduleData.put("description", description);
            }
            scheduleData.put("priority", priority != null ? priority : "medium");
            
            // 调用日程管理服务
            ScheduleManagementService.ScheduleResponse scheduleResponse = 
                scheduleManagementService.addSchedule(scheduleData, userId != null ? userId : "1");
            
            if (scheduleResponse.isSuccess()) {
                // 构建成功反馈
                String feedbackText = String.format(
                    "日程添加成功！标题：%s，日期：%s，时间：%s，类别：%s", 
                    title, date, time, category);
                
                return FunctionExecutionResult.builder()
                    .functionName("add_schedule")
                    .success(true)
                    .resultData(scheduleResponse)
                    .feedbackText(feedbackText)
                    .status(FunctionExecutionResult.ExecutionStatus.COMPLETED)
                    .build();
                    
            } else {
                return FunctionExecutionResult.builder()
                    .functionName("add_schedule")
                    .success(false)
                    .errorMessage("日程添加失败：" + scheduleResponse.getErrorMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("日程添加功能执行失败", e);
            return FunctionExecutionResult.builder()
                .functionName("add_schedule")
                .success(false)
                .errorMessage("日程添加功能执行失败：" + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
    
    /**
     * 执行健康检查功能（待实现）
     */
    private FunctionExecutionResult executeHealthCheck(IntentAnalysisResult intent) {
        return FunctionExecutionResult.builder()
            .functionName("health_check")
            .success(false)
            .errorMessage("健康检查功能暂未实现")
            .status(FunctionExecutionResult.ExecutionStatus.FAILED)
            .build();
    }
    
    /**
     * 执行宠物交互功能（待实现）
     */
    private FunctionExecutionResult executePetInteraction(IntentAnalysisResult intent) {
        return FunctionExecutionResult.builder()
            .functionName("pet_interaction")
            .success(false)
            .errorMessage("宠物交互功能暂未实现")
            .status(FunctionExecutionResult.ExecutionStatus.FAILED)
            .build();
    }
    
    /**
     * 执行添加重要日期功能
     */
    private FunctionExecutionResult executeAddImportantDate(IntentAnalysisResult intent) {
        log.info("开始执行添加重要日期功能，参数: {}", intent.getParameters());
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // 提取重要日期参数
            String title = extractScheduleParameter(params, "title", intent.getOriginalText());
            String date = extractScheduleParameter(params, "date", intent.getOriginalText());
            String type = extractScheduleParameter(params, "type", intent.getOriginalText());
            String description = extractScheduleParameter(params, "description", intent.getOriginalText());
            
            // 验证必要参数
            if (title == null || title.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("缺少必要参数：重要日期标题")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            if (date == null || date.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("缺少必要参数：重要日期")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            if (type == null || type.trim().isEmpty()) {
                type = "custom"; // 默认类型
            }
            
            // 构建重要日期数据
            Map<String, Object> importantDateData = new HashMap<>();
            importantDateData.put("title", title);
            importantDateData.put("date", date);
            importantDateData.put("type", type);
            if (description != null && !description.trim().isEmpty()) {
                importantDateData.put("description", description);
            }
            
            // 调用重要日期管理服务
            ImportantDateManagementService.ImportantDateResponse response = 
                importantDateManagementService.addImportantDate(importantDateData);
            
            if (response.isSuccess()) {
                log.info("重要日期添加成功: {}", response.getMessage());
                
                // 构建成功结果
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("importantDateId", response.getImportantDateId());
                resultData.put("title", title);
                resultData.put("date", date);
                resultData.put("type", type);
                
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(true)
                    .feedbackText("重要日期添加成功！标题：" + title + "，日期：" + date + "，类型：" + type)
                    .resultData(resultData)
                    .status(FunctionExecutionResult.ExecutionStatus.COMPLETED)
                    .build();
                    
            } else {
                log.error("重要日期添加失败: {}", response.getMessage());
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("重要日期添加失败：" + response.getMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
                }
                
        } catch (Exception e) {
            log.error("执行添加重要日期功能时发生异常: {}", e.getMessage(), e);
            return FunctionExecutionResult.builder()
                .functionName("add_important_date")
                .success(false)
                .errorMessage("重要日期添加失败：" + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
}
