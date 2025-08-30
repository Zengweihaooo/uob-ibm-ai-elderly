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
    
    // 其他功能服务可以在这里注入
    // @Autowired
    // private ScheduleService scheduleService;
    
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
     * 执行添加日程功能（待实现）
     */
    private FunctionExecutionResult executeAddSchedule(IntentAnalysisResult intent) {
        return FunctionExecutionResult.builder()
            .functionName("add_schedule")
            .success(false)
            .errorMessage("日程功能暂未实现")
            .status(FunctionExecutionResult.ExecutionStatus.FAILED)
            .build();
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
}
