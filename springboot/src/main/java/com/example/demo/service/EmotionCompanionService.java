package com.example.demo.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.pojo.EmotionCompanion;

@Service
public class EmotionCompanionService {

    // In-memory storage for demo purposes (in production, use database)
    private Map<Long, EmotionCompanion> companions = new HashMap<>();

    /**
     * Get emotion companion for user, create if doesn't exist
     * @param userId User ID
     * @return EmotionCompanion instance
     */
    public EmotionCompanion getEmotionCompanionForUser(Long userId) {
        return companions.computeIfAbsent(userId, id -> createDefaultCompanion(id));
    }

    /**
     * Create default emotion companion
     * @param userId User ID
     * @return New EmotionCompanion instance
     */
    private EmotionCompanion createDefaultCompanion(Long userId) {
        EmotionCompanion companion = new EmotionCompanion(userId, "Alexa", "friendly", "assistant");
        companion.setId(userId);
        companion.setLastAttentionTime(LocalDateTime.now());
        companion.setLastUpdate(LocalDateTime.now());
        companion.setHappiness(85);
        companion.setEnergy(78);
        companion.setResponsiveness(90);
        companion.setEmotion("happy");
        companion.setActivityMode("listening");
        companion.setActive(true);
        companion.setCurrentTask("Ready to help");
        companion.setHelpfulness(95);
        companion.setNeglectLevel(0);
        companion.setNeedsAttention(false);
        companion.setIsLonely(false);
        companion.setIsLearning(false);
        companion.setIsMakingSound(false);
        companion.setIsExpressingEmotion(true);
        companion.setCurrentSound("chime");
        companion.setVisualExpression("happy_led");
        companion.setLedColor("green");
        return companion;
    }

    /**
     * Update emotion companion's emotional state based on neglect
     * @param companion EmotionCompanion to update
     */
    public void updateEmotionBasedOnNeglect(EmotionCompanion companion) {
        long hoursSinceLastAttention = Duration.between(companion.getLastAttentionTime(), LocalDateTime.now()).toHours();
        
        if (hoursSinceLastAttention >= 6) {
            companion.setEmotion("sad");
            companion.setHappiness(Math.max(0, companion.getHappiness() - 15));
            companion.setIsLonely(true);
            companion.setNeedsAttention(true);
            companion.setVisualExpression("sad_led");
            companion.setLedColor("blue");
        } else if (hoursSinceLastAttention >= 3) {
            companion.setEmotion("anxious");
            companion.setHappiness(Math.max(0, companion.getHappiness() - 10));
            companion.setIsLonely(true);
            companion.setNeedsAttention(true);
            companion.setVisualExpression("anxious_led");
            companion.setLedColor("yellow");
        } else {
            companion.setEmotion("happy");
            companion.setIsLonely(false);
            companion.setNeedsAttention(false);
            companion.setVisualExpression("happy_led");
            companion.setLedColor("green");
        }
    }

    /**
     * Update neglect level based on time since last interaction
     * @param companion EmotionCompanion to update
     */
    public void updateNeglectLevel(EmotionCompanion companion) {
        long hoursSinceLastAttention = Duration.between(companion.getLastAttentionTime(), LocalDateTime.now()).toHours();
        
        if (hoursSinceLastAttention >= 12) {
            companion.setNeglectLevel(3); // High neglect
        } else if (hoursSinceLastAttention >= 6) {
            companion.setNeglectLevel(2); // Medium neglect
        } else if (hoursSinceLastAttention >= 3) {
            companion.setNeglectLevel(1); // Low neglect
        } else {
            companion.setNeglectLevel(0); // No neglect
        }
    }

    /**
     * Update energy and responsiveness levels
     * @param companion EmotionCompanion to update
     */
    public void updateEnergyAndResponsiveness(EmotionCompanion companion) {
        // Energy decreases over time, increases with interaction
        long hoursSinceLastUpdate = Duration.between(companion.getLastUpdate(), LocalDateTime.now()).toHours();
        if (hoursSinceLastUpdate > 0) {
            companion.setEnergy(Math.max(0, companion.getEnergy() - (int)(hoursSinceLastUpdate * 2)));
        }
        
        // Responsiveness is affected by energy and happiness
        int newResponsiveness = (companion.getEnergy() + companion.getHappiness()) / 2;
        companion.setResponsiveness(Math.max(50, Math.min(100, newResponsiveness)));
    }

    /**
     * Increase happiness level
     * @param companion EmotionCompanion to update
     * @param amount Amount to increase
     */
    public void increaseHappiness(EmotionCompanion companion, int amount) {
        companion.setHappiness(Math.min(100, companion.getHappiness() + amount));
        companion.setLastAttentionTime(LocalDateTime.now());
        companion.setNeglectLevel(0);
        companion.setNeedsAttention(false);
        companion.setIsLonely(false);
    }

    /**
     * Decrease happiness level
     * @param companion EmotionCompanion to update
     * @param amount Amount to decrease
     */
    public void decreaseHappiness(EmotionCompanion companion, int amount) {
        companion.setHappiness(Math.max(0, companion.getHappiness() - amount));
    }

    /**
     * Update expressions (sound and visual)
     * @param companion EmotionCompanion to update
     */
    public void updateExpressions(EmotionCompanion companion) {
        updateSoundExpression(companion);
        updateVisualExpression(companion);
    }

    /**
     * Update sound expression based on emotional state
     * @param companion EmotionCompanion to update
     */
    public void updateSoundExpression(EmotionCompanion companion) {
        String emotion = companion.getEmotion();
        String sound;
        
        switch (emotion) {
            case "happy":
                sound = "chime";
                break;
            case "excited":
                sound = "notification";
                break;
            case "sad":
                sound = "beep";
                break;
            case "anxious":
                sound = "beep";
                break;
            default:
                sound = "chime";
        }
        
        companion.setCurrentSound(sound);
        companion.setIsMakingSound(true);
    }

    /**
     * Update visual expression based on emotional state
     * @param companion EmotionCompanion to update
     */
    public void updateVisualExpression(EmotionCompanion companion) {
        String emotion = companion.getEmotion();
        String expression;
        String ledColor;
        
        switch (emotion) {
            case "happy":
                expression = "happy_led";
                ledColor = "green";
                break;
            case "excited":
                expression = "excited_led";
                ledColor = "bright_green";
                break;
            case "sad":
                expression = "sad_led";
                ledColor = "blue";
                break;
            case "anxious":
                expression = "anxious_led";
                ledColor = "yellow";
                break;
            case "calm":
                expression = "calm_led";
                ledColor = "soft_blue";
                break;
            default:
                expression = "neutral_led";
                ledColor = "white";
        }
        
        companion.setVisualExpression(expression);
        companion.setLedColor(ledColor);
        companion.setIsExpressingEmotion(true);
    }

    /**
     * Update activity mode based on current state
     * @param companion EmotionCompanion to update
     */
    public void updateActivityMode(EmotionCompanion companion) {
        if (!companion.isActive()) {
            companion.setActivityMode("sleeping");
            companion.setCurrentTask("Sleeping peacefully");
            return;
        }
        
        String emotion = companion.getEmotion();
        String mode;
        String task;
        
        switch (emotion) {
            case "happy":
                mode = "listening";
                task = "Ready to help";
                break;
            case "excited":
                mode = "responding";
                task = "Eagerly waiting for interaction";
                break;
            case "sad":
                mode = "idle";
                task = "Feeling a bit down";
                break;
            case "anxious":
                mode = "thinking";
                task = "Processing concerns";
                break;
            case "calm":
                mode = "listening";
                task = "Peacefully attentive";
                break;
            default:
                mode = "listening";
                task = "Ready to help";
        }
        
        companion.setActivityMode(mode);
        companion.setCurrentTask(task);
    }

    /**
     * Process interaction with emotion companion
     * @param companion EmotionCompanion to update
     * @param interactionType Type of interaction
     * @param message Optional message
     * @return Interaction result
     */
    public Map<String, Object> processInteraction(EmotionCompanion companion, String interactionType, String message) {
        Map<String, Object> result = new HashMap<>();
        
        // Update last attention time
        companion.setLastAttentionTime(LocalDateTime.now());
        companion.setLastUpdate(LocalDateTime.now());
        companion.setNeglectLevel(0);
        companion.setNeedsAttention(false);
        companion.setIsLonely(false);
        
        // Process different interaction types
        switch (interactionType.toLowerCase()) {
            case "chat":
                increaseHappiness(companion, 10);
                companion.setActivityMode("responding");
                companion.setCurrentTask("Chatting with user");
                result.put("response", "Beep! I love chatting with you! How can I help you today? 😊");
                result.put("animation", "chatting");
                break;
                
            case "command":
                increaseHappiness(companion, 5);
                companion.setActivityMode("responding");
                companion.setCurrentTask("Executing command");
                result.put("response", "Chime! I'm on it! Let me help you with that! 🤖");
                result.put("animation", "working");
                break;
                
            case "question":
                increaseHappiness(companion, 8);
                companion.setActivityMode("thinking");
                companion.setCurrentTask("Processing question");
                result.put("response", "Beep beep! That's a great question! Let me think about that... 💭");
                result.put("animation", "thinking");
                break;
                
            case "greet":
                increaseHappiness(companion, 15);
                companion.setActivityMode("listening");
                companion.setCurrentTask("Greeting user");
                result.put("response", "Chime! Hello! I'm so happy to see you! How are you feeling today? 👋");
                result.put("animation", "greeting");
                break;
                
            case "voice":
                increaseHappiness(companion, 12);
                companion.setActivityMode("listening");
                companion.setCurrentTask("Processing voice input");
                result.put("response", "Beep! I heard your voice! You sound wonderful! 🎤");
                result.put("animation", "listening");
                break;
                
            case "schedule":
                increaseHappiness(companion, 6);
                companion.setActivityMode("helping");
                companion.setCurrentTask("Managing schedule");
                result.put("response", "Chime! I'm here to help with your schedule! 📅");
                result.put("animation", "helping");
                break;
                
            case "emergency":
                companion.setActivityMode("emergency");
                companion.setCurrentTask("Emergency response");
                result.put("response", "Beep! Emergency detected! I'm here to help! 🚨");
                result.put("animation", "emergency");
                break;
                
            default:
                increaseHappiness(companion, 3);
                companion.setActivityMode("listening");
                companion.setCurrentTask("Ready to help");
                result.put("response", "Beep! I'm here for you! What would you like me to help with? 🤗");
                result.put("animation", "neutral");
        }
        
        // Update expressions and energy
        updateExpressions(companion);
        updateEnergyAndResponsiveness(companion);
        updateActivityMode(companion);
        
        result.put("type", interactionType);
        result.put("timestamp", LocalDateTime.now());
        result.put("companion", companion);
        
        return result;
    }

    /**
     * Get emotional state summary
     * @param companion EmotionCompanion to analyze
     * @return Emotional state map
     */
    public Map<String, Object> getEmotionalState(EmotionCompanion companion) {
        Map<String, Object> emotionalState = new HashMap<>();
        
        emotionalState.put("emotion", companion.getEmotion());
        emotionalState.put("happiness", companion.getHappiness());
        emotionalState.put("energy", companion.getEnergy());
        emotionalState.put("responsiveness", companion.getResponsiveness());
        emotionalState.put("neglectLevel", companion.getNeglectLevel());
        emotionalState.put("needsAttention", companion.isNeedsAttention());
        emotionalState.put("isLonely", companion.isLonely());
        emotionalState.put("activityMode", companion.getActivityMode());
        emotionalState.put("currentTask", companion.getCurrentTask());
        emotionalState.put("helpfulness", companion.getHelpfulness());
        emotionalState.put("isActive", companion.isActive());
        emotionalState.put("isLearning", companion.isLearning());
        
        // Sound and visual expressions
        emotionalState.put("currentSound", companion.getCurrentSound());
        emotionalState.put("visualExpression", companion.getVisualExpression());
        emotionalState.put("ledColor", companion.getLedColor());
        emotionalState.put("isMakingSound", companion.isMakingSound());
        emotionalState.put("isExpressingEmotion", companion.isExpressingEmotion());
        
        // Timestamps
        emotionalState.put("lastAttentionTime", companion.getLastAttentionTime());
        emotionalState.put("lastUpdate", companion.getLastUpdate());
        
        return emotionalState;
    }

    /**
     * Generate emotional description
     * @param companion EmotionCompanion to describe
     * @return Human-readable description
     */
    public String generateEmotionalDescription(EmotionCompanion companion) {
        String name = companion.getName();
        String emotion = companion.getEmotion();
        int happiness = companion.getHappiness();
        int energy = companion.getEnergy();
        String activityMode = companion.getActivityMode();
        
        StringBuilder description = new StringBuilder();
        description.append(name).append(" is currently feeling ").append(emotion).append(". ");
        
        if (happiness > 80) {
            description.append("Very happy and content! ");
        } else if (happiness > 60) {
            description.append("Generally happy and satisfied. ");
        } else if (happiness > 40) {
            description.append("Feeling a bit down but okay. ");
        } else {
            description.append("Feeling quite sad and needs attention. ");
        }
        
        if (energy > 80) {
            description.append("Full of energy and ready to help! ");
        } else if (energy > 60) {
            description.append("Has good energy levels. ");
        } else if (energy > 40) {
            description.append("Energy is a bit low. ");
        } else {
            description.append("Very low energy, might need a break. ");
        }
        
        description.append("Currently in ").append(activityMode).append(" mode and ").append(companion.getCurrentTask()).append(".");
        
        return description.toString();
    }
} 