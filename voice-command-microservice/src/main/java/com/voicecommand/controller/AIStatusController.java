package com.voicecommand.controller;

import com.voicecommand.service.UnifiedAIServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * AI Service Status Controller
 * 
 * Provides AI service status checking and configuration management functionality
 *
 * @author AI Assistant
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
@Slf4j
public class AIStatusController {

    @Autowired
    private UnifiedAIServiceClient unifiedAIServiceClient;

    /**
     * Get AI service status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        try {
            Map<String, Object> status = unifiedAIServiceClient.getAIServiceStatus();
            log.info("AI status requested: {}", status);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get AI status", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get AI status");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Switch microservice AI mode
     */
    @PostMapping("/mode")
    public ResponseEntity<Map<String, Object>> setAIMode(@RequestBody Map<String, Object> request) {
        try {
            Boolean microserviceMode = (Boolean) request.get("microserviceMode");
            if (microserviceMode == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameter: microserviceMode");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            unifiedAIServiceClient.setMicroserviceAIMode(microserviceMode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "AI mode updated successfully");
            response.put("microserviceMode", microserviceMode);
            response.put("timestamp", System.currentTimeMillis());

            log.info("AI mode changed to: {}", microserviceMode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to set AI mode", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to set AI mode");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Test AI service connection
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testAIConnection(@RequestBody Map<String, Object> request) {
        try {
            String testMessage = (String) request.getOrDefault("message", "Hello, this is a test message");

            Map<String, Object> testRequest = new HashMap<>();
            testRequest.put("message", testMessage);

            long startTime = System.currentTimeMillis();
            Map<String, Object> response = unifiedAIServiceClient.chatWithAI(testRequest);
            long endTime = System.currentTimeMillis();

            Map<String, Object> testResult = new HashMap<>();
            testResult.put("success", true);
            testResult.put("testMessage", testMessage);
            testResult.put("response", response);
            testResult.put("responseTime", endTime - startTime);
            testResult.put("timestamp", System.currentTimeMillis());

            log.info("AI connection test completed in {}ms", endTime - startTime);
            return ResponseEntity.ok(testResult);

        } catch (Exception e) {
            log.error("AI connection test failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "AI connection test failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get AI service configuration information
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAIConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("microserviceAIMode", true); // Default enable microservice AI (Gemini)
        config.put("mainProjectUrl", "http://localhost:8080");
        config.put("fallbackEnabled", true);
        config.put("maxRetries", 2);
        config.put("timeout", 30000);
        config.put("aiService", "gemini-pro"); // Current AI service
        config.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(config);
    }
}
