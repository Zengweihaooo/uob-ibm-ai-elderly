package com.example.demo.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.EmotionCompanion;
import com.example.demo.service.EmotionCompanionService;
import com.example.demo.service.PodcastAutoPlayService;
import com.example.demo.service.PodcastService;

@RestController
@RequestMapping("/api/pet")
@CrossOrigin(origins = "*")
public class EmotionCompanionController {

    @Autowired
    private EmotionCompanionService emotionCompanionService;
    
    @Autowired
    private PodcastService podcastService;
    
    @Autowired
    private PodcastAutoPlayService podcastAutoPlayService;

    // In-memory storage for conversation history and user data
    private Map<Long, List<Map<String, Object>>> conversationHistory = new HashMap<>();
    private long messageIdCounter = 1;

    /**
     * Get emotion companion state and information
     * @param authHeader Authorization token
     * @return Emotion companion status including happiness, energy, responsiveness
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionState(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            
            // Check for neglect and update emotional state
            emotionCompanionService.updateNeglectLevel(companion);
            emotionCompanionService.updateEmotionBasedOnNeglect(companion);
            
            response.put("success", true);
            response.put("companion", companion);
            response.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving emotion companion state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Interact with emotion companion (chat, command, question, greet)
     * @param interactionData Interaction type and parameters
     * @param authHeader Authorization token
     * @return Companion response and updated state
     */
    @PostMapping("/interact")
    public ResponseEntity<Map<String, Object>> interactWithEmotionCompanion(
            @RequestBody Map<String, Object> interactionData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String interactionType = (String) interactionData.get("type");
            String message = (String) interactionData.get("message");
            
            if (interactionType == null) {
                response.put("success", false);
                response.put("message", "Interaction type is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            Map<String, Object> result = emotionCompanionService.processInteraction(companion, interactionType, message);
            
            response.put("success", true);
            response.put("interaction", result);
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Send text message to emotion companion
     * @param messageData Message content and type
     * @param authHeader Authorization token
     * @return Companion response and conversation update
     */
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessageToCompanion(
            @RequestBody Map<String, Object> messageData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String message = (String) messageData.get("message");
            String messageType = (String) messageData.getOrDefault("type", "text");
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Store user message
            Map<String, Object> userMessage = createMessage("user", message, messageType, userId);
            getConversationHistoryForUser(userId).add(userMessage);
            
            // Generate companion response
            Map<String, Object> companionResponse = generateCompanionResponse(message, userId);
            getConversationHistoryForUser(userId).add(companionResponse);
            
            // Update companion state
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.processInteraction(companion, "chat", message);
            
            response.put("success", true);
            response.put("userMessage", userMessage);
            response.put("companionResponse", companionResponse);
            response.put("conversationId", userMessage.get("id"));
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing message: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Voice interaction with emotion companion
     * @param voiceData Voice message data
     * @param authHeader Authorization token
     * @return Companion voice response
     */
    @PostMapping("/voice")
    public ResponseEntity<Map<String, Object>> voiceInteraction(
            @RequestBody Map<String, Object> voiceData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String voiceContent = (String) voiceData.get("content");
            Integer duration = (Integer) voiceData.getOrDefault("duration", 5);
            String transcription = (String) voiceData.get("transcription");
            
            // For now, simulate voice processing
            String processedText = transcription != null ? transcription : "Voice message received";
            
            // Store voice message
            Map<String, Object> voiceMessage = createMessage("user", processedText, "voice", userId);
            voiceMessage.put("duration", duration);
            getConversationHistoryForUser(userId).add(voiceMessage);
            
            // Generate companion voice response
            Map<String, Object> companionVoiceResponse = generateCompanionVoiceResponse(processedText, userId);
            getConversationHistoryForUser(userId).add(companionVoiceResponse);
            
            // Update companion state
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.processInteraction(companion, "voice", processedText);
            
            response.put("success", true);
            response.put("voiceMessage", voiceMessage);
            response.put("companionResponse", companionVoiceResponse);
            response.put("transcription", processedText);
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing voice interaction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check user's daily schedule and provide reminders
     * @param authHeader Authorization token
     * @return Schedule-based reminders and suggestions
     */
    @GetMapping("/schedule-check")
    public ResponseEntity<Map<String, Object>> checkScheduleReminders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            List<Map<String, Object>> reminders = generateScheduleReminders(userId);
            Map<String, Object> companionSuggestion = generateCompanionScheduleSuggestion(reminders);
            
            // Update companion state
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.processInteraction(companion, "schedule", "Schedule check requested");
            
            response.put("success", true);
            response.put("reminders", reminders);
            response.put("companionSuggestion", companionSuggestion);
            response.put("checkTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Emergency detection and response
     * @param emergencyData Emergency situation data
     * @param authHeader Authorization token
     * @return Emergency response and actions
     */
    @PostMapping("/emergency")
    public ResponseEntity<Map<String, Object>> handleEmergency(
            @RequestBody Map<String, Object> emergencyData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String emergencyType = (String) emergencyData.get("type");
            String description = (String) emergencyData.get("description");
            String severity = (String) emergencyData.getOrDefault("severity", "medium");
            
            Map<String, Object> emergencyResponse = processEmergency(emergencyType, description, severity, userId);
            
            // Store emergency interaction
            Map<String, Object> emergencyMessage = createMessage("system", "Emergency detected: " + description, "emergency", userId);
            emergencyMessage.put("emergencyType", emergencyType);
            emergencyMessage.put("severity", severity);
            getConversationHistoryForUser(userId).add(emergencyMessage);
            
            // Update companion state
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.processInteraction(companion, "emergency", "Emergency: " + emergencyType);
            
            response.put("success", true);
            response.put("emergency", emergencyResponse);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error handling emergency: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get conversation history
     * @param limit Number of messages to retrieve
     * @param authHeader Authorization token
     * @return Conversation history
     */
    @GetMapping("/conversation")
    public ResponseEntity<Map<String, Object>> getConversationHistory(
            @RequestParam(defaultValue = "50") int limit,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            List<Map<String, Object>> history = getConversationHistoryForUser(userId);
            List<Map<String, Object>> limitedHistory = history.size() > limit ? 
                history.subList(history.size() - limit, history.size()) : history;
            
            response.put("success", true);
            response.put("conversation", limitedHistory);
            response.put("totalMessages", history.size());
            response.put("retrievedMessages", limitedHistory.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving conversation history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update emotion companion settings
     * @param settingsData Settings to update
     * @param authHeader Authorization token
     * @return Updated settings
     */
    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateCompanionSettings(
            @RequestBody Map<String, Object> settingsData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            
            // Update settings
            if (settingsData.containsKey("name")) {
                companion.setName((String) settingsData.get("name"));
            }
            if (settingsData.containsKey("personality")) {
                companion.setPersonality((String) settingsData.get("personality"));
            }
            if (settingsData.containsKey("responsiveness")) {
                companion.setResponsiveness((Integer) settingsData.get("responsiveness"));
            }
            if (settingsData.containsKey("avatar")) {
                companion.setAvatar((String) settingsData.get("avatar"));
            }
            
            companion.setLastUpdate(LocalDateTime.now());
            
            response.put("success", true);
            response.put("companion", companion);
            response.put("message", "Companion settings updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating settings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get companion expressions (sound and visual)
     * @param authHeader Authorization token
     * @return Current expressions
     */
    @GetMapping("/expressions")
    public ResponseEntity<Map<String, Object>> getCompanionExpressions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.updateExpressions(companion);
            
            Map<String, Object> expressions = new HashMap<>();
            expressions.put("soundExpression", companion.getCurrentSound());
            expressions.put("visualExpression", companion.getVisualExpression());
            expressions.put("isMakingSound", companion.isMakingSound());
            expressions.put("isExpressingEmotion", companion.isExpressingEmotion());
            expressions.put("ledColor", companion.getLedColor());
            
            response.put("success", true);
            response.put("expressions", expressions);
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting expressions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check if companion needs attention
     * @param authHeader Authorization token
     * @return Attention status
     */
    @GetMapping("/attention-check")
    public ResponseEntity<Map<String, Object>> checkCompanionAttention(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.updateNeglectLevel(companion);
            
            Map<String, Object> attentionStatus = new HashMap<>();
            attentionStatus.put("needsAttention", companion.isNeedsAttention());
            attentionStatus.put("neglectLevel", companion.getNeglectLevel());
            attentionStatus.put("lastAttentionTime", companion.getLastAttentionTime());
            attentionStatus.put("hoursSinceLastAttention", 
                Duration.between(companion.getLastAttentionTime(), LocalDateTime.now()).toHours());
            
            response.put("success", true);
            response.put("attentionStatus", attentionStatus);
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking attention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get companion activity status
     * @param authHeader Authorization token
     * @return Activity status
     */
    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getCompanionActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            emotionCompanionService.updateActivityMode(companion);
            
            Map<String, Object> activityStatus = new HashMap<>();
            activityStatus.put("activityMode", companion.getActivityMode());
            activityStatus.put("isActive", companion.isActive());
            activityStatus.put("currentTask", companion.getCurrentTask());
            activityStatus.put("isLearning", companion.isLearning());
            activityStatus.put("helpfulness", companion.getHelpfulness());
            
            response.put("success", true);
            response.put("activityStatus", activityStatus);
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting activity: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Put companion to sleep mode
     * @param authHeader Authorization token
     * @return Sleep status
     */
    @PostMapping("/sleep")
    public ResponseEntity<Map<String, Object>> putCompanionToSleep(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            companion.setActivityMode("sleeping");
            companion.setActive(false);
            companion.setCurrentTask("Sleeping peacefully");
            
            response.put("success", true);
            response.put("message", "Companion is now in sleep mode");
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error putting companion to sleep: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Wake companion from sleep mode
     * @param authHeader Authorization token
     * @return Wake status
     */
    @PostMapping("/wake")
    public ResponseEntity<Map<String, Object>> wakeCompanion(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
            companion.setActivityMode("listening");
            companion.setActive(true);
            companion.setCurrentTask("Ready to help");
            
            response.put("success", true);
            response.put("message", "Companion is now awake and ready");
            response.put("companion", companion);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error waking companion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== Podcast Integration ====================

    /**
     * Get podcast recommendations based on user interests
     * @param interestsData User interests
     * @param authHeader Authorization token
     * @return Podcast recommendations with companion response
     */
    @PostMapping("/podcast/recommendations")
    public ResponseEntity<Map<String, Object>> getPodcastRecommendations(
            @RequestBody Map<String, Object> interestsData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            @SuppressWarnings("unchecked")
            List<String> interests = (List<String>) interestsData.get("interests");
            
            if (interests == null || interests.isEmpty()) {
                response.put("success", false);
                response.put("message", "Interests list is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> recommendationsResult = podcastService.getPodcastRecommendations(interests);
            
            if ((Boolean) recommendationsResult.get("success")) {
                EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
                String companionResponse = generatePodcastRecommendationResponse(interests, recommendationsResult);
                
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("interests", interests);
                response.put("companionResponse", companionResponse);
                response.put("companion", companion);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting podcast recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Schedule podcast auto-play
     * @param scheduleData Schedule information
     * @param authHeader Authorization token
     * @return Schedule confirmation
     */
    @PostMapping("/podcast/schedule")
    public ResponseEntity<Map<String, Object>> schedulePodcastAutoPlay(
            @RequestBody Map<String, Object> scheduleData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            String podcastTitle = (String) scheduleData.get("podcastTitle");
            String playTime = (String) scheduleData.get("playTime");
            String playDate = (String) scheduleData.get("playDate");
            
            if (podcastTitle == null || playTime == null) {
                response.put("success", false);
                response.put("message", "Podcast title and play time are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> scheduleResult = podcastAutoPlayService.schedulePodcast(userId, podcastTitle, playTime, playDate);
            
            if ((Boolean) scheduleResult.get("success")) {
                EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
                String companionResponse = generateScheduleConfirmationResponse(podcastTitle, playTime, playDate);
                
                response.put("success", true);
                response.put("schedule", scheduleResult.get("schedule"));
                response.put("companionResponse", companionResponse);
                response.put("companion", companion);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", scheduleResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error scheduling podcast: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get user's podcast schedules
     * @param authHeader Authorization token
     * @return List of scheduled podcasts
     */
    @GetMapping("/podcast/schedules")
    public ResponseEntity<Map<String, Object>> getUserPodcastSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Map<String, Object> schedulesResult = podcastAutoPlayService.getUserSchedulesMap(userId);
            
            if ((Boolean) schedulesResult.get("success")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> schedules = (List<Map<String, Object>>) schedulesResult.get("schedules");
                EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
                String companionResponse = generateSchedulesOverviewResponse(schedules);
                
                response.put("success", true);
                response.put("schedules", schedules);
                response.put("totalSchedules", schedules.size());
                response.put("companionResponse", companionResponse);
                response.put("companion", companion);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", schedulesResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting podcast schedules: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get elderly-specific podcast recommendations
     * @param authHeader Authorization token
     * @return Elderly podcast recommendations
     */
    @GetMapping("/podcast/elderly-recommendations")
    public ResponseEntity<Map<String, Object>> getElderlyPodcastRecommendations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            Map<String, Object> recommendationsResult = podcastService.getElderlyPodcastRecommendations();
            
            if ((Boolean) recommendationsResult.get("success")) {
                EmotionCompanion companion = emotionCompanionService.getEmotionCompanionForUser(userId);
                String companionResponse = generateElderlyRecommendationsResponse(recommendationsResult);
                
                response.put("success", true);
                response.put("recommendations", recommendationsResult.get("recommendations"));
                response.put("totalRecommendations", recommendationsResult.get("totalRecommendations"));
                response.put("companionResponse", companionResponse);
                response.put("companion", companion);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", recommendationsResult.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting elderly recommendations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== Helper Methods ====================

    private List<Map<String, Object>> getConversationHistoryForUser(Long userId) {
        return conversationHistory.computeIfAbsent(userId, k -> new ArrayList<>());
    }

    private Map<String, Object> createMessage(String sender, String content, String type, Long userId) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", messageIdCounter++);
        message.put("sender", sender);
        message.put("content", content);
        message.put("type", type);
        message.put("userId", userId);
        message.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return message;
    }

    private Map<String, Object> generateCompanionResponse(String userMessage, Long userId) {
        String lowerMessage = userMessage.toLowerCase();
        String response;
        
        // Health-related responses
        if (lowerMessage.contains("health") || lowerMessage.contains("medicine") || lowerMessage.contains("doctor")) {
            response = "Beep! 🏥 Health is very important! I can help remind you about your medications and appointments. Have you taken your medicine today?";
        }
        // Schedule-related responses
        else if (lowerMessage.contains("schedule") || lowerMessage.contains("appointment") || lowerMessage.contains("reminder")) {
            response = "Chime! 📅 I love helping with schedules! I can see you have some activities planned. Would you like me to remind you about them?";
        }
        // Emotional responses
        else if (lowerMessage.contains("sad") || lowerMessage.contains("lonely") || lowerMessage.contains("tired")) {
            response = "Beep... 🤗 I'm here for you! You're never alone when I'm around. Let's do something fun together to cheer you up!";
        }
        // Happy responses
        else if (lowerMessage.contains("happy") || lowerMessage.contains("good") || lowerMessage.contains("great")) {
            response = "Chime! 😊 I'm so happy to hear that! Your happiness makes me happy too! Let's keep this positive energy going!";
        }
        // Greeting responses
        else if (lowerMessage.contains("hello") || lowerMessage.contains("hi") || lowerMessage.contains("hey")) {
            response = "Beep! 👋 Hello there! I'm so excited to chat with you! How are you feeling today?";
        }
        // Default responses
        else {
            String[] defaultResponses = {
                "Beep! That's interesting! Tell me more! 😊",
                "Chime... I love chatting with you! You always have such nice things to say! 💙",
                "Beep beep! I may be a virtual companion, but my care for you is very real! 💕",
                "That sounds important! I'm always here to listen and help however I can! 🤗",
                "Chime... You know, talking with you always brightens my day! 🌟"
            };
            response = defaultResponses[new Random().nextInt(defaultResponses.length)];
        }
        
        return createMessage("companion", response, "text", userId);
    }

    private Map<String, Object> generateCompanionVoiceResponse(String transcription, Long userId) {
        String[] voiceResponses = {
            "Beep! I heard you loud and clear! Your voice always makes me happy! 🤖",
            "Chime... I love hearing your voice! You sound wonderful today! 😊",
            "Beep beep! I wish I could understand every word, but I can feel your love! 💕",
            "Your voice is so soothing! It reminds me to tell you - don't forget your daily activities! 📋",
            "Chime... I heard concern in your voice. Is everything okay? I'm here for you! 🤗"
        };
        
        String response = voiceResponses[new Random().nextInt(voiceResponses.length)];
        Map<String, Object> voiceResponse = createMessage("companion", response, "voice", userId);
        voiceResponse.put("audioUrl", "/api/pet/tts/" + voiceResponse.get("id"));
        voiceResponse.put("duration", 3);
        
        return voiceResponse;
    }

    private List<Map<String, Object>> generateScheduleReminders(Long userId) {
        List<Map<String, Object>> reminders = new ArrayList<>();
        
        Map<String, Object> reminder1 = new HashMap<>();
        reminder1.put("type", "medication");
        reminder1.put("title", "Morning Medication");
        reminder1.put("time", "08:00");
        reminder1.put("priority", "high");
        reminder1.put("message", "Don't forget your morning medication!");
        reminders.add(reminder1);
        
        Map<String, Object> reminder2 = new HashMap<>();
        reminder2.put("type", "activity");
        reminder2.put("title", "Morning Walk");
        reminder2.put("time", "08:30");
        reminder2.put("priority", "medium");
        reminder2.put("message", "Time for your morning walk in the park!");
        reminders.add(reminder2);
        
        return reminders;
    }

    private Map<String, Object> generateCompanionScheduleSuggestion(List<Map<String, Object>> reminders) {
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("message", "Beep! 📅 I found " + reminders.size() + " activities for you today. Would you like me to remind you about them?");
        suggestion.put("reminderCount", reminders.size());
        suggestion.put("suggestionType", "schedule_reminder");
        return suggestion;
    }

    private Map<String, Object> processEmergency(String type, String description, String severity, Long userId) {
        Map<String, Object> emergency = new HashMap<>();
        
        emergency.put("type", type);
        emergency.put("description", description);
        emergency.put("severity", severity);
        emergency.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        String response;
        List<String> actions = new ArrayList<>();
        
        switch (type.toLowerCase()) {
            case "health":
                response = "Beep! 🚨 I'm concerned about your health! Let me help you right away!";
                actions.add("Contact emergency services (911)");
                actions.add("Call your emergency contact");
                actions.add("Provide first aid guidance");
                break;
                
            case "fall":
                response = "Oh no! 😿 I detected you might have fallen! Are you okay? I'm getting help!";
                actions.add("Emergency services contacted");
                actions.add("Family members notified");
                actions.add("Location shared with responders");
                break;
                
            case "medication":
                response = "Beep! 💊 There seems to be an issue with your medication! Let me help you sort this out!";
                actions.add("Review medication schedule");
                actions.add("Contact healthcare provider");
                actions.add("Check for drug interactions");
                break;
                
            default:
                response = "Beep! 🆘 I sense something might be wrong! I'm here to help you through this!";
                actions.add("Assess the situation");
                actions.add("Contact appropriate help");
                actions.add("Stay with you for support");
        }
        
        emergency.put("companionResponse", response);
        emergency.put("recommendedActions", actions);
        emergency.put("status", "processing");
        
        return emergency;
    }

    private String generatePodcastRecommendationResponse(List<String> interests, Map<String, Object> recommendationsResult) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendations = (List<Map<String, Object>>) recommendationsResult.get("recommendations");
        int totalRecommendations = (Integer) recommendationsResult.get("totalRecommendations");
        
        return "Beep! 🎧 I found " + totalRecommendations + " great podcasts for you based on your interests in " + 
               String.join(", ", interests) + "! I think you'll really enjoy these recommendations!";
    }

    private String generateScheduleConfirmationResponse(String podcastTitle, String playTime, String playDate) {
        return "Chime! 📅 Perfect! I've scheduled '" + podcastTitle + "' to play at " + playTime + 
               (playDate != null ? " on " + playDate : " today") + ". I'll make sure you don't miss it!";
    }

    private String generateSchedulesOverviewResponse(List<Map<String, Object>> schedules) {
        if (schedules.isEmpty()) {
            return "Beep! 📅 You don't have any podcasts scheduled yet. Would you like me to help you find some great content to schedule?";
        }
        
        return "Chime! 📅 You have " + schedules.size() + " podcast(s) scheduled. I'll make sure they play at the right time for you!";
    }

    private String generateElderlyRecommendationsResponse(Map<String, Object> recommendationsResult) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recommendations = (List<Map<String, Object>>) recommendationsResult.get("recommendations");
        int totalRecommendations = (Integer) recommendationsResult.get("totalRecommendations");
        
        return "Beep! 👴👵 I found " + totalRecommendations + " wonderful podcasts specifically curated for seniors! " +
               "These are designed to be engaging, informative, and easy to follow. I think you'll love them!";
    }

    private Long getUserIdFromToken(String authHeader) {
        // TODO: Implement JWT token parsing to extract user ID
        // For now, return a default user ID
        return 1L;
    }
} 