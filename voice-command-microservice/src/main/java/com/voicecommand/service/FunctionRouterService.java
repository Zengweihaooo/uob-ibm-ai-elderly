package com.voicecommand.service;

import com.voicecommand.model.IntentAnalysisResult;
import com.voicecommand.model.FunctionExecutionResult;
import com.voicecommand.client.EmailServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Function Router Service
 * 
 * Routes to appropriate function executors based on AI intent analysis
 * Supports auto-calling capabilities like email sending and schedule management
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
     * Execute function
     * 
     * @param intent AI intent analysis result
     * @return function execution result
     */
    public FunctionExecutionResult executeFunction(IntentAnalysisResult intent) {
        log.info("Start executing function: {}", intent.getFunctionName());
        
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
                    result.setErrorMessage("Unknown function: " + intent.getFunctionName());
                    result.setStatus(FunctionExecutionResult.ExecutionStatus.FAILED);
                    break;
            }
            
        } catch (Exception e) {
            log.error("Error while executing function", e);
            result.setSuccess(false);
            result.setErrorMessage("Error while executing function: " + e.getMessage());
            result.setStatus(FunctionExecutionResult.ExecutionStatus.FAILED);
        }
        
        result.setEndTime(System.currentTimeMillis());
        result.setExecutionTime(result.getEndTime() - result.getStartTime());
        
        if (result.isSuccess()) {
            result.setStatus(FunctionExecutionResult.ExecutionStatus.COMPLETED);
        }
        
        log.info("Function execution finished: function={}, success={}, time={}ms", 
                intent.getFunctionName(), result.isSuccess(), result.getExecutionTime());
        
        return result;
    }
    
    /**
     * Execute email sending function
     */
    private FunctionExecutionResult executeSendEmail(IntentAnalysisResult intent) {
        log.info("Execute email sending");
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // Extract email parameters
            String toEmail = extractEmailParameter(params, "toEmail", intent.getOriginalText());
            String subject = extractEmailParameter(params, "subject", intent.getOriginalText());
            String content = extractEmailParameter(params, "content", intent.getOriginalText());
            
            // Default sender
            String fromEmail = "system@elderly-companion.com";
            String senderName = "AI Assistant";
            
            // Get user info for personalization
            String userId = extractEmailParameter(params, "userId", intent.getOriginalText());
            String userName = extractEmailParameter(params, "userName", intent.getOriginalText());
            
            // If user info not in params, fallback to defaults
            if (userName == null || userName.trim().isEmpty()) {
                userName = "User"; // default user name
            }
            
            // Personalize email content - English template
            String personalizedContent = String.format("Hello!\n\n%s\n\nThis email was sent by %s through AI Assistant.\n\nBest regards!", 
                content, userName);
            
            // Validate required params
            if (toEmail == null || subject == null || content == null) {
                return FunctionExecutionResult.builder()
                    .functionName("send_email")
                    .success(false)
                    .errorMessage("Missing required parameters: recipient, subject or content")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            // Call main project's email service
            com.voicecommand.model.EmailResponse emailResponse = emailServiceClient.sendEmail(
                fromEmail, toEmail, subject, personalizedContent, userName);
            
            if (emailResponse.isSuccess()) {
                // Build success feedback - English
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
                    .errorMessage("Email sending failed: " + emailResponse.getErrorMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Email sending execution failed", e);
            return FunctionExecutionResult.builder()
                .functionName("send_email")
                .success(false)
                .errorMessage("Email sending execution failed: " + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Extract email parameter from params or original text
     */
    private String extractEmailParameter(Map<String, Object> params, String paramName, String originalText) {
        // Try from AI parsed params first
        if (params != null && params.containsKey(paramName)) {
            return (String) params.get(paramName);
        }
        
        // Otherwise, try extracting from original text
        return extractParameterFromText(originalText, paramName);
    }
    
    /**
     * Extract parameter from original text
     */
    private String extractParameterFromText(String text, String paramName) {
        switch (paramName) {
            case "toEmail":
                // Extract recipient (simplified)
                if (text.contains("给") && text.contains("发邮件")) {
                    int start = text.indexOf("给") + 1;
                    int end = text.indexOf("发邮件");
                    if (start < end) {
                        return text.substring(start, end).trim();
                    }
                }
                break;
                
            case "subject":
                // Extract subject
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
                // Extract content
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
     * Extract schedule parameter from params or original text
     */
    private String extractScheduleParameter(Map<String, Object> params, String paramName, String originalText) {
        // Try from AI parsed params first
        if (params != null && params.containsKey(paramName)) {
            return (String) params.get(paramName);
        }
        
        // Otherwise, try extracting from original text
        return extractScheduleParameterFromText(originalText, paramName);
    }
    
    /**
     * Extract schedule parameter from original text
     */
    private String extractScheduleParameterFromText(String text, String paramName) {
        switch (paramName) {
            case "title":
                // Extract title (simplified)
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
                // Extract date (simplified)
                if (text.contains("明天")) {
                    return java.time.LocalDate.now().plusDays(1).toString();
                } else if (text.contains("后天")) {
                    return java.time.LocalDate.now().plusDays(2).toString();
                } else if (text.contains("下周")) {
                    return java.time.LocalDate.now().plusDays(7).toString();
                }
                break;
                
            case "time":
                // Extract time (simplified)
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
                // Extract category
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
     * Execute add schedule function
     */
    private FunctionExecutionResult executeAddSchedule(IntentAnalysisResult intent) {
        log.info("Execute add schedule");
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // Extract schedule params
            String title = extractScheduleParameter(params, "title", intent.getOriginalText());
            String date = extractScheduleParameter(params, "date", intent.getOriginalText());
            String time = extractScheduleParameter(params, "time", intent.getOriginalText());
            String category = extractScheduleParameter(params, "category", intent.getOriginalText());
            String description = extractScheduleParameter(params, "description", intent.getOriginalText());
            String priority = extractScheduleParameter(params, "priority", intent.getOriginalText());
            String userId = extractScheduleParameter(params, "userId", intent.getOriginalText());
            
            // Validate required params
            if (title == null || title.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_schedule")
                    .success(false)
                    .errorMessage("Missing required parameter: schedule title")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            // Build schedule data
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("title", title);
            scheduleData.put("date", date != null ? date : java.time.LocalDate.now().toString());
            scheduleData.put("time", time != null ? time : "09:00");
            scheduleData.put("category", category != null ? category : "afternoon");
            if (description != null && !description.trim().isEmpty()) {
                scheduleData.put("description", description);
            }
            scheduleData.put("priority", priority != null ? priority : "medium");
            
            // Call schedule service
            ScheduleManagementService.ScheduleResponse scheduleResponse = 
                scheduleManagementService.addSchedule(scheduleData, userId != null ? userId : "1");
            
            if (scheduleResponse.isSuccess()) {
                // Build success feedback
                String feedbackText = String.format(
                    "Schedule added successfully! Title: %s, Date: %s, Time: %s, Category: %s", 
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
                    .errorMessage("Schedule add failed: " + scheduleResponse.getErrorMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Add schedule execution failed", e);
            return FunctionExecutionResult.builder()
                .functionName("add_schedule")
                .success(false)
                .errorMessage("Add schedule execution failed: " + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
    
    /**
     * Execute health check (TBD)
     */
    private FunctionExecutionResult executeHealthCheck(IntentAnalysisResult intent) {
        return FunctionExecutionResult.builder()
            .functionName("health_check")
            .success(false)
            .errorMessage("Health check is not implemented yet")
            .status(FunctionExecutionResult.ExecutionStatus.FAILED)
            .build();
    }
    
    /**
     * Execute pet interaction (TBD)
     */
    private FunctionExecutionResult executePetInteraction(IntentAnalysisResult intent) {
        return FunctionExecutionResult.builder()
            .functionName("pet_interaction")
            .success(false)
            .errorMessage("Pet interaction is not implemented yet")
            .status(FunctionExecutionResult.ExecutionStatus.FAILED)
            .build();
    }
    
    /**
     * Execute add important date
     */
    private FunctionExecutionResult executeAddImportantDate(IntentAnalysisResult intent) {
        log.info("Start execute add important date, params: {}", intent.getParameters());
        
        try {
            Map<String, Object> params = intent.getParameters();
            
            // Extract important date params
            String title = extractScheduleParameter(params, "title", intent.getOriginalText());
            String date = extractScheduleParameter(params, "date", intent.getOriginalText());
            String type = extractScheduleParameter(params, "type", intent.getOriginalText());
            String description = extractScheduleParameter(params, "description", intent.getOriginalText());
            
            // Validate required params
            if (title == null || title.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("Missing required parameter: important date title")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            if (date == null || date.trim().isEmpty()) {
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("Missing required parameter: important date")
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
            }
            
            if (type == null || type.trim().isEmpty()) {
                type = "custom"; // default type
            }
            
            // Build important date data
            Map<String, Object> importantDateData = new HashMap<>();
            importantDateData.put("title", title);
            importantDateData.put("date", date);
            importantDateData.put("type", type);
            if (description != null && !description.trim().isEmpty()) {
                importantDateData.put("description", description);
            }
            
            // Call important date service
            ImportantDateManagementService.ImportantDateResponse response = 
                importantDateManagementService.addImportantDate(importantDateData);
            
            if (response.isSuccess()) {
                log.info("Important date added successfully: {}", response.getMessage());
                
                // Build success result
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("importantDateId", response.getImportantDateId());
                resultData.put("title", title);
                resultData.put("date", date);
                resultData.put("type", type);
                
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(true)
                    .feedbackText("Important date added successfully! Title: " + title + ", Date: " + date + ", Type: " + type)
                    .resultData(resultData)
                    .status(FunctionExecutionResult.ExecutionStatus.COMPLETED)
                    .build();
                    
            } else {
                log.error("Failed to add important date: {}", response.getMessage());
                return FunctionExecutionResult.builder()
                    .functionName("add_important_date")
                    .success(false)
                    .errorMessage("Important date add failed: " + response.getMessage())
                    .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                    .build();
                }
                
        } catch (Exception e) {
            log.error("Exception while executing add important date: {}", e.getMessage(), e);
            return FunctionExecutionResult.builder()
                .functionName("add_important_date")
                .success(false)
                .errorMessage("Important date add failed: " + e.getMessage())
                .status(FunctionExecutionResult.ExecutionStatus.FAILED)
                .build();
        }
    }
}
