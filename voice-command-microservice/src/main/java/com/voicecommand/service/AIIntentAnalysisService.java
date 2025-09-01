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
 * AI Intent Analysis Service
 *
 * Uses the main project's Gemini AI to analyze user voice commands
 * Automatically identifies the function and parameters the user wants to execute
 * Falls back to local simulated analysis when the main AI service is unavailable
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
     * Analyze user intent
     *
     * @param userText user input text
     * @param context context information
     * @return intent analysis result
     */
    public IntentAnalysisResult analyzeIntent(String userText, Map<String, Object> context) {
        log.info("Start analyzing user intent: {}", userText);
        
        try {
            // First try to call the real AI service for analysis
            log.info("Trying to call AI service for intent analysis...");
            String analysisPrompt = buildAnalysisPrompt(userText, context);
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("message", analysisPrompt);
            
            try {
                Map<String, Object> aiResponse = aiServiceClient.chatWithGemini(aiRequest);
                IntentAnalysisResult result = parseAIResponse(aiResponse, userText);
                log.info("AI intent analysis done: function={}, confidence={}", result.getFunctionName(), result.getConfidence());
                return result;
            } catch (Exception e) {
                log.warn("AI service call failed, using local simulated analysis as fallback: {}", e.getMessage());
                // Use local simulated analysis as fallback
                return performLocalIntentAnalysis(userText, context);
            }
        } catch (Exception e) {
            log.error("Error during intent analysis, using local fallback", e);
            return performLocalIntentAnalysis(userText, context);
        }
    }

    // Local simulated intent analysis (used when main AI service is unavailable)
    private IntentAnalysisResult performLocalIntentAnalysis(String userText, Map<String, Object> context) {
        log.info("Analyze user intent locally: {}", userText);
        
        // Simple keyword matching
        String lowerText = userText.toLowerCase();
        
        // Email sending intent detection - support both Chinese and English keywords
        if (lowerText.contains("邮件") || lowerText.contains("email") || 
            lowerText.contains("发送") || lowerText.contains("发邮件") || lowerText.contains("send") ||
            lowerText.contains("mail") || lowerText.contains("message") ||
            (lowerText.contains("给") && (lowerText.contains("发") || lowerText.contains("发送"))) ||
            (lowerText.contains("to") && (lowerText.contains("send") || lowerText.contains("mail"))) ||
            lowerText.contains("使用发送邮件功能") || lowerText.contains("send email")) {
             
             Map<String, Object> params = new HashMap<>();
             
            // Extract subject and content
            String subject = extractEmailSubject(userText);
            String content = extractEmailContent(userText);
            
            if (content == null || content.isEmpty()) {
                log.warn("Failed to extract valid email content");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("Local analysis: unable to identify valid email content")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // Extract recipient (name or email)
            String recipient = extractRecipient(userText);
            String email = extractEmailAddress(userText);
            
            if (email != null && !email.isEmpty()) {
                // Found email address, use it
                params.put("toEmail", email);
                params.put("content", content);
                params.put("subject", subject);
                
                // Add user info for personalization
                String userName = extractUserNameFromContext(context);
                params.put("userName", userName);
                params.put("userId", extractUserIdFromContext(context));
                
                log.info("Local analysis result - function: send_email, toEmail: {}, content: {}, subject: {}, userName: {}", email, content, subject, userName);
                
                return IntentAnalysisResult.builder()
                    .functionName("send_email")
                    .confidence(0.9)
                    .parameters(params)
                    .reasoning("Based on keywords 'email', 'send', etc., identified as email sending intent with email, subject and content")
                    .knowledgeUsed("Local keyword matching analysis")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            } else {
                // No email address but has recipient name and content, try DB lookup
                log.info("Try to lookup recipient email from DB: {}", recipient);
                
                // Lookup email
                String emailFromDB = contactLookupService.lookupEmailByName(recipient, null);
                
                if (emailFromDB != null && !emailFromDB.isEmpty()) {
                    // Found email in DB
                    params.put("toEmail", emailFromDB);
                    params.put("content", content);
                    params.put("subject", subject);
                    params.put("recipientName", recipient);
                    params.put("source", "database");
                    
                    // Add user info for personalization
                    String userName = extractUserNameFromContext(context);
                    params.put("userName", userName);
                    params.put("userId", extractUserIdFromContext(context));
                    
                    log.info("Found recipient email from DB: {} -> {}, userName: {}, subject: {}", recipient, emailFromDB, userName, subject);
                    
                    return IntentAnalysisResult.builder()
                        .functionName("send_email")
                        .confidence(0.95)
                        .parameters(params)
                        .reasoning("Based on keywords 'email', 'send', etc., identified as email intent with recipient name, subject, and content; email auto-fetched from DB")
                        .knowledgeUsed("Local keyword matching + DB contact lookup")
                        .originalText(userText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("local-fallback")
                        .build();
                } else {
                    // Not found in DB, generate hint
                    params.put("recipientName", recipient);
                    params.put("content", content);
                    params.put("subject", subject);
                    params.put("needsEmail", true);
                    params.put("message", "Email sending intent detected, recipient email address is required");
                    params.put("searchedDatabase", true);
                    
                    // Add user info for personalization
                    String userName = extractUserNameFromContext(context);
                    params.put("userName", userName);
                    params.put("userId", extractUserIdFromContext(context));
                    
                    log.info("Local analysis result - function: send_email, recipient: {}, content: {}, subject: {}, userName: {}, DB not found", recipient, content, subject, userName);
                    
                    return IntentAnalysisResult.builder()
                        .functionName("send_email")
                        .confidence(0.8)
                        .parameters(params)
                        .reasoning("Based on keywords 'email', 'send', identified as email intent with recipient name, subject, content; DB lookup attempted but not found")
                        .knowledgeUsed("Local keyword matching + DB contact lookup")
                        .originalText(userText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("local-fallback")
                        .build();
                }
            }
        }
        
        // Important date management intent detection - supports CN/EN keywords (higher priority)
        if (lowerText.contains("重要日期") || lowerText.contains("important date") || 
            lowerText.contains("生日") || lowerText.contains("birthday") ||
            lowerText.contains("纪念日") || lowerText.contains("anniversary") ||
            lowerText.contains("节日") || lowerText.contains("holiday") ||
            (lowerText.contains("添加") || lowerText.contains("add") || lowerText.contains("设置") || lowerText.contains("set")) &&
            (lowerText.contains("生日") || lowerText.contains("birthday") || lowerText.contains("纪念日") || lowerText.contains("anniversary"))) {
             
             Map<String, Object> params = new HashMap<>();
             
            // Extract important date info
            String title = extractImportantDateTitle(userText);
            String date = extractImportantDateDate(userText);
            String type = extractImportantDateType(userText);
            String description = extractImportantDateDescription(userText);
            
            if (title == null || title.isEmpty()) {
                log.warn("Failed to extract valid important date title");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("Local analysis: unable to identify valid important date title")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // Set params
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
            
            // Add user info
            String userName = extractUserNameFromContext(context);
            params.put("userName", userName);
            params.put("userId", extractUserIdFromContext(context));
            
            log.info("Local analysis result - function: add_important_date, title: {}, date: {}, type: {}, userName: {}", 
                title, date, type, userName);
            
            return IntentAnalysisResult.builder()
                .functionName("add_important_date")
                .confidence(0.9)
                .parameters(params)
                .reasoning("Based on keywords 'important date', 'birthday', 'anniversary', identified important date management intent with title/date/type")
                .knowledgeUsed("Local keyword matching analysis")
                .originalText(userText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("local-fallback")
                .build();
        }
        
        // Schedule management intent detection - supports CN/EN keywords
        if (lowerText.contains("日程") || lowerText.contains("schedule") || 
            lowerText.contains("安排") || lowerText.contains("添加") || lowerText.contains("add") ||
            lowerText.contains("设置") || lowerText.contains("set") || lowerText.contains("预约") ||
            lowerText.contains("提醒") || lowerText.contains("reminder") ||
            (lowerText.contains("明天") || lowerText.contains("后天") || lowerText.contains("下周")) ||
            (lowerText.contains("tomorrow") || lowerText.contains("next") || lowerText.contains("schedule"))) {
             
             Map<String, Object> params = new HashMap<>();
             
            // Extract schedule info
            String title = extractScheduleTitle(userText);
            String date = extractScheduleDate(userText);
            String time = extractScheduleTime(userText);
            String category = extractScheduleCategory(userText);
            String description = extractScheduleDescription(userText);
            String priority = extractSchedulePriority(userText);
            
            if (title == null || title.isEmpty()) {
                log.warn("Failed to extract valid schedule title");
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("Local analysis: unable to identify valid schedule title")
                    .originalText(userText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("local-fallback")
                    .build();
            }
            
            // Set params
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
            
            // Add user info
            String userName = extractUserNameFromContext(context);
            params.put("userName", userName);
            params.put("userId", extractUserIdFromContext(context));
            
            log.info("Local analysis result - function: add_schedule, title: {}, date: {}, time: {}, category: {}, userName: {}", 
                title, date, time, category, userName);
            
            return IntentAnalysisResult.builder()
                .functionName("add_schedule")
                .confidence(0.9)
                .parameters(params)
                .reasoning("Based on keywords 'schedule', 'arrange', 'add', identified schedule management intent with title/date/time")
                .knowledgeUsed("Local keyword matching analysis")
                .originalText(userText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("local-fallback")
                .build();
        }
        

        
        // Other intents can be added here...
        
        return IntentAnalysisResult.builder()
            .functionName("unknown")
            .confidence(0.3)
            .reasoning("Unable to identify user intent, please use text input")
            .originalText(userText)
            .analysisTimestamp(System.currentTimeMillis())
            .aiModel("local-fallback")
            .build();
    }
    
    // Extract email address
    private String extractEmailAddress(String text) {
        log.info("Start extracting email address, input: {}", text);
        
        // Use permissive regex to match email
        String emailPattern = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String email = matcher.group();
            log.info("Extracted email: {}", email);
            return email;
        }
        
        // If regex fails, try simple '@' matching
        if (text.contains("@")) {
            String[] parts = text.split("@");
            if (parts.length == 2) {
                String localPart = parts[0];
                String domainPart = parts[1];
                
                // Find domain part from the end
                int dotIndex = domainPart.lastIndexOf('.');
                if (dotIndex > 0) {
                    String domain = domainPart.substring(0, dotIndex);
                    String tld = domainPart.substring(dotIndex);
                    
                    // Build email
                    String email = localPart + "@" + domain + tld;
                    log.info("Extracted email via '@' matching: {}", email);
                    return email;
                }
            }
        }
        
        log.warn("No valid email format found");
        return null;
    }
    
    // Extract email content
    private String extractEmailContent(String text) {
        String lowerText = text.toLowerCase();
        
        // Try multiple patterns
        String content = null;
        
        // Pattern 1: "内容是[content]" or "content is [content]"
        if (lowerText.contains("内容是") || lowerText.contains("content is")) {
            int start;
            if (lowerText.contains("内容是")) {
                start = text.indexOf("内容是") + 4;
            } else {
                start = text.indexOf("content is") + 12;
            }
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                // Remove email from content
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("Pattern1 content: {}", content);
                    return content;
                }
            }
        }
        
        // Pattern 2: "发送[content]" or "send [content]"
        if (lowerText.contains("发送") || lowerText.contains("send")) {
            int start = lowerText.indexOf("发送");
            if (start == -1) start = lowerText.indexOf("send");
            start += (lowerText.contains("发送") ? 2 : 4);
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                // Remove email from content
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("Pattern2 content: {}", content);
                    return content;
                }
            }
        }
        
        // Pattern 3: "说[content]" or "say [content]"
        if (lowerText.contains("说") || lowerText.contains("say")) {
            int start = lowerText.indexOf("说");
            if (start == -1) start = lowerText.indexOf("say");
            start += (lowerText.contains("说") ? 1 : 3);
            
            if (start < text.length()) {
                content = text.substring(start).trim();
                content = removeEmailFromContent(content);
                if (!content.isEmpty()) {
                    log.info("Pattern3 content: {}", content);
                    return content;
                }
            }
        }
        
        // Pattern 4: content after email
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(emailPattern);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            int emailEnd = matcher.end();
            if (emailEnd < text.length()) {
                content = text.substring(emailEnd).trim();
                // Remove keywords like "发送"
                content = content.replaceAll("发送", "").replaceAll("发邮件", "").trim();
                if (!content.isEmpty()) {
                    log.info("Pattern4 content: {}", content);
                    return content;
                }
            }
        }
        
        log.warn("Failed to extract valid email content");
        return null;
    }
    
    // Extract email subject
    private String extractEmailSubject(String text) {
        String lowerText = text.toLowerCase();
        
        // Try to extract subject
        String subject = null;
        
        // Pattern 1: "主题是[subject]" or "subject is [subject]"
        if (lowerText.contains("主题是") || lowerText.contains("subject is")) {
            int start;
            if (lowerText.contains("主题是")) {
                start = text.indexOf("主题是") + 4;
            } else {
                start = text.indexOf("subject is") + 12;
            }
            
            if (start < text.length()) {
                // Find next keyword
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
                    // Clean subject
                    subject = subject.replaceAll("[,，。.]+$", "").trim();
                    if (!subject.isEmpty()) {
                        log.info("Pattern1 subject: {}", subject);
                        return subject;
                    }
                }
            }
        }
        
        // If not found, return default subject
        log.info("No explicit subject found, using default subject");
        return "Message from AI Assistant";
    }
    
    // Remove email and irrelevant keywords from content
    private String removeEmailFromContent(String content) {
        if (content == null) return "";
        
        // Remove email address
        String emailPattern = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        content = content.replaceAll(emailPattern, "");
        
        // Remove irrelevant keywords related to email
        content = content.replaceAll("邮件", "");
        content = content.replaceAll("发送", "");
        content = content.replaceAll("发邮件", "");
        content = content.replaceAll("给", "");
        
        // Do not remove "主题是" and "内容是"
        // content = content.replaceAll("主题是", "");
        // content = content.replaceAll("内容是", "");
        
        // Smartly remove "到" but keep meaningful usage
        // e.g., "我快到了" -> keep; "到邮箱" -> remove
        if (content.contains("到") && !content.matches(".*[你我他她它].*到.*")) {
            content = content.replaceAll("到", "");
        }
        
        // Trim extra spaces and punctuation
        content = content.replaceAll("\\s+", " ").trim();
        content = content.replaceAll("^[,，。.]+", "").replaceAll("[,，。.]+$", "");
        
        // If content is empty or only contains spaces, return null
        if (content.trim().isEmpty()) {
            return null;
        }
        
        return content;
    }
    
    /**
     * Build AI analysis prompt
     */
    private String buildAnalysisPrompt(String userText, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an intelligent assistant that analyzes the user's voice command and determines which function to execute.\n\n");
        prompt.append("User input: ").append(userText).append("\n\n");
        
        prompt.append("Below is the function knowledge base you can use:\n");
        prompt.append("Carefully read each function's description, parameter requirements and examples, then analyze the user's intent.\n\n");
        
        // Use knowledge base service to get functions
        if (functionKnowledgeService.isKnowledgeBaseLoaded()) {
            List<FunctionInfo> functions = functionKnowledgeService.getAllFunctions();
            for (FunctionInfo function : functions) {
                prompt.append("Function name: ").append(function.getName()).append("\n");
                prompt.append("Function description: ").append(function.getDescription()).append("\n");
                prompt.append("Parameter requirements:\n");
                
                function.getParameters().forEach((paramName, paramInfo) -> {
                    prompt.append("- ").append(paramName).append(": ").append(paramInfo.getDescription());
                    if (paramInfo.isRequired()) {
                        prompt.append(" (required)");
                    } else {
                        prompt.append(" (optional)");
                    }
                    prompt.append("\n");
                });
                
                prompt.append("Examples:\n");
                for (String example : function.getExamples()) {
                    prompt.append("- ").append(example).append("\n");
                }
                prompt.append("\n");
            }
        } else {
            // If KB not loaded, use hard-coded email function info as fallback
            prompt.append("Function name: send_email\n");
            prompt.append("Function description: Email sending function\n");
            prompt.append("Parameter requirements:\n");
            prompt.append("- toEmail: recipient email address (required)\n");
            prompt.append("- subject: email subject (required)\n");
            prompt.append("- content: email content (required)\n");
            prompt.append("- fromEmail: sender email (optional, defaults to system email)\n");
            prompt.append("Examples:\n");
            prompt.append("- Send an email to John Doe, subject is meeting reminder, content is meeting tomorrow at 3 PM\n");
            prompt.append("- Send an email to Jane Doe to tell her about tomorrow's meeting\n");
            prompt.append("- Write an email to Jack about the project progress\n\n");
        }
        
        prompt.append("Based on the knowledge base above, analyze the user's intent and return JSON:\n");
        prompt.append("{\n");
        prompt.append("  \"functionName\": \"name of the function to call\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"parameters\": {\"param1\": \"value1\"},\n");
        prompt.append("  \"reasoning\": \"reasoning based on the knowledge base\",\n");
        prompt.append("  \"knowledgeUsed\": \"which knowledge items were used\"\n");
        prompt.append("}\n\n");
        
        prompt.append("Important requirements:\n");
        prompt.append("1. Carefully analyze the user input to understand the real intent\n");
        prompt.append("2. Refer to the function descriptions and examples in the knowledge base\n");
        prompt.append("3. If the user's intent is unclear, ask for clarification\n");
        prompt.append("4. Ensure parameter extraction accuracy\n");
        prompt.append("5. Return JSON only, no extra text\n");
        prompt.append("6. Strictly follow the JSON format, do not add explanations\n");
        prompt.append("7. If recognized as email function, extract email address and content\n");
        prompt.append("8. Ensure the JSON format is fully correct and directly parsable\n");
        prompt.append("9. Do not add any prefixes, suffixes, or explanations\n");
        prompt.append("10. If the user says 'send email content to xxx', functionName must be 'send_email'\n");
        prompt.append("11. If the user says 'send email content to xxx', parameters must include toEmail and content\n");
        prompt.append("12. If the user says 'send email content to xxx', subject can be set to 'Message from AI Assistant'\n");
        prompt.append("13. Example: If the user says 'send an email to 15510399391@163.com saying I am almost there', the JSON should be:\n");
        prompt.append("{\n");
        prompt.append("  \"functionName\": \"send_email\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"parameters\": {\n");
        prompt.append("    \"toEmail\": \"15510399391@163.com\",\n");
        prompt.append("    \"subject\": \"Message from AI Assistant\",\n");
        prompt.append("    \"content\": \"I am almost there\"\n");
        prompt.append("  },\n");
        prompt.append("  \"reasoning\": \"The user explicitly wants to send an email including recipient and content\",\n");
        prompt.append("  \"knowledgeUsed\": \"Email function knowledge base\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }
    
    /**
     * Parse AI response
     */
    private IntentAnalysisResult parseAIResponse(Map<String, Object> aiResponse, String originalText) {
        try {
            log.info("Start parsing AI response, raw: {}", aiResponse);
            
            // Check for errors
            if (aiResponse.containsKey("error") || aiResponse.containsKey("status")) {
                String errorMsg = "";
                if (aiResponse.containsKey("error")) {
                    errorMsg = String.valueOf(aiResponse.get("error"));
                }
                if (aiResponse.containsKey("status") && "error".equals(aiResponse.get("status"))) {
                    errorMsg += " AI service status error";
                }
                
                log.error("AI service returned error: {}", errorMsg);
                
                // Use local analysis if AI service fails
                log.info("AI service failed, fallback to local analysis");
                return performLocalIntentAnalysis(originalText, new HashMap<>());
            }
            
            // Try multiple ways to get AI response content
            String responseText = null;
            
            // Way 1: try 'response'
            if (aiResponse.containsKey("response")) {
                responseText = (String) aiResponse.get("response");
                log.info("Got response text from 'response': {}", responseText);
            }
            
            // Way 2: try 'content'
            if ((responseText == null || responseText.trim().isEmpty()) && aiResponse.containsKey("content")) {
                responseText = (String) aiResponse.get("content");
                log.info("Got response text from 'content': {}", responseText);
            }
            
            // Way 3: try 'message'
            if ((responseText == null || responseText.trim().isEmpty()) && aiResponse.containsKey("message")) {
                responseText = (String) aiResponse.get("message");
                log.info("Got response text from 'message': {}", responseText);
            }
            
            // Way 4: if none, log fields and try full response
            if (responseText == null || responseText.trim().isEmpty()) {
                log.warn("AI response text is empty, logging all fields...");
                for (Map.Entry<String, Object> entry : aiResponse.entrySet()) {
                    log.info("Field {}: {}", entry.getKey(), entry.getValue());
                }
                
                // Try whole response
                responseText = aiResponse.toString();
                log.info("Use entire response as text: {}", responseText);
            }
            
            // Check if response indicates error
            if (responseText != null && (responseText.contains("error") || responseText.contains("Error") || 
                responseText.contains("failed") || responseText.contains("Failed") ||
                responseText.contains("technical issues") || responseText.contains("404"))) {
                log.error("AI response indicates error: {}", responseText);
                log.info("AI service error, fallback to local analysis");
                return performLocalIntentAnalysis(originalText, new HashMap<>());
            }
            
            // Try to extract JSON from response
            Map<String, Object> parsedData = extractJSONFromResponse(responseText);
            
            if (parsedData != null && !parsedData.isEmpty()) {
                log.info("AI response parsed, function: {}, confidence: {}", 
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
                log.warn("Failed to parse AI response, try inference...");
                
                // Infer function type
                String inferredFunction = inferFunctionFromText(responseText);
                if (inferredFunction != null) {
                    log.info("Inferred function: {}", inferredFunction);
                    return IntentAnalysisResult.builder()
                        .functionName(inferredFunction)
                        .confidence(0.8)
                        .parameters(new HashMap<>())
                        .reasoning("Inferred based on AI response text")
                        .knowledgeUsed("AI response analysis")
                        .originalText(originalText)
                        .analysisTimestamp(System.currentTimeMillis())
                        .aiModel("gemini")
                        .build();
                }
                
                // If still cannot parse JSON, use default analysis
                log.warn("AI response parse failed, response: {}", responseText);
                return IntentAnalysisResult.builder()
                    .functionName("unknown")
                    .confidence(0.0)
                    .reasoning("Unable to parse AI response: " + responseText)
                    .originalText(originalText)
                    .analysisTimestamp(System.currentTimeMillis())
                    .aiModel("gemini")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            
            return IntentAnalysisResult.builder()
                .functionName("unknown")
                .confidence(0.0)
                .reasoning("Failed to parse AI response: " + e.getMessage())
                .originalText(originalText)
                .analysisTimestamp(System.currentTimeMillis())
                .aiModel("gemini")
                .build();
        }
    }
    
    /**
     * Extract JSON data from AI response
     */
    private Map<String, Object> extractJSONFromResponse(String responseText) {
        try {
            // Simple JSON extraction logic
            if (responseText.contains("{") && responseText.contains("}")) {
                int start = responseText.indexOf("{");
                int end = responseText.lastIndexOf("}") + 1;
                String jsonText = responseText.substring(start, end);
                
                // Should use a real JSON parser; simplified here
                // In real projects, use Jackson or Gson
                return parseSimpleJSON(jsonText);
            }
        } catch (Exception e) {
            log.error("Failed to extract JSON", e);
        }
        return null;
    }
    
    /**
     * Simple JSON parsing - extract key info
     */
    private Map<String, Object> parseSimpleJSON(String jsonText) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Start parsing JSON from AI: {}", jsonText);
            
            // functionName
            if (jsonText.contains("\"functionName\"")) {
                String functionName = extractValue(jsonText, "functionName");
                if (functionName != null && !functionName.isEmpty()) {
                    result.put("functionName", functionName);
                    log.info("Extracted functionName: {}", functionName);
                }
            }
            
            // confidence
            if (jsonText.contains("\"confidence\"")) {
                String confidenceStr = extractValue(jsonText, "confidence");
                if (confidenceStr != null) {
                    try {
                        double confidence = Double.parseDouble(confidenceStr);
                        result.put("confidence", confidence);
                        log.info("Extracted confidence: {}", confidence);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse confidence: {}", confidenceStr);
                        result.put("confidence", 0.8); // default
                    }
                }
            }
            
            // parameters
            if (jsonText.contains("\"parameters\"")) {
                Map<String, Object> params = extractParameters(jsonText);
                if (params != null && !params.isEmpty()) {
                    result.put("parameters", params);
                    log.info("Extracted parameters: {}", params);
                }
            }
            
            // reasoning
            if (jsonText.contains("\"reasoning\"")) {
                String reasoning = extractValue(jsonText, "reasoning");
                if (reasoning != null && !reasoning.isEmpty()) {
                    result.put("reasoning", reasoning);
                    log.info("Extracted reasoning: {}", reasoning);
                }
            }
            
            // knowledgeUsed
            if (jsonText.contains("\"knowledgeUsed\"")) {
                String knowledgeUsed = extractValue(jsonText, "knowledgeUsed");
                if (knowledgeUsed != null && !knowledgeUsed.isEmpty()) {
                    result.put("knowledgeUsed", knowledgeUsed);
                    log.info("Extracted knowledgeUsed: {}", knowledgeUsed);
                }
            }
            
            // If no functionName, try inference
            if (!result.containsKey("functionName")) {
                String inferredFunction = inferFunctionFromText(jsonText);
                if (inferredFunction != null) {
                    result.put("functionName", inferredFunction);
                    log.info("Inferred function: {}", inferredFunction);
                }
            }
            
            // Defaults
            if (!result.containsKey("confidence")) {
                result.put("confidence", 0.8);
            }
            if (!result.containsKey("parameters")) {
                result.put("parameters", new HashMap<>());
            }
            if (!result.containsKey("reasoning")) {
                result.put("reasoning", "Based on AI analysis");
            }
            if (!result.containsKey("knowledgeUsed")) {
                result.put("knowledgeUsed", "AI intent analysis");
            }
            
            log.info("JSON parsing completed, result: {}", result);
            
        } catch (Exception e) {
            log.error("Error during JSON parsing", e);
        }
        
        return result;
    }
    
    /**
     * Extract a field value from JSON-like text
     */
    private String extractValue(String jsonText, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(jsonText);
            
            if (m.find()) {
                return m.group(1);
            }
            
            // Try non-string values (numbers)
            pattern = "\"" + fieldName + "\"\\s*:\\s*([^,\\s}]+)";
            p = java.util.regex.Pattern.compile(pattern);
            m = p.matcher(jsonText);
            
            if (m.find()) {
                return m.group(1);
            }
            
        } catch (Exception e) {
            log.warn("Failed to extract field {}: {}", fieldName, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extract parameters info
     */
    private Map<String, Object> extractParameters(String jsonText) {
        Map<String, Object> params = new HashMap<>();
        
        try {
            // Find parameters block
            int start = jsonText.indexOf("\"parameters\"");
            if (start == -1) return params;
            
            // Find object start
            start = jsonText.indexOf("{", start);
            if (start == -1) return params;
            
            // Find matching closing brace
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
                log.info("Extracted parameters text: {}", paramsText);
                
                // Simple extraction
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
            log.warn("Failed to extract parameters: {}", e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Infer function from text
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
     * Extract recipient (name or email)
     */
    private String extractRecipient(String text) {
        log.info("Start extracting recipient, input: {}", text);
        
        // Try email address first
        String email = extractEmailAddress(text);
        if (email != null && !email.isEmpty()) {
            return email;
        }
        
        // If no email, try name
        // Pattern 1: "给[Name]发送邮件" or "to [Name] send email"
        if (text.contains("给") || text.toLowerCase().contains("to")) {
            int start = -1;
            if (text.contains("给")) {
                start = text.indexOf("给") + 1;
            } else {
                start = text.toLowerCase().indexOf("to") + 2;
            }
            
            if (start < text.length()) {
                String afterGiving = text.substring(start);
                // Find next keyword
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
                        log.info("Pattern1 recipient name: {}", name);
                        return name;
                    }
                }
            }
        }
        
        // Pattern 2: "发送邮件给[Name]"
        if (text.contains("发送邮件给")) {
            int start = text.indexOf("发送邮件给") + 6;
            if (start < text.length()) {
                String afterTo = text.substring(start);
                // Find next keyword
                int end = afterTo.length();
                if (afterTo.contains("，")) {
                    end = afterTo.indexOf("，");
                } else if (afterTo.contains("，")) {
                    end = afterTo.indexOf("，");
                }
                
                if (end < afterTo.length()) {
                    String name = afterTo.substring(0, end).trim();
                    if (!name.isEmpty()) {
                        log.info("Pattern2 recipient name: {}", name);
                        return name;
                    }
                }
            }
        }
        
        log.info("No valid recipient info found");
        return "Unknown recipient";
    }
    
    /**
     * Extract userName from context
     */
    private String extractUserNameFromContext(Map<String, Object> context) {
        if (context == null) {
            return "User";
        }
        
        // Try different fields
        String userName = null;
        
        if (context.containsKey("userName")) {
            userName = (String) context.get("userName");
        } else if (context.containsKey("name")) {
            userName = (String) context.get("name");
        } else if (context.containsKey("displayName")) {
            userName = (String) context.get("displayName");
        } else if (context.containsKey("userId")) {
            // If only userId is present
            String userId = String.valueOf(context.get("userId"));
            userName = "User" + userId;
        }
        
        return userName != null && !userName.trim().isEmpty() ? userName.trim() : "User";
    }
    
    /**
     * Extract userId from context
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
    
    // ==================== Schedule extraction helpers ====================
    
    /**
     * Extract schedule title
     */
    private String extractScheduleTitle(String text) {
        String lowerText = text.toLowerCase();
        
        // Try multiple patterns to extract title
        String title = null;
        
        // Pattern 1: "添加[title]日程" or "add [title] schedule"
        if (lowerText.contains("添加") || lowerText.contains("add")) {
            int start;
            if (lowerText.contains("添加")) {
                start = lowerText.indexOf("添加") + 2;
            } else {
                start = lowerText.indexOf("add") + 3;
            }
            
            if (start < text.length()) {
                // Find next keyword
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
                        log.info("Pattern1 schedule title: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // Pattern 2: "安排[title]" or "schedule [title]"
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
                    log.info("Pattern2 schedule title: {}", title);
                    return title;
                }
            }
        }
        
        log.warn("Failed to extract valid schedule title");
        return null;
    }
    
    /**
     * Extract schedule date
     */
    private String extractScheduleDate(String text) {
        String lowerText = text.toLowerCase();
        
        // Get current date
        java.time.LocalDate today = java.time.LocalDate.now();
        
        // Try to extract relative date
        if (lowerText.contains("明天") || lowerText.contains("tomorrow")) {
            String date = today.plusDays(1).toString();
            log.info("Extracted relative date: tomorrow -> {}", date);
            return date;
        } else if (lowerText.contains("后天") || lowerText.contains("next day")) {
            String date = today.plusDays(2).toString();
            log.info("Extracted relative date: tomorrow -> {}", date);
            return date;
        } else if (lowerText.contains("下周") || lowerText.contains("next week")) {
            // Next week corresponds to 7 days later
            String date = today.plusDays(7).toString();
            log.info("Extracted relative date: next week -> {}", date);
            return date;
        } else if (lowerText.contains("下周") || lowerText.contains("next week")) {
            // Next week corresponds to 7 days later
            String date = today.plusDays(7).toString();
            log.info("Extracted relative date: next week -> {}", date);
            return date;
        }
        
        // If not found, use today
        log.info("No explicit date found, using today: {}", today.toString());
        return today.toString();
    }
    
    /**
     * Extract schedule time
     */
    private String extractScheduleTime(String text) {
        String lowerText = text.toLowerCase();
        
        // Try to extract time
        String time = null;
        
        // Pattern 1: "下午3点" or "3pm"
        if (lowerText.contains("下午") || lowerText.contains("pm")) {
            if (lowerText.contains("下午")) {
                int start = lowerText.indexOf("下午") + 2;
                if (start < text.length()) {
                    String afterAfternoon = text.substring(start);
                    // Extract number
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
                    java.util.regex.Matcher matcher = pattern.matcher(afterAfternoon);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour + 12);
                            log.info("Pattern1 extracted time: afternoon{} -> {}", hour, time);
                            return time;
                        }
                    }
                }
            } else if (lowerText.contains("pm")) {
                int start = lowerText.indexOf("pm") - 1;
                if (start >= 0) {
                    // Find number before pm
                    String beforePm = text.substring(0, start + 1);
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*pm");
                    java.util.regex.Matcher matcher = pattern.matcher(beforePm);
                    if (matcher.find()) {
                        int hour = Integer.parseInt(matcher.group(1));
                        if (hour >= 1 && hour <= 12) {
                            time = String.format("%02d:00", hour + 12);
                            log.info("Pattern1 extracted time: {}pm -> {}", hour, time);
                            return time;
                        }
                    }
                }
            }
        }
        
        // Pattern 2: "早上8点" or "8am"
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
                            log.info("Pattern2 extracted time: morning{} -> {}", hour, time);
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
                            log.info("Pattern2 extracted time: {}am -> {}", hour, time);
                            return time;
                        }
                    }
                }
            }
        }
        
        // If not found, return default time
        log.info("No explicit time found, using default: 09:00");
        return "09:00";
    }
    
    /**
     * Extract schedule category
     */
    private String extractScheduleCategory(String text) {
        String lowerText = text.toLowerCase();
        
        // Determine category by time or keywords
        if (lowerText.contains("早上") || lowerText.contains("am") || lowerText.contains("morning")) {
            return "morning";
        } else if (lowerText.contains("下午") || lowerText.contains("pm") || lowerText.contains("afternoon")) {
            return "afternoon";
        } else if (lowerText.contains("晚上") || lowerText.contains("evening") || lowerText.contains("night")) {
            return "evening";
        } else if (lowerText.contains("吃药") || lowerText.contains("medicine") || lowerText.contains("medication")) {
            return "medication";
        }
        
        // If not found, infer by time
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
        
        // Default category
        log.info("No explicit category found, using default: afternoon");
        return "afternoon";
    }
    
    /**
     * Extract schedule description
     */
    private String extractScheduleDescription(String text) {
        String lowerText = text.toLowerCase();
        
        // Try to extract description
        String description = null;
        
        // Pattern 1: "内容是[描述]" or "content is [描述]"
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
                    log.info("Pattern1 extracted schedule description: {}", description);
                    return description;
                }
            }
        }
        
        // If not found, return null
        log.info("No explicit schedule description");
        return null;
    }
    
    /**
     * Extract schedule priority
     */
    private String extractSchedulePriority(String text) {
        String lowerText = text.toLowerCase();
        
        // Determine priority by keywords
        if (lowerText.contains("重要") || lowerText.contains("紧急") || lowerText.contains("important") || lowerText.contains("urgent")) {
            return "high";
        } else if (lowerText.contains("一般") || lowerText.contains("普通") || lowerText.contains("normal") || lowerText.contains("regular")) {
            return "low";
        }
        
        // Default priority
        log.info("No explicit priority found, using default: medium");
        return "medium";
    }
    
    // ==================== Important date extraction helpers ====================
    
    /**
     * Extract important date title
     */
    private String extractImportantDateTitle(String text) {
        String lowerText = text.toLowerCase();
        
        // Pattern 1: "添加[title]的生日" or "add [title] birthday"
        if (lowerText.contains("的生日") || lowerText.contains(" birthday")) {
            int end;
            if (lowerText.contains("的生日")) {
                end = text.indexOf("的生日");
            } else {
                end = text.indexOf(" birthday");
            }
            
            if (end > 0) {
                // Find title start position
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
                        log.info("Pattern1 extracted important date title: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // Pattern 2: "添加[title]纪念日" or "add [title] anniversary"
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
                        log.info("Pattern2 extracted important date title: {}", title);
                        return title;
                    }
                }
            }
        }
        
        // Pattern 3: extract title directly (no specific keyword)
        String[] keywords = {"添加", "add", "设置", "set"};
        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                int start = text.indexOf(keyword) + keyword.length();
                if (start < text.length()) {
                    String title = text.substring(start).trim();
                    // Remove date/time parts
                    if (title.contains("是") || title.contains("is")) {
                        int dateIndex = title.indexOf("是");
                        if (dateIndex > 0) {
                            title = title.substring(0, dateIndex).trim();
                        }
                    }
                    if (!title.isEmpty()) {
                        log.info("Pattern3 extracted important date title: {}", title);
                        return title;
                    }
                }
            }
        }
        
        log.warn("Failed to extract important date title");
        return null;
    }
    
    /**
     * Extract important date date
     */
    private String extractImportantDateDate(String text) {
        String lowerText = text.toLowerCase();
        
        // Pattern 1: "是[日期]" or "is [date]"
        if (lowerText.contains("是") || lowerText.contains("is")) {
            int start;
            if (lowerText.contains("是")) {
                start = text.indexOf("是") + 1;
            } else {
                start = text.indexOf("is") + 2;
            }
            
            if (start < text.length()) {
                String datePart = text.substring(start).trim();
                
                // Try parse date
                String date = parseDateFromText(datePart);
                if (date != null) {
                    log.info("Pattern1 extracted important date date: {}", date);
                    return date;
                }
            }
        }
        
        // Pattern 2: find date formats directly
        String date = parseDateFromText(text);
        if (date != null) {
            log.info("Pattern2 extracted important date date: {}", date);
            return date;
        }
        
        log.warn("Failed to extract important date date");
        return null;
    }
    
    /**
     * Extract important date type
     */
    private String extractImportantDateType(String text) {
        String lowerText = text.toLowerCase();
        
        // Determine type by keywords
        if (lowerText.contains("生日") || lowerText.contains("birthday")) {
            return "birthday";
        } else if (lowerText.contains("纪念日") || lowerText.contains("anniversary")) {
            return "anniversary";
        } else if (lowerText.contains("节日") || lowerText.contains("holiday")) {
            return "holiday";
        } else if (lowerText.contains("自定义") || lowerText.contains("custom")) {
            return "custom";
        }
        
        // Default type
        log.info("No explicit type found, using default: custom");
        return "custom";
    }
    
    /**
     * Extract important date description
     */
    private String extractImportantDateDescription(String text) {
        String lowerText = text.toLowerCase();
        
        // Try to extract description
        String description = null;
        
        // Pattern 1: "描述是[内容]" or "description is [内容]"
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
                    log.info("Pattern1 extracted important date description: {}", description);
                    return description;
                }
            }
        }
        
        // If not found, return null
        log.info("No explicit important date description");
        return null;
    }
    
    /**
     * Parse date from text
     */
    private String parseDateFromText(String text) {
        try {
            // Trim spaces
            text = text.trim();
            
            // Try various date formats
            if (text.matches("\\d{1,2}月\\d{1,2}日")) {
                // Chinese format: 12月25日
                String[] parts = text.split("[月日]");
                if (parts.length >= 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    // Assume next year
                    int year = LocalDate.now().getYear() + 1;
                    return String.format("%d-%02d-%02d", year, month, day);
                }
            } else if (text.matches("\\d{1,2}/\\d{1,2}")) {
                // English format: 12/25
                String[] parts = text.split("/");
                if (parts.length >= 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = LocalDate.now().getYear() + 1;
                    return String.format("%d-%02d-%02d", year, month, day);
                }
            } else if (text.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                // Standard format: 2025-12-25
                return text;
            }
            
            // Try relative dates
            if (text.contains("明天")) {
                return LocalDate.now().plusDays(1).toString();
            } else if (text.contains("后天")) {
                return LocalDate.now().plusDays(2).toString();
            } else if (text.contains("下周")) {
                return LocalDate.now().plusWeeks(1).toString();
            }
            
        } catch (Exception e) {
            log.error("Failed to parse date: {}", e.getMessage());
        }
        
        return null;
    }
}
