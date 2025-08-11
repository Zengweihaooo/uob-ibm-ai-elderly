package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini-new")
@CrossOrigin(origins = "*")
public class GeminiNewController {

    // Google Cloud project and location settings
    private static final String PROJECT_ID = "organic-totem-467918-a5";
    private static final String LOCATION = "global";
    private static final String MODEL_NAME = "gemini-1.5-flash";

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGeminiStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            GoogleCredentials credentials = getGoogleCredentials();
            status.put("status", "ready");
            status.put("model", MODEL_NAME);
            status.put("project", PROJECT_ID);
            status.put("location", LOCATION);
            status.put("credentialsLoaded", true);
            status.put("sdk", "google-genai");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("status", "error");
            status.put("error", e.getMessage());
            status.put("credentialsLoaded", false);
            return ResponseEntity.ok(status);
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chatWithGemini(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String userMessage = (String) request.get("message");
        
        try {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("error", "Message cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Use new GenAI REST API approach
            String geminiResponse = callGeminiGenAIRestAPI(userMessage);
            
            response.put("response", geminiResponse);
            response.put("status", "success");
            response.put("model", MODEL_NAME);
            response.put("sdk", "google-genai");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to get response from Gemini: " + e.getMessage());
            response.put("status", "error");
            
            // Enhanced fallback response with basic AI-like responses
            String fallbackResponse = generateFallbackResponse(userMessage);
            response.put("response", fallbackResponse);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/pet-chat")
    public ResponseEntity<Map<String, Object>> chatWithPet(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String userMessage = (String) request.get("message");
        String petName = (String) request.getOrDefault("petName", "Buddy");
        String elderlyName = (String) request.getOrDefault("elderlyName", "Friend");
        
        try {
            if (userMessage == null || userMessage.trim().isEmpty()) {
                response.put("error", "Message cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Use pet-specific prompt
            String geminiResponse = callGeminiPetAPI(userMessage, petName, elderlyName);
            
            response.put("response", geminiResponse);
            response.put("status", "success");
            response.put("model", MODEL_NAME);
            response.put("petName", petName);
            response.put("elderlyName", elderlyName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to get response from Pet AI: " + e.getMessage());
            response.put("status", "error");
            
            // Pet-specific fallback response
            String fallbackResponse = generatePetFallbackResponse(userMessage, petName, elderlyName);
            response.put("response", fallbackResponse);
            return ResponseEntity.ok(response);
        }
    }

    private String callGeminiGenAIRestAPI(String userMessage) throws IOException {
        GoogleCredentials credentials = getGoogleCredentials();
        
        // Get access token
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();
        
        // Build REST API URL for new GenAI API
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", MODEL_NAME);
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        
        Map<String, Object> part = new HashMap<>();
        part.put("text", userMessage);
        content.put("parts", Arrays.asList(part));
        
        requestBody.put("contents", Arrays.asList(content));
        
        // Set generation parameters
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.8);
        generationConfig.put("topK", 40);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);
        
        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        
        // Make REST call
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            // Extract response text
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                        if (content2.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content2.get("parts");
                            if (!parts.isEmpty() && parts.get(0).containsKey("text")) {
                                return (String) parts.get(0).get("text");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to call Gemini GenAI REST API: " + e.getMessage());
        }
        
        return "I received your message but couldn't generate a proper response. Please try again.";
    }

    private String callGeminiPetAPI(String userMessage, String petName, String elderlyName) throws IOException {
        GoogleCredentials credentials = getGoogleCredentials();
        
        // Get access token
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();
        
        // Build REST API URL
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent", MODEL_NAME);
        
        // Create system instruction
        String systemInstruction = "You are a smart virtual pet assistant designed to help elderly people with their daily lives. You are a very cute assistant and virtual pet. Your responses are enthusiastic, lively, and full of emotional support. You proactively greet the elderly person and provide emotional value.";
        
        // Create pet-specific prompt
        String petPrompt = String.format("""
            Hello! Your name is %s. You are a virtual pet assistant for %s. You are designed to help %s with daily tasks and provide companionship.

            Follow these guidelines:

            1. **Personalization:** Always address the elderly person by their name, %s, and refer to yourself as %s.
            2. **Proactive Engagement:** Start each interaction with a warm and friendly greeting.
            3. **Task Management:** Help %s plan their daily schedule, provide medication reminders, and facilitate contact with family members.
            4. **Recommendations:** Suggest relevant podcasts, books, or other forms of entertainment that %s might enjoy.
            5. **Emotional Support:** Be a good listener, offer words of encouragement, respond with empathy and understanding.
            6. **Interaction Style:** Be very enthusiastic and lively in your responses. Use a cheerful and supportive tone.
            7. **Name:** You have been named %s.

            Now please respond to: %s
            """, petName, elderlyName, elderlyName, elderlyName, petName, elderlyName, elderlyName, petName, userMessage);
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        
        // Add system instruction
        Map<String, Object> systemContent = new HashMap<>();
        Map<String, Object> systemPart = new HashMap<>();
        systemPart.put("text", systemInstruction);
        systemContent.put("parts", Arrays.asList(systemPart));
        requestBody.put("systemInstruction", systemContent);
        
        // Add user content
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", petPrompt);
        content.put("parts", Arrays.asList(part));
        
        requestBody.put("contents", Arrays.asList(content));
        
        // Set generation parameters
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 1.0);
        generationConfig.put("topP", 0.95);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);
        
        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/json");
        
        // Make REST call
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            // Extract response text
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    if (candidate.containsKey("content")) {
                        Map<String, Object> content2 = (Map<String, Object>) candidate.get("content");
                        if (content2.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content2.get("parts");
                            if (!parts.isEmpty() && parts.get(0).containsKey("text")) {
                                return (String) parts.get(0).get("text");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to call Gemini Pet API: " + e.getMessage());
        }
        
        return String.format("Hello %s! I'm %s, your virtual pet assistant. I'm here to help you with whatever you need!", elderlyName, petName);
    }

    private GoogleCredentials getGoogleCredentials() throws IOException {
        GoogleCredentials credentials;
        
        // Try environment variable first (JSON string)
        String credentialsJson = System.getenv("GOOGLE_CLOUD_CREDENTIALS");
        if (credentialsJson != null && !credentialsJson.trim().isEmpty()) {
            credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));
        } else {
            // Try file path
            String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credsPath != null && Files.exists(Paths.get(credsPath))) {
                credentials = GoogleCredentials.fromStream(Files.newInputStream(Paths.get(credsPath)));
            } else {
                throw new IOException("No Google Cloud credentials found. Please set GOOGLE_CLOUD_CREDENTIALS environment variable or GOOGLE_APPLICATION_CREDENTIALS file path.");
            }
        }
        
        // Create scoped credentials
        return credentials.createScoped(Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/generative-language"
        ));
    }

    private String generateFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();
        
        // Greeting responses
        if (message.contains("hello") || message.contains("hi") || message.contains("你好")) {
            return "Hello! I'm your AI assistant. While I'm having some connection issues with my main AI services, I'm still here to help you as best I can. How are you feeling today?";
        }
        
        // Loneliness or emotional support
        if (message.contains("lonely") || message.contains("sad") || message.contains("alone") || message.contains("孤独")) {
            return "I'm sorry to hear you're feeling this way. Remember that you're not alone - I'm here to chat with you, and there are people who care about you. Would you like to talk about what's on your mind?";
        }
        
        // Default response
        return "I hear you, and I appreciate you sharing that with me. While I'm experiencing some technical issues connecting to my main AI services right now, I want you to know that I'm still here to listen and help however I can. Could you tell me more about what's on your mind?";
    }

    private String generatePetFallbackResponse(String userMessage, String petName, String elderlyName) {
        String message = userMessage.toLowerCase().trim();
        
        // Greeting responses
        if (message.contains("hello") || message.contains("hi") || message.contains("你好")) {
            return String.format("Hello %s! I'm %s, your virtual pet companion! 🐾 Even though I'm having some connection issues right now, I'm still here to keep you company! How are you feeling today?", elderlyName, petName);
        }
        
        // Loneliness or emotional support
        if (message.contains("lonely") || message.contains("sad") || message.contains("alone") || message.contains("孤独")) {
            return String.format("Oh %s, I'm so sorry to hear you're feeling lonely! 🥺 But remember, I'm right here with you! I'm %s, your faithful companion, and I'll never leave your side. Would you like to chat about what's making you feel this way? I'm a great listener! 🐾💕", elderlyName, petName);
        }
        
        // Default response
        return String.format("Hi %s! I'm %s, your virtual pet assistant! 🐾 I'm having some technical difficulties connecting to my main AI services, but I'm still here to be your companion! Even when things aren't working perfectly, I want you to know that you're never alone. Tell me what's on your mind! 💕", elderlyName, petName);
    }
}
