package com.voicecommand.service;

import com.voicecommand.model.IntentAnalysisResult;
import com.voicecommand.model.FunctionInfo;
import com.voicecommand.client.AIServiceClient;
import com.voicecommand.service.UnifiedAIServiceClient;
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
    private UnifiedAIServiceClient unifiedAIServiceClient;
    
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
                Map<String, Object> aiResponse = unifiedAIServiceClient.chatWithAI(aiRequest);
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
        
        // Email sending intent detection - support English keywords
        if (lowerText.contains("email") || 
            lowerText.contains("send") ||
            lowerText.contains("mail") || lowerText.contains("message") ||
            (lowerText.contains("to") && (lowerText.contains("send") || lowerText.contains("mail"))) ||
            lowerText.contains("send email")) {
             
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
        
        // Important date management intent detection - supports English keywords (higher priority)
        if (lowerText.contains("important date") || 
            lowerText.contains("birthday") ||
            lowerText.contains("anniversary") ||
            lowerText.contains("holiday") ||
            (lowerText.contains("add") || lowerText.contains("set")) &&
            (lowerText.contains("birthday") || lowerText.contains("anniversary"))) {
             
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
        
        // Schedule management intent detection - supports English keywords
        if (lowerText.contains("schedule") || 
            lowerText.contains("add") ||
            lowerText.contains("set") ||
            lowerText.contains("reminder") ||
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
        
        // Pattern 1: "content is [content]"
        if (lowerText.contains("content is")) {
            int start = text.indexOf("content is") + 12;
            
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
        
        // Pattern 2: "send [content]"
        if (lowerText.contains("send")) {
            int start = lowerText.indexOf("send") + 4;
            
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
        
        // Pattern 3: "say [content]"
        if (lowerText.contains("say")) {
            int start = lowerText.indexOf("say") + 3;
            
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
                // Remove keywords like "send"
                content = content.replaceAll("send", "").replaceAll("email", "").trim();
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
        
        // Pattern 1: "subject is [subject]"
        if (lowerText.contains("subject is")) {
            int start = text.indexOf("subject is") + 12;
            
            if (start < text.length()) {
                // Find next keyword
                int end = text.length();
                if (lowerText.contains("content is")) {
                    end = text.indexOf("content is");
                } else if (lowerText.contains("content")) {
                    end = text.indexOf("content");
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
        content = content.replaceAll("email", "");
        content = content.replaceAll("send", "");
        content = content.replaceAll("mail", "");
        content = content.replaceAll("to", "");
        
        // Do not remove "subject is" and "content is"
        // content = content.replaceAll("subject is", "");
        // content = content.replaceAll("content is", "");
        
        // Smartly remove "to" but keep meaningful usage
        // e.g., "I'm almost there" -> keep; "to email" -> remove
        if (content.contains("to") && !content.matches(".*[I you he she it].*to.*")) {
            content = content.replaceAll("to", "");
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
            log.info("Extracting JSON from response: {}", responseText);
            
            // Remove markdown code block markers if present
            if (responseText.contains("```json")) {
                responseText = responseText.replaceAll("```json\\s*", "");
                responseText = responseText.replaceAll("```\\s*$", "");
                log.info("Removed markdown markers, cleaned text: {}", responseText);
            } else if (responseText.contains("```")) {
                responseText = responseText.replaceAll("```\\s*", "");
                log.info("Removed generic markdown markers, cleaned text: {}", responseText);
            }
            
            // Simple JSON extraction logic
            if (responseText.contains("{") && responseText.contains("}")) {
                int start = responseText.indexOf("{");
                int end = responseText.lastIndexOf("}") + 1;
                String jsonText = responseText.substring(start, end);
                
                log.info("Extracted JSON text: {}", jsonText);
                
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
        
        if (lowerText.contains("send_email") || 
            lowerText.contains("email") || lowerText.contains("send")) {
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
        // Pattern 1: "to [Name] send email"
        if (text.toLowerCase().contains("to")) {
            int start = text.toLowerCase().indexOf("to") + 2;
            
            if (start < text.length()) {
                String afterGiving = text.substring(start);
                // Find next keyword
                int end = afterGiving.length();
                if (afterGiving.toLowerCase().contains("send")) {
                    end = afterGiving.toLowerCase().indexOf("send");
                } else if (afterGiving.toLowerCase().contains("email")) {
                    end = afterGiving.toLowerCase().indexOf("email");
                } else if (afterGiving.toLowerCase().contains("mail")) {
                    end = afterGiving.toLowerCase().indexOf("mail");
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
        
        // Pattern 2: "send email to [Name]"
        if (text.toLowerCase().contains("send email to")) {
            int start = text.toLowerCase().indexOf("send email to") + 13;
            if (start < text.length()) {
                String afterTo = text.substring(start);
                // Find next keyword
                int end = afterTo.length();
                if (afterTo.contains(",")) {
                    end = afterTo.indexOf(",");
                } else if (afterTo.contains(".")) {
                    end = afterTo.indexOf(".");
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
        
        // Pattern 1: "add [title] schedule"
        if (lowerText.contains("add")) {
            int start = lowerText.indexOf("add") + 3;
            
            if (start < text.length()) {
                // Find next keyword
                int end = text.length();
                if (lowerText.contains("schedule")) {
                    end = lowerText.indexOf("schedule");
                } else if (lowerText.contains("arrange")) {
                    end = lowerText.indexOf("arrange");
                } else if (lowerText.contains("tomorrow")) {
                    end = lowerText.indexOf("tomorrow");
                } else if (lowerText.contains("next")) {
                    end = lowerText.indexOf("next");
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
        
        // Pattern 2: "schedule [title]"
        if (lowerText.contains("schedule")) {
            int start = lowerText.indexOf("schedule") + 8;
            
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
        if (lowerText.contains("tomorrow")) {
            String date = today.plusDays(1).toString();
            log.info("Extracted relative date: tomorrow -> {}", date);
            return date;
        } else if (lowerText.contains("next day")) {
            String date = today.plusDays(2).toString();
            log.info("Extracted relative date: next day -> {}", date);
            return date;
        } else if (lowerText.contains("next week")) {
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
        log.info("Extracting time from text: '{}'", text);
        log.info("Lowercase text: '{}'", lowerText);
        
        // Try to extract time
        String time = null;
        
        // Pattern 1: "3pm"
        if (lowerText.contains("pm")) {
            // Enhanced PM pattern matching for "3pm", "3:30pm", etc.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2}):?(\\d{0,2})\\s*pm");
            java.util.regex.Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = 0;
                if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                    minute = Integer.parseInt(matcher.group(2));
                }
                if (hour >= 1 && hour <= 12) {
                    time = String.format("%02d:%02d", hour + 12, minute);
                    log.info("Pattern1 extracted time: {}:{}pm -> {}", hour, minute, time);
                    return time;
                }
            }
        }
        
        // Pattern 2: "8am"
        if (lowerText.contains("am")) {
            // Enhanced AM pattern matching for "8am", "8:30am", etc.
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{1,2}):?(\\d{0,2})\\s*am");
            java.util.regex.Matcher matcher = pattern.matcher(lowerText);
            if (matcher.find()) {
                int hour = Integer.parseInt(matcher.group(1));
                int minute = 0;
                if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                    minute = Integer.parseInt(matcher.group(2));
                }
                if (hour >= 1 && hour <= 12) {
                    time = String.format("%02d:%02d", hour, minute);
                    log.info("Pattern2 extracted time: {}:{}am -> {}", hour, minute, time);
                    return time;
                }
            }
        }
        
        // Pattern 3: "3 o'clock" or "3 o clock" or "3 o'clock in the afternoon"
        java.util.regex.Pattern patternOClock = java.util.regex.Pattern.compile("(\\d{1,2})\\s*o['\\s]*clock");
        java.util.regex.Matcher matcherOClock = patternOClock.matcher(lowerText);
        if (matcherOClock.find()) {
            int hour = Integer.parseInt(matcherOClock.group(1));
            if (hour >= 1 && hour <= 12) {
                // Check context for time of day
                if (lowerText.contains("afternoon") || lowerText.contains("evening") || lowerText.contains("night")) {
                    time = String.format("%02d:00", hour + 12);
                    log.info("Pattern3 extracted time: {} o'clock (afternoon/evening) -> {}", hour, time);
                    return time;
                } else if (lowerText.contains("morning")) {
                    time = String.format("%02d:00", hour);
                    log.info("Pattern3 extracted time: {} o'clock (morning) -> {}", hour, time);
                    return time;
                } else {
                    // Default to morning for 1-12 without context
                    time = String.format("%02d:00", hour);
                    log.info("Pattern3 extracted time: {} o'clock (default morning) -> {}", hour, time);
                    return time;
                }
            } else if (hour >= 13 && hour <= 23) {
                // 24-hour format
                time = String.format("%02d:00", hour);
                log.info("Pattern3 extracted time: {} o'clock (24-hour) -> {}", hour, time);
                return time;
            }
        }
        
        // Pattern 4: "at 3pm" or "at 3:00pm" (with "at" prefix)
        java.util.regex.Pattern patternAt = java.util.regex.Pattern.compile("at\\s+(\\d{1,2}):?(\\d{0,2})\\s*(am|pm)");
        java.util.regex.Matcher matcherAt = patternAt.matcher(lowerText);
        if (matcherAt.find()) {
            int hour = Integer.parseInt(matcherAt.group(1));
            int minute = 0;
            if (matcherAt.group(2) != null && !matcherAt.group(2).isEmpty()) {
                minute = Integer.parseInt(matcherAt.group(2));
            }
            String period = matcherAt.group(3);
            
            if (period.equals("pm") && hour >= 1 && hour <= 12) {
                time = String.format("%02d:%02d", hour + 12, minute);
                log.info("Pattern4 extracted time: at {}:{}pm -> {}", hour, minute, time);
                return time;
            } else if (period.equals("am") && hour >= 1 && hour <= 12) {
                time = String.format("%02d:%02d", hour, minute);
                log.info("Pattern4 extracted time: at {}:{}am -> {}", hour, minute, time);
                return time;
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
        if (lowerText.contains("am") || lowerText.contains("morning")) {
            return "morning";
        } else if (lowerText.contains("pm") || lowerText.contains("afternoon")) {
            return "afternoon";
        } else if (lowerText.contains("evening") || lowerText.contains("night")) {
            return "evening";
        } else if (lowerText.contains("medicine") || lowerText.contains("medication")) {
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
        
        // Pattern 1: "content is [description]"
        if (lowerText.contains("content is")) {
            int start = text.indexOf("content is") + 12;
            
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
        if (lowerText.contains("important") || lowerText.contains("urgent")) {
            return "high";
        } else if (lowerText.contains("normal") || lowerText.contains("regular")) {
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
        
        // Pattern 1: "add [title] birthday"
        if (lowerText.contains(" birthday")) {
            int end = text.indexOf(" birthday");
            
            if (end > 0) {
                // Find title start position
                int start = 0;
                if (lowerText.contains("add")) {
                    start = text.indexOf("add") + 3;
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
        
        // Pattern 2: "add [title] anniversary"
        if (lowerText.contains(" anniversary")) {
            int end = text.indexOf(" anniversary");
            
            if (end > 0) {
                int start = 0;
                if (lowerText.contains("add")) {
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
        String[] keywords = {"add", "set"};
        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                int start = text.indexOf(keyword) + keyword.length();
                if (start < text.length()) {
                    String title = text.substring(start).trim();
                    // Remove date/time parts
                    if (title.contains("is")) {
                        int dateIndex = title.indexOf("is");
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
        
        // Pattern 1: "is [date]"
        if (lowerText.contains("is")) {
            int start = text.indexOf("is") + 2;
            
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
        if (lowerText.contains("birthday")) {
            return "birthday";
        } else if (lowerText.contains("anniversary")) {
            return "anniversary";
        } else if (lowerText.contains("holiday")) {
            return "holiday";
        } else if (lowerText.contains("custom")) {
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
        
        // Pattern 1: "description is [content]"
        if (lowerText.contains("description is")) {
            int start = text.indexOf("description is") + 15;
            
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
            if (text.matches("\\d{1,2}/\\d{1,2}")) {
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
            if (text.contains("tomorrow")) {
                return LocalDate.now().plusDays(1).toString();
            } else if (text.contains("next day")) {
                return LocalDate.now().plusDays(2).toString();
            } else if (text.contains("next week")) {
                return LocalDate.now().plusWeeks(1).toString();
            }
            
        } catch (Exception e) {
            log.error("Failed to parse date: {}", e.getMessage());
        }
        
        return null;
    }
}
