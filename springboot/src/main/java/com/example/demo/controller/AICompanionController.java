package com.example.demo.controller;

import com.example.demo.pojo.AICompanion;
import com.example.demo.service.AICompanionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-companion")
@CrossOrigin(origins = "*")
public class AICompanionController {

    @Autowired
    private AICompanionService aiCompanionService;

    // In-memory storage for demo purposes (in production, use database)
    private Map<Long, AICompanion> companions = new HashMap<>();

    /**
     * Get AI companion's current emotional state
     * @param authHeader Authorization token
     * @return AI companion's emotional state and expressions
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getAICompanionState(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Update AI companion's emotional state (no interaction)
            aiCompanionService.updateAIEmotion(companion, false);
            
            // Get emotional state summary
            Map<String, Object> emotionalState = aiCompanionService.getEmotionalState(companion);
            
            response.put("success", true);
            response.put("companion", emotionalState);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving AI companion state: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Interact with AI companion and get emotional response
     * @param interactionData Interaction details
     * @param authHeader Authorization token
     * @return AI companion's response and updated emotional state
     */
    @PostMapping("/interact")
    public ResponseEntity<Map<String, Object>> interactWithAICompanion(
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
            
            AICompanion companion = getCompanionForUser(userId);
            
            // Process interaction and get response
            Map<String, Object> interactionResponse = aiCompanionService.processInteraction(companion, interactionType);
            
            // Update AI companion's emotional state (with interaction)
            aiCompanionService.updateAIEmotion(companion, true);
            
            response.put("success", true);
            response.put("interaction", interactionResponse);
            response.put("companion", aiCompanionService.getEmotionalState(companion));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing interaction: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get AI companion's current expressions (sound and visual)
     * @param authHeader Authorization token
     * @return Current expressions
     */
    @GetMapping("/expressions")
    public ResponseEntity<Map<String, Object>> getAICompanionExpressions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Update AI companion's emotional state
            aiCompanionService.updateAIEmotion(companion, false);
            
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
            response.put("message", "Error retrieving AI companion expressions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Check if AI companion needs attention
     * @param authHeader Authorization token
     * @return Attention status
     */
    @GetMapping("/attention-check")
    public ResponseEntity<Map<String, Object>> checkAICompanionAttention(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Update AI companion's emotional state
            aiCompanionService.updateAIEmotion(companion, false);
            
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
            response.put("message", "Error checking AI companion attention: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get AI companion's activity and status
     * @param authHeader Authorization token
     * @return Activity and status information
     */
    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getAICompanionActivity(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Update AI companion's emotional state
            aiCompanionService.updateAIEmotion(companion, false);
            
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
            response.put("message", "Error retrieving AI companion activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Put AI companion to sleep mode
     * @param authHeader Authorization token
     * @return Updated activity status
     */
    @PostMapping("/sleep")
    public ResponseEntity<Map<String, Object>> putAICompanionToSleep(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Put AI companion to sleep
            companion.setActivityMode("sleeping");
            companion.setCurrentLocation("sleep_mode");
            companion.setCurrentSound("silent");
            companion.setLedColor("dim_blue");
            companion.setVisualExpression("sleep_led");
            
            // Update emotional state
            aiCompanionService.updateAIEmotion(companion, false);
            
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
            response.put("message", "Error putting AI companion to sleep: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Wake up AI companion
     * @param authHeader Authorization token
     * @return Updated activity status
     */
    @PostMapping("/wake")
    public ResponseEntity<Map<String, Object>> wakeAICompanion(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        Long userId = getUserIdFromToken(authHeader);
        
        try {
            AICompanion companion = getCompanionForUser(userId);
            
            // Wake up AI companion
            companion.setActivityMode("listening");
            companion.setCurrentLocation("home_screen");
            companion.setCurrentSound("wake_chime");
            companion.setLedColor("green");
            companion.setVisualExpression("happy_led");
            
            // Update emotional state
            aiCompanionService.updateAIEmotion(companion, true);
            
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
            response.put("message", "Error waking AI companion: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // Helper methods
    private AICompanion getCompanionForUser(Long userId) {
        return companions.computeIfAbsent(userId, id -> createDefaultCompanion(id));
    }

    private AICompanion createDefaultCompanion(Long userId) {
        AICompanion companion = new AICompanion(userId, "Alexa", "friendly", "assistant");
        companion.setId(userId);
        companion.setLastAttentionTime(LocalDateTime.now());
        return companion;
    }

    private Long getUserIdFromToken(String authHeader) {
        // TODO: Extract real userId from JWT token
        // For demo purposes, return a default userId
        return 1L;
    }

    private String generateAttentionMessage(AICompanion companion) {
        if (companion.isNeedsAttention()) {
            return companion.getName() + " needs attention! The AI is feeling " + 
                   companion.getEmotion() + " and has a neglect level of " + companion.getNeglectLevel() + 
                   ". Please interact with " + companion.getName() + " soon.";
        } else {
            return companion.getName() + " is content and doesn't need immediate attention.";
        }
    }

    private String generateActivityDescription(AICompanion companion) {
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