package com.example.demo.controller;

import com.example.demo.pojo.EmotionCompanion;
import com.example.demo.service.EmotionCompanionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/emotion-companion")
@CrossOrigin(origins = "*")
public class EmotionCompanionController {

    @Autowired
    private EmotionCompanionService emotionCompanionService;

    // In-memory storage for demo purposes (in production, use database)
    private Map<Long, EmotionCompanion> companions = new HashMap<>();

    /**
     * Get emotion companion's current emotional state
     * @param authHeader Authorization token
     * @return Emotion companion's emotional state and expressions
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionState(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Update emotion companion's emotional state (no interaction)
            emotionCompanionService.updateEmotionCompanionEmotion(companion, false);
            
            // Get emotional state summary
            Map<String, Object> emotionalState = emotionCompanionService.getEmotionalState(companion);
            
            response.put("success", true);
            response.put("companion", emotionalState);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving emotion companion state: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Interact with emotion companion and get emotional response
     * @param interactionData Interaction details
     * @param authHeader Authorization token
     * @return Emotion companion's response and updated emotional state
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
            
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Process interaction and get response
            Map<String, Object> interactionResponse = emotionCompanionService.processInteraction(companion, interactionType);
            
            // Update emotion companion's emotional state (with interaction)
            emotionCompanionService.updateEmotionCompanionEmotion(companion, true);
            
            response.put("success", true);
            response.put("interaction", interactionResponse);
            response.put("companion", emotionCompanionService.getEmotionalState(companion));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get emotion companion's current expressions (sound and visual)
     * @param authHeader Authorization token
     * @return Current expressions
     */
    @GetMapping("/expressions")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionExpressions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Update emotion companion's emotional state
            emotionCompanionService.updateEmotionCompanionEmotion(companion, false);
            
            Map<String, Object> expressions = new HashMap<>();
            expressions.put("sound", companion.getCurrentSound());
            expressions.put("isMakingSound", companion.isMakingSound());
            expressions.put("visualExpression", companion.getVisualExpression());
            expressions.put("isExpressingEmotion", companion.isExpressingEmotion());
            expressions.put("ledColor", companion.getLedColor());
            expressions.put("activityMode", companion.getActivityMode());
            expressions.put("isActive", companion.isActive());
            expressions.put("location", companion.getCurrentLocation());
            
            response.put("success", true);
            response.put("expressions", expressions);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving emotion companion expressions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check if emotion companion needs attention
     * @param authHeader Authorization token
     * @return Attention status
     */
    @GetMapping("/attention-check")
    public ResponseEntity<Map<String, Object>> checkEmotionCompanionAttention(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Update emotion companion's emotional state
            emotionCompanionService.updateEmotionCompanionEmotion(companion, false);
            
            Map<String, Object> attentionStatus = new HashMap<>();
            attentionStatus.put("needsAttention", companion.isNeedsAttention());
            attentionStatus.put("isLonely", companion.isLonely());
            attentionStatus.put("neglectLevel", companion.getNeglectLevel());
            attentionStatus.put("lastAttentionTime", companion.getLastAttentionTime());
            attentionStatus.put("emotion", companion.getEmotion());
            attentionStatus.put("happiness", companion.getHappiness());
            attentionStatus.put("responsiveness", companion.getResponsiveness());
            
            // Generate attention message
            String attentionMessage = generateAttentionMessage(companion);
            attentionStatus.put("message", attentionMessage);
            
            response.put("success", true);
            response.put("attentionStatus", attentionStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error checking emotion companion attention: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get emotion companion's activity and status
     * @param authHeader Authorization token
     * @return Activity and status information
     */
    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Update emotion companion's emotional state
            emotionCompanionService.updateEmotionCompanionEmotion(companion, false);
            
            Map<String, Object> activityInfo = new HashMap<>();
            activityInfo.put("isActive", companion.isActive());
            activityInfo.put("activityMode", companion.getActivityMode());
            activityInfo.put("currentLocation", companion.getCurrentLocation());
            activityInfo.put("currentTask", companion.getCurrentTask());
            activityInfo.put("energy", companion.getEnergy());
            activityInfo.put("responsiveness", companion.getResponsiveness());
            activityInfo.put("emotion", companion.getEmotion());
            activityInfo.put("helpfulness", companion.getHelpfulness());
            
            // Generate activity description
            String activityDescription = generateActivityDescription(companion);
            activityInfo.put("description", activityDescription);
            
            response.put("success", true);
            response.put("activity", activityInfo);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving emotion companion activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Put emotion companion to sleep mode
     * @param authHeader Authorization token
     * @return Updated activity status
     */
    @PostMapping("/sleep")
    public ResponseEntity<Map<String, Object>> putEmotionCompanionToSleep(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Put emotion companion to sleep
            companion.setActivityMode("sleeping");
            companion.setCurrentLocation("sleep_mode");
            companion.setCurrentSound("silent");
            companion.setLedColor("dim_blue");
            companion.setVisualExpression("sleep_led");
            
            // Update emotional state
            emotionCompanionService.updateEmotionCompanionEmotion(companion, false);
            
            response.put("success", true);
            response.put("message", companion.getName() + " is now in sleep mode.");
            response.put("activity", Map.of(
                "isActive", companion.isActive(),
                "activityMode", companion.getActivityMode(),
                "currentLocation", companion.getCurrentLocation(),
                "ledColor", companion.getLedColor()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error putting emotion companion to sleep: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Wake up emotion companion
     * @param authHeader Authorization token
     * @return Updated activity status
     */
    @PostMapping("/wake")
    public ResponseEntity<Map<String, Object>> wakeEmotionCompanion(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            EmotionCompanion companion = getCompanionForUser(userId);
            
            // Wake up emotion companion
            companion.setActivityMode("listening");
            companion.setCurrentLocation("home_screen");
            companion.setCurrentSound("wake_chime");
            companion.setLedColor("green");
            companion.setVisualExpression("happy_led");
            
            // Update emotional state
            emotionCompanionService.updateEmotionCompanionEmotion(companion, true);
            
            response.put("success", true);
            response.put("message", companion.getName() + " is now awake and ready to help!");
            response.put("activity", Map.of(
                "isActive", companion.isActive(),
                "activityMode", companion.getActivityMode(),
                "currentLocation", companion.getCurrentLocation(),
                "ledColor", companion.getLedColor()
            ));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error waking emotion companion: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Helper methods
    private EmotionCompanion getCompanionForUser(Long userId) {
        return companions.computeIfAbsent(userId, id -> createDefaultCompanion(id));
    }

    private EmotionCompanion createDefaultCompanion(Long userId) {
        EmotionCompanion companion = new EmotionCompanion(userId, "Alexa", "friendly", "assistant");
        companion.setId(userId);
        companion.setLastAttentionTime(LocalDateTime.now());
        return companion;
    }

    private Long getUserIdFromToken(String authHeader) {
        // TODO: Extract real userId from JWT token
        // For demo purposes, return a default userId
        return 1L;
    }

    private String generateAttentionMessage(EmotionCompanion companion) {
        if (companion.isNeedsAttention()) {
            return companion.getName() + " needs attention! The AI is feeling " + 
                   companion.getEmotion() + " and has a neglect level of " + companion.getNeglectLevel() + 
                   ". Please interact with " + companion.getName() + " soon.";
        } else {
            return companion.getName() + " is content and doesn't need immediate attention.";
        }
    }

    private String generateActivityDescription(EmotionCompanion companion) {
        String name = companion.getName();
        String activityMode = companion.getActivityMode();
        String location = companion.getCurrentLocation();
        String currentTask = companion.getCurrentTask();
        
        if (companion.isActive()) {
            return name + " is " + activityMode + " in " + location + " mode. " +
                   "Current task: " + currentTask + ".";
        } else {
            return name + " is currently inactive in " + location + " mode.";
        }
    }
} 