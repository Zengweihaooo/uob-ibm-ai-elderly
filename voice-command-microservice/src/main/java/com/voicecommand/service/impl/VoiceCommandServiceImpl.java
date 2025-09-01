package com.voicecommand.service.impl;

import com.voicecommand.service.VoiceCommandService;
import com.voicecommand.service.AIIntentAnalysisService;
import com.voicecommand.service.FunctionRouterService;
import com.voicecommand.model.*;
import com.voicecommand.client.AIServiceClient;
import com.voicecommand.client.MainProjectAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Voice Command Service Implementation
 * 
 * Integrates speech recognition, AI intent analysis, function execution, etc.
 * Provides a complete voice command processing flow
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Service
@Slf4j
public class VoiceCommandServiceImpl implements VoiceCommandService {
    
    private static final Logger log = LoggerFactory.getLogger(VoiceCommandServiceImpl.class);
    
    @Autowired
    private AIServiceClient aiServiceClient;
    
    @Autowired
    private AIIntentAnalysisService aiIntentAnalysisService;
    
    @Autowired
    private FunctionRouterService functionRouterService;
    
    @Autowired
    private MainProjectAIClient mainProjectAIClient;
    
    // Store execution status
    private final Map<String, CommandExecutionStatus> executionStatusMap = new HashMap<>();
    
    @Override
    public VoiceCommandResponse processVoiceCommand(MultipartFile audioFile, String languageCode, 
                                                   String userId, String sessionId) {
        long startTime = System.currentTimeMillis();
        String executionId = generateExecutionId();
        
        log.info("Start processing voice command: executionId={}, userId={}, languageCode={}", 
                executionId, userId, languageCode);
        
        try {
            // 1. Speech to text
            String transcribedText = convertSpeechToText(audioFile, languageCode);
            if (transcribedText == null || transcribedText.trim().isEmpty()) {
                return buildErrorResponse(executionId, "Speech recognition failed, please try again", startTime);
            }
            
            // 2. Intent pre-judgment and dual-path handling
            VoiceCommandResponse response = processWithIntentPrejudgment(transcribedText, languageCode, 
                                                                        userId, sessionId, executionId, startTime);
            
            log.info("Voice command processed: executionId={}, success={}, time={}ms", 
                    executionId, response.isSuccess(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to process voice command: executionId={}", executionId, e);
            return buildErrorResponse(executionId, "Failed to process voice command: " + e.getMessage(), startTime);
        }
    }
    
    @Override
    public VoiceCommandResponse processTextCommand(String textCommand, String languageCode, 
                                                  String userId, String sessionId) {
        long startTime = System.currentTimeMillis();
        String executionId = generateExecutionId();
        
        log.info("Start processing text command: executionId={}, userId={}, text={}", 
                executionId, userId, textCommand);
        
        try {
            // Intent pre-judgment and dual-path handling
            VoiceCommandResponse response = processWithIntentPrejudgment(textCommand, languageCode, 
                                                                        userId, sessionId, executionId, startTime);
            
            log.info("Text command processed: executionId={}, success={}, time={}ms", 
                    executionId, response.isSuccess(), response.getProcessingTime());
            
            return response;
            
        } catch (Exception e) {
            log.error("Failed to process text command: executionId={}", executionId, e);
            return buildErrorResponse(executionId, "Failed to process text command: " + e.getMessage(), startTime);
        }
    }
    
    @Override
    public CommandExecutionStatus getExecutionStatus(String executionId) {
        return executionStatusMap.getOrDefault(executionId, 
            CommandExecutionStatus.builder()
                .executionId(executionId)
                .status(CommandExecutionStatus.ExecutionStatus.FAILED)
                .errorMessage("Execution ID does not exist")
                .build());
    }
    
    @Override
    public boolean cancelExecution(String executionId) {
        CommandExecutionStatus status = executionStatusMap.get(executionId);
        if (status != null && status.getStatus() == CommandExecutionStatus.ExecutionStatus.RUNNING) {
            status.setStatus(CommandExecutionStatus.ExecutionStatus.CANCELLED);
            status.setCompletionTime(System.currentTimeMillis());
            executionStatusMap.put(executionId, status);
            log.info("Cancel execution: executionId={}", executionId);
            return true;
        }
        return false;
    }
    
    /**
     * Speech to text
     */
    private String convertSpeechToText(MultipartFile audioFile, String languageCode) {
        try {
            // Call main project's speech-to-text service
            Map<String, Object> request = new HashMap<>();
            request.put("audio", audioFile);
            request.put("languageCode", languageCode);
            
            Map<String, Object> response = aiServiceClient.speechToText(
                audioFile.getBytes().toString(), languageCode);
            
            if (response != null && (Boolean) response.get("success")) {
                return (String) response.get("text");
            } else {
                log.error("Speech recognition failed: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.error("Speech to text failed", e);
            return null;
        }
    }
    
    /**
     * Text to speech
     */
    private String convertTextToSpeech(String text, String languageCode) {
        try {
            // Call main project's text-to-speech service
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            request.put("languageCode", languageCode);
            
            Map<String, Object> response = aiServiceClient.textToSpeech(request);
            
            if (response != null && (Boolean) response.get("success")) {
                return (String) response.get("audio");
            } else {
                log.warn("Text to speech failed: {}", response);
                return null;
            }
            
        } catch (Exception e) {
            log.warn("Text to speech failed", e);
            return null;
        }
    }
    
    /**
     * Build context info
     */
    private Map<String, Object> buildContext(String userId, String sessionId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("sessionId", sessionId);
        context.put("timestamp", System.currentTimeMillis());
        context.put("source", "voice-command-microservice");
        return context;
    }
    
    /**
     * Generate feedback text
     */
    private String generateFeedbackText(IntentAnalysisResult intent, FunctionExecutionResult execution) {
        if (execution.isSuccess()) {
            return execution.getFeedbackText();
        } else {
            return String.format("Sorry, %s failed: %s", 
                intent.getFunctionName(), execution.getErrorMessage());
        }
    }
    
    /**
     * Generate execution ID
     */
    private String generateExecutionId() {
        return "exec_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Build error response
     */
    private VoiceCommandResponse buildErrorResponse(String executionId, String errorMessage, long startTime) {
        return VoiceCommandResponse.builder()
            .executionId(executionId)
            .success(false)
            .errorMessage(errorMessage)
            .timestamp(System.currentTimeMillis())
            .processingTime(System.currentTimeMillis() - startTime)
            .statusCode(500)
            .build();
    }
    
    /**
     * Update execution status
     */
    private void updateExecutionStatus(String executionId, FunctionExecutionResult executionResult) {
        CommandExecutionStatus status = CommandExecutionStatus.builder()
            .executionId(executionId)
            .status(convertStatus(executionResult.getStatus()))
            .progress(executionResult.isSuccess() ? 100 : 0)
            .currentStep(executionResult.getFunctionName())
            .totalSteps(1)
            .startTime(executionResult.getStartTime())
            .completionTime(executionResult.getEndTime())
            .errorMessage(executionResult.getErrorMessage())
            .build();
        
        executionStatusMap.put(executionId, status);
    }
    
    /**
     * Convert execution status
     */
    private CommandExecutionStatus.ExecutionStatus convertStatus(FunctionExecutionResult.ExecutionStatus status) {
        switch (status) {
            case COMPLETED:
                return CommandExecutionStatus.ExecutionStatus.COMPLETED;
            case FAILED:
                return CommandExecutionStatus.ExecutionStatus.FAILED;
            case TIMEOUT:
                return CommandExecutionStatus.ExecutionStatus.TIMEOUT;
            case CANCELLED:
                return CommandExecutionStatus.ExecutionStatus.CANCELLED;
            default:
                return CommandExecutionStatus.ExecutionStatus.RUNNING;
        }
    }

    private VoiceCommandResponse processWithIntentPrejudgment(String userText, String languageCode, 
                                                            String userId, String sessionId, 
                                                            String executionId, long startTime) {
        // Intent pre-judgment
        if (isFunctionCallIntent(userText)) {
            log.info("Function-call intent detected, using function-call path");
            return processAsFunctionCall(userText, languageCode, userId, sessionId, executionId, startTime);
        } else {
            log.info("Normal chat intent detected, using AI chat path");
            return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
        }
    }
    
    private boolean isFunctionCallIntent(String userText) {
        String lowerText = userText.toLowerCase();
        
        // Function-call keywords
        List<String> functionKeywords = Arrays.asList(
            "send email", "email", "mail",
            "schedule", "calendar", "add", "meeting", "appointment", "event", "booking", "reservation",
            "health check", "health", "medical", "doctor", "hospital",
            "contact", "find", "search", "locate", "call",
            "important date", "birthday", "anniversary", "reminder date",
            "reminder", "set", "create", "make", "plan", "organize",
            "tomorrow", "today", "next week", "this week", "next month",
            "pm", "am", "morning", "afternoon", "evening", "night", "noon", "midnight",
            "o'clock", "hour", "minute", "time", "when"
        );
        
        // If contains any keyword, treat as function call
        boolean isFunctionCall = functionKeywords.stream().anyMatch(lowerText::contains);
        log.info("Intent pre-judgment: text={}, isFunctionCall={}", userText, isFunctionCall);
        
        return isFunctionCall;
    }
    
    private VoiceCommandResponse processAsFunctionCall(String userText, String languageCode, 
                                                     String userId, String sessionId, 
                                                     String executionId, long startTime) {
        try {
            // 1. AI intent analysis
            Map<String, Object> context = buildContext(userId, sessionId);
            IntentAnalysisResult intentResult = aiIntentAnalysisService.analyzeIntent(userText, context);
            
            // 2. Check confidence; if too low, fallback to normal chat
            if (intentResult.getConfidence() < 0.7) {
                log.info("Function-call confidence too low ({}), fallback to normal chat", intentResult.getConfidence());
                return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
            }
            
            // 3. Execute function
            FunctionExecutionResult executionResult = functionRouterService.executeFunction(intentResult);
            
            // 4. Generate feedback text
            String feedbackText = generateFeedbackText(intentResult, executionResult);
            
            // 5. Text to speech (optional)
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 6. Build response
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(userText)
                .intent(intentResult)
                .execution(executionResult)
                .feedbackText(feedbackText)
                .audioResponse(audioResponse)
                .success(executionResult.isSuccess())
                .errorMessage(executionResult.getErrorMessage())
                .timestamp(System.currentTimeMillis())
                .processingTime(System.currentTimeMillis() - startTime)
                .statusCode(executionResult.isSuccess() ? 200 : 500)
                .build();
            
            // 7. Update execution status
            updateExecutionStatus(executionId, executionResult);
            
            return response;
            
        } catch (Exception e) {
            log.error("Function-call handling failed, fallback to normal chat", e);
            return processAsNormalChat(userText, languageCode, userId, sessionId, executionId, startTime);
        }
    }
    
    private VoiceCommandResponse processAsNormalChat(String userText, String languageCode, 
                                                   String userId, String sessionId, 
                                                   String executionId, long startTime) {
        try {
            // 1. Call main project's AI chat service
            Map<String, Object> request = new HashMap<>();
            request.put("message", userText);
            
            Map<String, Object> aiResponse = mainProjectAIClient.chatWithGemini(request);
            
            String feedbackText = (String) aiResponse.get("response");
            if (feedbackText == null) {
                feedbackText = "Sorry, I cannot answer your question right now. Please try again later.";
            }
            
            // 2. Text to speech (optional)
            String audioResponse = convertTextToSpeech(feedbackText, languageCode);
            
            // 3. Build response
            VoiceCommandResponse response = VoiceCommandResponse.builder()
                .executionId(executionId)
                .transcribedText(userText)
                .feedbackText(feedbackText)
                .audioResponse(audioResponse)
                .success(true)
                .timestamp(System.currentTimeMillis())
                .processingTime(System.currentTimeMillis() - startTime)
                .statusCode(200)
                .build();
            
            return response;
            
        } catch (Exception e) {
            log.error("Normal chat handling failed", e);
            return buildErrorResponse(executionId, "AI chat service is temporarily unavailable, please try again later", startTime);
        }
    }
}
