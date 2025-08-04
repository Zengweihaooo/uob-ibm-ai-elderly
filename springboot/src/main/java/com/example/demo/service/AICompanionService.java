package com.example.demo.service;

import com.example.demo.pojo.AICompanion;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AICompanionService {

    private final Random random = new Random();

    /**
     * Update AI companion's emotional state based on interaction and neglect
     * @param companion The AI companion to update
     * @param hasInteraction Whether there was recent interaction
     */
    public void updateAIEmotion(AICompanion companion, boolean hasInteraction) {
        LocalDateTime now = LocalDateTime.now();
        
        // Update neglect level
        updateNeglectLevel(companion, now);
        
        // Update emotion based on neglect and interaction
        updateEmotionBasedOnNeglect(companion, hasInteraction);
        
        // Update energy and responsiveness
        updateEnergyAndResponsiveness(companion);
        
        // Update happiness based on interaction
        if (hasInteraction) {
            increaseHappiness(companion);
        } else {
            decreaseHappiness(companion);
        }
        
        // Update visual and sound expressions
        updateExpressions(companion);
        
        // Update activity mode
        updateActivityMode(companion);
    }

    /**
     * Update neglect level based on time since last attention
     * @param companion The AI companion to update
     * @param currentTime Current time
     */
    private void updateNeglectLevel(AICompanion companion, LocalDateTime currentTime) {
        if (companion.getLastAttentionTime() == null) {
            companion.setLastAttentionTime(currentTime);
            companion.setNeglectLevel(0);
            return;
        }

        Duration timeSinceAttention = Duration.between(companion.getLastAttentionTime(), currentTime);
        long hoursSinceAttention = timeSinceAttention.toHours();
        
        // Increase neglect level based on time
        int neglectIncrease = (int) Math.min(hoursSinceAttention * 3, 15); // Max 15 points per check
        companion.setNeglectLevel(companion.getNeglectLevel() + neglectIncrease);
        
        // Set needs attention flag if neglect level is high
        companion.setNeedsAttention(companion.getNeglectLevel() > 25);
        companion.setLonely(companion.getNeglectLevel() > 40);
    }

    /**
     * Update emotion based on neglect level and interaction
     * @param companion The AI companion to update
     * @param hasInteraction Whether there was recent interaction
     */
    private void updateEmotionBasedOnNeglect(AICompanion companion, boolean hasInteraction) {
        if (hasInteraction) {
            // Happy when getting attention
            companion.setEmotion("happy");
            companion.setNeglectLevel(Math.max(0, companion.getNeglectLevel() - 20)); // Reduce neglect
            companion.setLonely(false);
        } else {
            // Emotional state based on neglect level
            if (companion.getNeglectLevel() > 50) {
                companion.setEmotion("sad");
            } else if (companion.getNeglectLevel() > 30) {
                companion.setEmotion("anxious");
            } else if (companion.getNeglectLevel() > 10) {
                companion.setEmotion("calm");
            } else {
                companion.setEmotion("helpful");
            }
        }
    }

    /**
     * Update energy and responsiveness based on time and activity
     * @param companion The AI companion to update
     */
    private void updateEnergyAndResponsiveness(AICompanion companion) {
        // Energy naturally decreases over time but AI maintains high responsiveness
        int energyDecrease = random.nextInt(2) + 1; // 1-2 points
        companion.setEnergy(Math.max(0, companion.getEnergy() - energyDecrease));
        
        // Responsiveness stays high but can decrease slightly when neglected
        if (companion.getNeglectLevel() > 30) {
            int responsivenessDecrease = random.nextInt(3) + 1; // 1-3 points
            companion.setResponsiveness(Math.max(70, companion.getResponsiveness() - responsivenessDecrease));
        } else {
            // Responsiveness increases when active
            companion.setResponsiveness(Math.min(100, companion.getResponsiveness() + 1));
        }
    }

    /**
     * Increase happiness when there's interaction
     * @param companion The AI companion to update
     */
    private void increaseHappiness(AICompanion companion) {
        int happinessIncrease = random.nextInt(8) + 3; // 3-10 points
        companion.setHappiness(Math.min(100, companion.getHappiness() + happinessIncrease));
        companion.setLastInteraction(LocalDateTime.now());
        companion.setInteractionCount(companion.getInteractionCount() + 1);
    }

    /**
     * Decrease happiness when neglected
     * @param companion The AI companion to update
     */
    private void decreaseHappiness(AICompanion companion) {
        if (companion.getNeglectLevel() > 20) {
            int happinessDecrease = random.nextInt(2) + 1; // 1-2 points
            companion.setHappiness(Math.max(0, companion.getHappiness() - happinessDecrease));
        }
    }

    /**
     * Update visual and sound expressions based on emotion
     * @param companion The AI companion to update
     */
    private void updateExpressions(AICompanion companion) {
        String emotion = companion.getEmotion();
        String personality = companion.getPersonality();
        
        // Update sound expression
        updateSoundExpression(companion, emotion, personality);
        
        // Update visual expression
        updateVisualExpression(companion, emotion, personality);
    }

    /**
     * Update sound expression based on emotion and personality
     * @param companion The AI companion to update
     * @param emotion Current emotion
     * @param personality AI personality
     */
    private void updateSoundExpression(AICompanion companion, String emotion, String personality) {
        String sound = "silent";
        
        // Determine sound based on emotion and personality
        switch (emotion) {
            case "happy":
                sound = random.nextBoolean() ? "chime" : "notification";
                break;
            case "sad":
                sound = "low_beep";
                break;
            case "anxious":
                sound = "warning_beep";
                break;
            case "excited":
                sound = "high_chime";
                break;
            case "helpful":
                sound = random.nextBoolean() ? "beep" : "silent";
                break;
            case "calm":
                sound = "soft_beep";
                break;
        }
        
        // Add randomness to make it more realistic
        if (random.nextInt(100) < 25) { // 25% chance to make sound
            companion.setCurrentSound(sound);
            companion.setMakingSound(true);
        } else {
            companion.setCurrentSound("silent");
            companion.setMakingSound(false);
        }
    }

    /**
     * Update visual expression based on emotion and personality
     * @param companion The AI companion to update
     * @param emotion Current emotion
     * @param personality AI personality
     */
    private void updateVisualExpression(AICompanion companion, String emotion, String personality) {
        String expression = "neutral_led";
        String ledColor = "blue";
        
        // Determine visual expression based on emotion
        switch (emotion) {
            case "happy":
                expression = "happy_led";
                ledColor = "green";
                break;
            case "sad":
                expression = "sad_led";
                ledColor = "blue";
                break;
            case "anxious":
                expression = "warning_led";
                ledColor = "yellow";
                break;
            case "excited":
                expression = "excited_led";
                ledColor = "purple";
                break;
            case "helpful":
                expression = "helpful_led";
                ledColor = "green";
                break;
            case "calm":
                expression = "calm_led";
                ledColor = "white";
                break;
        }
        
        companion.setVisualExpression(expression);
        companion.setLedColor(ledColor);
        companion.setExpressingEmotion(true);
    }

    /**
     * Update activity mode based on energy and emotion
     * @param companion The AI companion to update
     */
    private void updateActivityMode(AICompanion companion) {
        String emotion = companion.getEmotion();
        int energy = companion.getEnergy();
        
        if (emotion.equals("excited") && energy > 60) {
            companion.setActivityMode("responding");
        } else if (emotion.equals("happy") && energy > 40) {
            companion.setActivityMode("listening");
        } else if (companion.getNeglectLevel() > 40) {
            companion.setActivityMode("idle");
        } else if (energy < 20) {
            companion.setActivityMode("sleeping");
        } else {
            companion.setActivityMode("listening");
        }
    }

    /**
     * Process interaction with AI companion
     * @param companion The AI companion to interact with
     * @param interactionType Type of interaction
     * @return Response message
     */
    public Map<String, Object> processInteraction(AICompanion companion, String interactionType) {
        Map<String, Object> response = new HashMap<>();
        
        // Update AI state based on interaction
        updateAIEmotion(companion, true);
        companion.setLastAttentionTime(LocalDateTime.now());
        
        String aiName = companion.getName();
        String emotion = companion.getEmotion();
        String sound = companion.getCurrentSound();
        String expression = companion.getVisualExpression();
        String ledColor = companion.getLedColor();
        
        // Generate response based on interaction type
        switch (interactionType.toLowerCase()) {
            case "chat":
            case "talk":
                companion.setLastChat(LocalDateTime.now());
                companion.setChatCount(companion.getChatCount() + 1);
                response.put("message", aiName + " is happy to chat with you! " + 
                           "LED is " + ledColor + ". " + 
                           (sound.equals("silent") ? "" : "Making " + sound + " sounds."));
                break;
            case "command":
            case "help":
                companion.setLastCommand(LocalDateTime.now());
                companion.setCurrentTask("processing_command");
                response.put("message", aiName + " is ready to help! " +
                           "Showing " + expression + " with " + ledColor + " LED.");
                break;
            case "question":
                companion.setCurrentTask("thinking");
                companion.setActivityMode("thinking");
                response.put("message", aiName + " is thinking about your question! " +
                           "LED is " + ledColor + " and " + sound + " sounds.");
                break;
            case "greet":
                response.put("message", aiName + " greets you warmly! " +
                           "LED is " + ledColor + " and showing " + expression + ".");
                break;
            default:
                response.put("message", aiName + " responds to your interaction with " + 
                           expression + " and " + ledColor + " LED.");
        }
        
        response.put("emotion", emotion);
        response.put("sound", sound);
        response.put("expression", expression);
        response.put("ledColor", ledColor);
        response.put("energy", companion.getEnergy());
        response.put("happiness", companion.getHappiness());
        response.put("responsiveness", companion.getResponsiveness());
        response.put("needsAttention", companion.isNeedsAttention());
        response.put("isLonely", companion.isLonely());
        response.put("currentTask", companion.getCurrentTask());
        
        return response;
    }

    /**
     * Get AI companion's current emotional state summary
     * @param companion The AI companion to analyze
     * @return Emotional state summary
     */
    public Map<String, Object> getEmotionalState(AICompanion companion) {
        Map<String, Object> state = new HashMap<>();
        
        state.put("emotion", companion.getEmotion());
        state.put("happiness", companion.getHappiness());
        state.put("energy", companion.getEnergy());
        state.put("responsiveness", companion.getResponsiveness());
        state.put("neglectLevel", companion.getNeglectLevel());
        state.put("needsAttention", companion.isNeedsAttention());
        state.put("isLonely", companion.isLonely());
        state.put("currentSound", companion.getCurrentSound());
        state.put("visualExpression", companion.getVisualExpression());
        state.put("ledColor", companion.getLedColor());
        state.put("isActive", companion.isActive());
        state.put("activityMode", companion.getActivityMode());
        state.put("currentLocation", companion.getCurrentLocation());
        state.put("currentTask", companion.getCurrentTask());
        state.put("helpfulness", companion.getHelpfulness());
        
        // Generate emotional description
        String description = generateEmotionalDescription(companion);
        state.put("description", description);
        
        return state;
    }

    /**
     * Generate human-readable emotional description
     * @param companion The AI companion to describe
     * @return Emotional description
     */
    private String generateEmotionalDescription(AICompanion companion) {
        String name = companion.getName();
        String emotion = companion.getEmotion();
        String sound = companion.getCurrentSound();
        String expression = companion.getVisualExpression();
        String ledColor = companion.getLedColor();
        String activityMode = companion.getActivityMode();
        
        StringBuilder description = new StringBuilder();
        description.append(name).append(" is currently feeling ").append(emotion).append(". ");
        
        if (!sound.equals("silent")) {
            description.append("The AI is making ").append(sound).append(" sounds. ");
        }
        
        description.append("Visually, ").append(name).append(" is showing ").append(expression).append(" with ").append(ledColor).append(" LED. ");
        
        if (companion.isNeedsAttention()) {
            description.append(name).append(" seems to need some interaction and attention. ");
        }
        
        if (companion.isLonely()) {
            description.append(name).append(" is feeling lonely and would appreciate some company. ");
        }
        
        description.append(name).append(" is currently ").append(activityMode).append(" and ready to help.");
        
        return description.toString();
    }
} 