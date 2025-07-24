package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    // In-memory storage for demo purposes
    private List<Map<String, Object>> chatHistory = new ArrayList<>();
    private long messageIdCounter = 1;

    /**
     * Send a text message to the AI assistant
     * @param messageData Message content and metadata
     * @return AI response
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestBody Map<String, Object> messageData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract message data
            String content = (String) messageData.get("content");
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) messageData.getOrDefault("attachments", new ArrayList<>());
            
            // Validate input
            if (content == null || content.trim().isEmpty()) {
                if (attachments.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Message content cannot be empty");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Store user message
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("id", messageIdCounter++);
            userMessage.put("type", "user");
            userMessage.put("content", content);
            userMessage.put("attachments", attachments);
            userMessage.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            userMessage.put("userId", getUserIdFromToken(authHeader));
            
            chatHistory.add(userMessage);
            
            // Generate AI response (demo mode)
            Map<String, Object> aiMessage = generateAIResponse(content, attachments);
            chatHistory.add(aiMessage);
            
            // Prepare response
            response.put("success", true);
            response.put("userMessage", userMessage);
            response.put("aiResponse", aiMessage);
            response.put("messageId", userMessage.get("id"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Upload file attachment
     * @param file Uploaded file
     * @return File upload response
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "No file uploaded");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate file size (max 10MB for demo)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "File size exceeds 10MB limit");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Process file (in real implementation, save to storage)
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("id", UUID.randomUUID().toString());
            fileInfo.put("name", file.getOriginalFilename());
            fileInfo.put("size", file.getSize());
            fileInfo.put("type", file.getContentType());
            fileInfo.put("uploadTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            fileInfo.put("userId", getUserIdFromToken(authHeader));
            
            // Determine file category
            String contentType = file.getContentType();
            String category = "file";
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    category = "image";
                } else if (contentType.startsWith("video/")) {
                    category = "video";
                } else if (contentType.startsWith("audio/")) {
                    category = "audio";
                } else if (contentType.equals("application/pdf")) {
                    category = "pdf";
                }
            }
            fileInfo.put("category", category);
            
            response.put("success", true);
            response.put("file", fileInfo);
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get chat history
     * @param limit Number of messages to retrieve
     * @return Chat history
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            // Filter messages by user (in demo, return all for simplicity)
            List<Map<String, Object>> filteredHistory = new ArrayList<>();
            
            // Get the last 'limit' messages
            int start = Math.max(0, chatHistory.size() - limit);
            for (int i = start; i < chatHistory.size(); i++) {
                Map<String, Object> message = chatHistory.get(i);
                // In real implementation, filter by userId
                filteredHistory.add(message);
            }
            
            response.put("success", true);
            response.put("messages", filteredHistory);
            response.put("totalMessages", chatHistory.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving chat history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Clear chat history
     * @return Success response
     */
    @DeleteMapping("/history")
    public ResponseEntity<Map<String, Object>> clearChatHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // In real implementation, only clear messages for specific user
            chatHistory.clear();
            messageIdCounter = 1;
            
            response.put("success", true);
            response.put("message", "Chat history cleared successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing chat history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get AI assistant status and capabilities
     * @return AI assistant information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> aiStatus = new HashMap<>();
        aiStatus.put("online", true);
        aiStatus.put("version", "1.0.0-demo");
        aiStatus.put("mode", "demo");
        aiStatus.put("capabilities", Arrays.asList(
            "text_chat",
            "file_upload",
            "image_analysis",
            "voice_messages",
            "location_sharing",
            "health_advice",
            "medication_reminders",
            "emergency_contacts"
        ));
        aiStatus.put("supportedFileTypes", Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "video/mp4", "video/webm",
            "audio/mp3", "audio/wav", "audio/webm",
            "application/pdf",
            "text/plain"
        ));
        aiStatus.put("maxFileSize", "10MB");
        aiStatus.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        response.put("success", true);
        response.put("aiStatus", aiStatus);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Generate AI response (demo implementation)
     * @param userMessage User's message content
     * @param attachments Message attachments
     * @return AI response message
     */
    private Map<String, Object> generateAIResponse(String userMessage, List<Map<String, Object>> attachments) {
        Map<String, Object> aiMessage = new HashMap<>();
        aiMessage.put("id", messageIdCounter++);
        aiMessage.put("type", "bot");
        aiMessage.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Generate contextual response based on content
        String response = generateContextualResponse(userMessage, attachments);
        aiMessage.put("content", response);
        
        // Add metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("confidence", 0.95);
        metadata.put("responseTime", "1.2s");
        metadata.put("model", "healthcare-assistant-demo-v1.0");
        aiMessage.put("metadata", metadata);
        
        return aiMessage;
    }

    /**
     * Generate contextual response based on user input
     */
    private String generateContextualResponse(String userMessage, List<Map<String, Object>> attachments) {
        if (userMessage == null) userMessage = "";
        String lowerMessage = userMessage.toLowerCase();
        
        // Health-related responses
        if (lowerMessage.contains("health") || lowerMessage.contains("feel") || lowerMessage.contains("sick")) {
            return "I understand you're asking about health concerns. While I'm currently in demo mode, I'm designed to help with health questions, medication reminders, and wellness support. Please consult with your healthcare provider for specific medical advice.";
        }
        
        // Medication-related responses
        if (lowerMessage.contains("medication") || lowerMessage.contains("medicine") || lowerMessage.contains("pill")) {
            return "I can help you manage your medications! In the full version, I'll be able to set reminders, track dosages, and alert your emergency contacts if needed. For now, I'm receiving your message successfully.";
        }
        
        // Emergency-related responses
        if (lowerMessage.contains("emergency") || lowerMessage.contains("help") || lowerMessage.contains("urgent")) {
            return "I recognize this might be urgent. In the full version, I would immediately alert your emergency contacts and provide appropriate guidance. Please call emergency services (911) if you need immediate assistance.";
        }
        
        // File/attachment responses
        if (!attachments.isEmpty()) {
            StringBuilder attachmentInfo = new StringBuilder();
            attachmentInfo.append("I received your message with ");
            
            Map<String, Integer> typeCounts = new HashMap<>();
            for (Map<String, Object> attachment : attachments) {
                String type = (String) attachment.getOrDefault("type", "file");
                typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
            }
            
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                if (count == 1) {
                    parts.add("1 " + type);
                } else {
                    parts.add(count + " " + type + "s");
                }
            }
            
            attachmentInfo.append(String.join(", ", parts));
            attachmentInfo.append(". In the full version, I would analyze these attachments and provide relevant healthcare insights. Thank you for sharing!");
            
            return attachmentInfo.toString();
        }
        
        // Location-related responses
        if (lowerMessage.contains("location") || lowerMessage.contains("where")) {
            return "I received your location information. In the full version, I could help you find nearby healthcare facilities, pharmacies, or provide location-based health reminders. Thank you for sharing your location!";
        }
        
        // General greeting responses
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey")) {
            return "Hello! I'm your AI Healthcare Assistant. I'm currently in demo mode, but I'm designed to help you with health questions, medication management, and wellness support. How can I assist you today?";
        }
        
        // Default response
        return "Message received! Thank you for reaching out. I'm currently in demo mode, but I'm ready to help you with health questions, medication reminders, and wellness support once my AI capabilities are fully activated. In the meantime, I'm successfully receiving and processing all your messages and attachments.";
    }

    /**
     * Extract user ID from authorization token (demo implementation)
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return 0L; // Guest user
        }
        
        String token = authHeader.substring(7);
        if (token.startsWith("demo-token-")) {
            return 1L; // Demo user
        }
        
        return 0L; // Default to guest
    }
} 