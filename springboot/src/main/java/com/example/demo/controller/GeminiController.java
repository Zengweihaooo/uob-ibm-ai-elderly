package com.example.demo.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
public class GeminiController {

    // Google Cloud project and location settings
    private static final String PROJECT_ID = "organic-totem-467918-a5"; // Your project ID
    private static final String LOCATION = "us-central1"; // Gemini is available in us-central1
    private static final String MODEL_NAME = "gemini-pro"; // Standard Gemini model

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

            // Initialize Vertex AI client
            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setCredentialsProvider(() -> getGoogleCredentials())
                .build();
                
            // Use REST API approach for better compatibility
            String geminiResponse = callGeminiRestAPI(userMessage);
            
            response.put("response", geminiResponse);
            response.put("status", "success");
            response.put("model", MODEL_NAME);
            
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
        
        // Create scoped credentials for Vertex AI
        return credentials.createScoped(Arrays.asList(
            "https://www.googleapis.com/auth/cloud-platform",
            "https://www.googleapis.com/auth/cloud-platform.read-only"
        ));
    }

    private void addMapToStruct(Struct.Builder builder, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Value.Builder valueBuilder = Value.newBuilder();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                valueBuilder.setStringValue((String) value);
            } else if (value instanceof Number) {
                valueBuilder.setNumberValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                valueBuilder.setBoolValue((Boolean) value);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) value;
                Value.Builder listBuilder = Value.newBuilder();
                for (Object item : list) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        Struct.Builder itemBuilder = Struct.newBuilder();
                        addMapToStruct(itemBuilder, itemMap);
                        listBuilder.getListValueBuilder().addValues(
                            Value.newBuilder().setStructValue(itemBuilder.build())
                        );
                    }
                }
                valueBuilder = listBuilder;
            }
            
            builder.putFields(entry.getKey(), valueBuilder.build());
        }
    }

    private String extractTextFromPrediction(Value prediction) {
        try {
            // Navigate the prediction structure to extract text
            if (prediction.hasStructValue()) {
                Struct struct = prediction.getStructValue();
                if (struct.containsFields("content")) {
                    Value content = struct.getFieldsMap().get("content");
                    if (content.hasStringValue()) {
                        return content.getStringValue();
                    }
                }
                
                // Try alternative structure
                if (struct.containsFields("candidates")) {
                    Value candidates = struct.getFieldsMap().get("candidates");
                    if (candidates.hasListValue() && !candidates.getListValue().getValuesList().isEmpty()) {
                        Value firstCandidate = candidates.getListValue().getValues(0);
                        if (firstCandidate.hasStructValue()) {
                            Struct candidateStruct = firstCandidate.getStructValue();
                            if (candidateStruct.containsFields("content")) {
                                Value candidateContent = candidateStruct.getFieldsMap().get("content");
                                if (candidateContent.hasStructValue()) {
                                    Struct contentStruct = candidateContent.getStructValue();
                                    if (contentStruct.containsFields("parts")) {
                                        Value parts = contentStruct.getFieldsMap().get("parts");
                                        if (parts.hasListValue() && !parts.getListValue().getValuesList().isEmpty()) {
                                            Value firstPart = parts.getListValue().getValues(0);
                                            if (firstPart.hasStructValue()) {
                                                Struct partStruct = firstPart.getStructValue();
                                                if (partStruct.containsFields("text")) {
                                                    return partStruct.getFieldsMap().get("text").getStringValue();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            return "I received your message but couldn't format my response properly. Please try again.";
        } catch (Exception e) {
            return "I'm having trouble processing your request. Please try again.";
        }
    }

    private String callGeminiRestAPI(String userMessage) throws IOException {
        GoogleCredentials credentials = getGoogleCredentials();
        
        // Get access token
        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();
        
        // Build REST API URL
        String url = String.format("https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent", 
            LOCATION, PROJECT_ID, LOCATION, MODEL_NAME);
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        content.put("role", "user");
        
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
            throw new IOException("Failed to call Gemini REST API: " + e.getMessage());
        }
        
        return "I received your message but couldn't generate a proper response. Please try again.";
    }

    private String generateFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase().trim();
        
        // Greeting responses
        if (message.contains("hello") || message.contains("hi") || message.contains("你好")) {
            return "Hello! I'm your AI assistant. While I'm having some connection issues with my main AI services, I'm still here to help you as best I can. How are you feeling today?";
        }
        
        // Health-related questions
        if (message.contains("health") || message.contains("feel") || message.contains("sick") || message.contains("pain")) {
            return "I understand you're asking about health. While I can't provide medical advice, I recommend speaking with a healthcare professional if you're not feeling well. Is there anything else I can help you with today?";
        }
        
        // Loneliness or emotional support
        if (message.contains("lonely") || message.contains("sad") || message.contains("alone") || message.contains("孤独")) {
            return "I'm sorry to hear you're feeling this way. Remember that you're not alone - I'm here to chat with you, and there are people who care about you. Would you like to talk about what's on your mind?";
        }
        
        // Schedule or time-related
        if (message.contains("schedule") || message.contains("time") || message.contains("appointment") || message.contains("日程")) {
            return "I can help you think about your schedule! You can use the Schedule page to add and manage your appointments. Is there a particular event or activity you'd like to plan?";
        }
        
        // Weather-related
        if (message.contains("weather") || message.contains("rain") || message.contains("sunny") || message.contains("天气")) {
            return "I wish I could check the current weather for you! For the most accurate weather information, I recommend checking your local weather app or website. Are you planning any outdoor activities?";
        }
        
        // Family or social
        if (message.contains("family") || message.contains("children") || message.contains("grandchildren") || message.contains("家人")) {
            return "Family is so important! It's wonderful that you're thinking about your loved ones. Have you been able to connect with them recently? Sometimes a simple phone call or message can brighten everyone's day.";
        }
        
        // Thank you
        if (message.contains("thank") || message.contains("thanks") || message.contains("谢谢")) {
            return "You're very welcome! I'm happy to help. Even though I'm having some technical difficulties, I'm always here to listen and support you. Is there anything else you'd like to talk about?";
        }
        
        // Default response
        return "I hear you, and I appreciate you sharing that with me. While I'm experiencing some technical issues connecting to my main AI services right now, I want you to know that I'm still here to listen and help however I can. Could you tell me more about what's on your mind?";
    }
}
