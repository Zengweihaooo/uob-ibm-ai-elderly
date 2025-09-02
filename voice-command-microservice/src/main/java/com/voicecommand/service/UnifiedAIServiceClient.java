package com.voicecommand.service;

import com.voicecommand.model.IntentAnalysisResult;
import com.voicecommand.model.FunctionInfo;
import com.voicecommand.client.AIServiceClient;
import com.voicecommand.client.MainProjectAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.time.LocalDate;

/**
 * Unified AI Service Client
 * 
 * Provides intelligent failover mechanism:
 * 1. Priority: Call microservice's own AI service
 * 2. If microservice AI is unavailable, fallback to main project AI
 * 3. If main project AI is also unavailable, use local fallback
 *
 * @author AI Assistant
 * @version 2.0.0
 */
@Service
@Slf4j
public class UnifiedAIServiceClient {

    @Autowired
    private AIServiceClient aiServiceClient;

    @Autowired
    private MainProjectAIClient mainProjectAIClient;

    private boolean microserviceAIMode = true; // Default enable microservice's own AI (Gemini)

    /**
     * Unified AI chat interface
     */
    public Map<String, Object> chatWithAI(Map<String, Object> request) {
        log.info("Unified AI chat request: {}", request);

        // Strategy 1: Try to use microservice's own AI (if enabled)
        if (microserviceAIMode) {
            try {
                log.info("Trying microservice AI first...");
                Map<String, Object> response = aiServiceClient.chatWithGemini(request);
                if (isValidResponse(response)) {
                    log.info("Microservice AI call successful");
                    return response;
                } else {
                    log.warn("Microservice AI returned invalid response: {}", response);
                }
            } catch (Exception e) {
                log.warn("Microservice AI call failed: {}", e.getMessage());
            }
        }

        // Strategy 2: Fallback to main project AI
        try {
            log.info("Fallback to main project AI...");
            Map<String, Object> response = mainProjectAIClient.chatWithGemini(request);
            if (isValidResponse(response)) {
                log.info("Main project AI call successful");
                return response;
            } else {
                log.warn("Main project AI returned invalid response: {}", response);
            }
        } catch (Exception e) {
            log.error("Main project AI call failed: {}", e.getMessage());
        }

        // Strategy 3: Final fallback - return local preset response
        log.warn("All AI services failed, using local fallback");
        return generateLocalFallbackResponse(request);
    }

    /**
     * Check if main project AI service is available
     */
    public boolean isMainProjectAIAvailable() {
        return mainProjectAIClient.isMainProjectAIAvailable();
    }

    /**
     * Check if microservice AI service is available
     */
    public boolean isMicroserviceAIAvailable() {
        if (!microserviceAIMode) {
            return false;
        }

        try {
            Map<String, Object> statusResponse = aiServiceClient.getGeminiStatus();
            String status = (String) statusResponse.get("status");
            return "ready".equals(status);
        } catch (Exception e) {
            log.warn("Microservice AI status check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Enable/disable microservice AI mode
     */
    public void setMicroserviceAIMode(boolean enabled) {
        this.microserviceAIMode = enabled;
        log.info("Microservice AI mode set to: {}", enabled);
    }

    /**
     * Get current AI service status
     */
    public Map<String, Object> getAIServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("microserviceAIEnabled", microserviceAIMode);
        status.put("microserviceAIAvailable", isMicroserviceAIAvailable());
        status.put("mainProjectAIAvailable", isMainProjectAIAvailable());
        status.put("timestamp", System.currentTimeMillis());

        // Determine current AI service being used
        if (microserviceAIMode && isMicroserviceAIAvailable()) {
            status.put("activeService", "microservice");
        } else if (isMainProjectAIAvailable()) {
            status.put("activeService", "main-project");
        } else {
            status.put("activeService", "fallback");
        }

        return status;
    }

    /**
     * Validate if AI response is valid
     */
    private boolean isValidResponse(Map<String, Object> response) {
        if (response == null) {
            return false;
        }

        // Check for errors
        if (response.containsKey("error") || response.containsKey("status")) {
            String status = (String) response.get("status");
            if ("error".equals(status)) {
                return false;
            }
        }

        // Check for valid content
        return response.containsKey("response") || response.containsKey("content");
    }

    /**
     * Generate local fallback response
     */
    private Map<String, Object> generateLocalFallbackResponse(Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "fallback");
        response.put("model", "local-fallback");
        response.put("timestamp", System.currentTimeMillis());

        String userMessage = (String) request.get("message");
        if (userMessage == null) {
            userMessage = "";
        }

        String fallbackMessage = generateFallbackMessage(userMessage);
        response.put("response", fallbackMessage);

        return response;
    }

    /**
     * Generate basic fallback message
     */
    private String generateFallbackMessage(String userMessage) {
        String message = userMessage.toLowerCase().trim();

                // Greeting
        if (message.contains("hello") || message.contains("hi")) {
            return "Hello! I'm your AI assistant. Although I'm having some connection issues, I'll do my best to help you. What can I help you with?";
        }
        
        // Health related
        if (message.contains("health") || message.contains("feel") || message.contains("sick")) {
            return "I understand you're asking about health-related questions. Although I can't provide professional medical advice, I recommend consulting with healthcare professionals. If needed, I can help you schedule medical appointments.";
        }
        
        // Emotional support
        if (message.contains("lonely") || message.contains("sad") || message.contains("alone")) {
            return "I understand how you're feeling. Remember, you're not alone. I'll always be here to accompany you. If you want to talk about what's on your mind, I'm happy to listen.";
        }
        
        // Default response
        return "I received your message. Although I'm experiencing some technical issues, I'll do my best to help you. Please try again later, or tell me specifically what you need help with.";
    }
}
