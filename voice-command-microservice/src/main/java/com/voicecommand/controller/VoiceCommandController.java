package com.voicecommand.controller;

import com.voicecommand.service.VoiceCommandService;
import com.voicecommand.service.FunctionKnowledgeService;
import com.voicecommand.model.VoiceCommandResponse;
import com.voicecommand.model.VoiceCommandRequest;
import com.voicecommand.model.CommandExecutionStatus;
import com.voicecommand.model.FunctionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Voice Command Controller
 * 
 * Provides REST API interfaces for voice command processing
 * Supports both voice input and text input methods
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/voice-command")
@CrossOrigin(origins = "*")
@Slf4j
public class VoiceCommandController {
    
    @Autowired
    private VoiceCommandService voiceCommandService;
    
    @Autowired
    private FunctionKnowledgeService functionKnowledgeService;
    
    /**
     * Process voice command
     * 
     * @param audioFile Audio file
     * @param languageCode Language code
     * @param userId User ID
     * @param sessionId Session ID
     * @return Processing result
     */
    @PostMapping("/process")
    public ResponseEntity<VoiceCommandResponse> processVoiceCommand(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(value = "languageCode", defaultValue = "en-US") String languageCode,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        log.info("Received voice command request: languageCode={}, userId={}, sessionId={}", 
                languageCode, userId, sessionId);
        
        try {
            // Set default values
            if (userId == null) userId = "guest";
            if (sessionId == null) sessionId = "session_" + System.currentTimeMillis();
            
            VoiceCommandResponse response = voiceCommandService.processVoiceCommand(
                audioFile, languageCode, userId, sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to process voice command", e);
            
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("Failed to process voice command: " + e.getMessage())
                .timestamp(System.currentTimeMillis())
                .statusCode(500)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Process text command
     * 
     * @param request Request body
     * @return Processing result
     */
    @PostMapping("/text")
    public ResponseEntity<VoiceCommandResponse> processTextCommand(
            @RequestBody VoiceCommandRequest request) {
        
        log.info("Received text command request: text={}, userId={}, sessionId={}", 
                request.getTextCommand(), request.getUserId(), request.getSessionId());
        
        // Check if textCommand is empty, if empty try to get from text field
        String textCommand = request.getTextCommand();
        if (textCommand == null || textCommand.trim().isEmpty()) {
            // Try to get text field from context
            if (request.getContext() != null && request.getContext().containsKey("text")) {
                textCommand = (String) request.getContext().get("text");
                log.info("Got text field from context: {}", textCommand);
            }
        }
        
        if (textCommand == null || textCommand.trim().isEmpty()) {
            log.error("Text command is empty, cannot process");
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("Text command cannot be empty")
                .timestamp(System.currentTimeMillis())
                .statusCode(400)
                .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // Set default values
            if (request.getLanguageCode() == null) request.setLanguageCode("en-US");
            if (request.getUserId() == null) request.setUserId("guest");
            if (request.getSessionId() == null) request.setSessionId("session_" + System.currentTimeMillis());
            
            VoiceCommandResponse response = voiceCommandService.processTextCommand(
                textCommand, request.getLanguageCode(), 
                request.getUserId(), request.getSessionId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to process text command", e);
            
            VoiceCommandResponse errorResponse = VoiceCommandResponse.builder()
                .success(false)
                .errorMessage("Failed to process text command: " + e.getMessage())
                .timestamp(System.currentTimeMillis())
                .statusCode(500)
                .build();
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Get command execution status
     * 
     * @param executionId Execution ID
     * @return Execution status
     */
    @GetMapping("/status/{executionId}")
    public ResponseEntity<CommandExecutionStatus> getExecutionStatus(
            @PathVariable String executionId) {
        
        log.info("Query execution status: executionId={}", executionId);
        
        try {
            CommandExecutionStatus status = voiceCommandService.getExecutionStatus(executionId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Failed to query execution status: executionId={}", executionId, e);
            
            CommandExecutionStatus errorStatus = CommandExecutionStatus.builder()
                .executionId(executionId)
                .status(CommandExecutionStatus.ExecutionStatus.FAILED)
                .errorMessage("Failed to query execution status: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(500).body(errorStatus);
        }
    }
    
    /**
     * Cancel command execution
     * 
     * @param executionId Execution ID
     * @return Cancel result
     */
    @PostMapping("/cancel/{executionId}")
    public ResponseEntity<Map<String, Object>> cancelExecution(
            @PathVariable String executionId) {
        
        log.info("Cancel execution: executionId={}", executionId);
        
        try {
            boolean cancelled = voiceCommandService.cancelExecution(executionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", cancelled);
            response.put("message", cancelled ? "Execution cancelled" : "Cannot cancel execution");
            response.put("executionId", executionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to cancel execution: executionId={}", executionId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to cancel execution: " + e.getMessage());
            errorResponse.put("executionId", executionId);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Get function knowledge base
     * 
     * @return Function knowledge base information
     */
    @GetMapping("/knowledge-base")
    public ResponseEntity<Map<String, Object>> getKnowledgeBase() {
        log.info("Get function knowledge base");
        
        try {
            if (functionKnowledgeService.isKnowledgeBaseLoaded()) {
                List<FunctionInfo> functions = functionKnowledgeService.getAllFunctions();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("totalFunctions", functions.size());
                response.put("functions", functions);
                response.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("errorMessage", "Function knowledge base not loaded");
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.status(500).body(errorResponse);
            }
            
        } catch (Exception e) {
            log.error("Failed to get function knowledge base", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("errorMessage", "Failed to get function knowledge base: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Health check
     * 
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Voice Command Microservice");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get service information
     * 
     * @return Service information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "Voice Command Microservice");
        info.put("description", "AI voice command processing microservice");
        info.put("version", "1.0.0");
        info.put("features", Arrays.asList(
            "Speech to text",
            "AI intent analysis", 
            "Automatic function execution",
            "Email sending",
            "Status tracking"
        ));
        info.put("supportedLanguages", Arrays.asList("en-US", "zh-CN"));
        info.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(info);
    }
}