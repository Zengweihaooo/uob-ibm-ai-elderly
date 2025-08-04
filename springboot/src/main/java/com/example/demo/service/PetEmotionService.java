package com.example.demo.service;

import com.example.demo.pojo.Pet;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class PetEmotionService {

    private final Random random = new Random();

    /**
     * Update pet's emotional state based on interaction and neglect
     * @param pet The pet to update
     * @param hasInteraction Whether there was recent interaction
     */
    public void updatePetEmotion(Pet pet, boolean hasInteraction) {
        LocalDateTime now = LocalDateTime.now();
        
        // Update neglect level
        updateNeglectLevel(pet, now);
        
        // Update emotion based on neglect and interaction
        updateEmotionBasedOnNeglect(pet, hasInteraction);
        
        // Update energy level
        updateEnergyLevel(pet);
        
        // Update happiness based on interaction
        if (hasInteraction) {
            increaseHappiness(pet);
        } else {
            decreaseHappiness(pet);
        }
        
        // Update visual and sound expressions
        updateExpressions(pet);
        
        // Update movement behavior
        updateMovement(pet);
    }

    /**
     * Update neglect level based on time since last attention
     * @param pet The pet to update
     * @param currentTime Current time
     */
    private void updateNeglectLevel(Pet pet, LocalDateTime currentTime) {
        if (pet.getLastAttentionTime() == null) {
            pet.setLastAttentionTime(currentTime);
            pet.setNeglectLevel(0);
            return;
        }

        Duration timeSinceAttention = Duration.between(pet.getLastAttentionTime(), currentTime);
        long hoursSinceAttention = timeSinceAttention.toHours();
        
        // Increase neglect level based on time
        int neglectIncrease = (int) Math.min(hoursSinceAttention * 5, 20); // Max 20 points per check
        pet.setNeglectLevel(pet.getNeglectLevel() + neglectIncrease);
        
        // Set needs attention flag if neglect level is high
        pet.setNeedsAttention(pet.getNeglectLevel() > 30);
    }

    /**
     * Update emotion based on neglect level and interaction
     * @param pet The pet to update
     * @param hasInteraction Whether there was recent interaction
     */
    private void updateEmotionBasedOnNeglect(Pet pet, boolean hasInteraction) {
        if (hasInteraction) {
            // Happy when getting attention
            pet.setEmotion("happy");
            pet.setNeglectLevel(Math.max(0, pet.getNeglectLevel() - 15)); // Reduce neglect
        } else {
            // Emotional state based on neglect level
            if (pet.getNeglectLevel() > 50) {
                pet.setEmotion("sad");
            } else if (pet.getNeglectLevel() > 30) {
                pet.setEmotion("anxious");
            } else if (pet.getNeglectLevel() > 10) {
                pet.setEmotion("calm");
            } else {
                pet.setEmotion("happy");
            }
        }
    }

    /**
     * Update energy level based on time and activity
     * @param pet The pet to update
     */
    private void updateEnergyLevel(Pet pet) {
        // Energy naturally decreases over time
        int energyDecrease = random.nextInt(3) + 1; // 1-3 points
        pet.setEnergy(Math.max(0, pet.getEnergy() - energyDecrease));
        
        // Energy increases when happy and active
        if (pet.getEmotion().equals("happy") && pet.isMoving()) {
            pet.setEnergy(Math.min(100, pet.getEnergy() + 2));
        }
    }

    /**
     * Increase happiness when there's interaction
     * @param pet The pet to update
     */
    private void increaseHappiness(Pet pet) {
        int happinessIncrease = random.nextInt(10) + 5; // 5-15 points
        pet.setHappiness(Math.min(100, pet.getHappiness() + happinessIncrease));
        pet.setLastInteraction(LocalDateTime.now());
        pet.setInteractionCount(pet.getInteractionCount() + 1);
    }

    /**
     * Decrease happiness when neglected
     * @param pet The pet to update
     */
    private void decreaseHappiness(Pet pet) {
        if (pet.getNeglectLevel() > 20) {
            int happinessDecrease = random.nextInt(3) + 1; // 1-3 points
            pet.setHappiness(Math.max(0, pet.getHappiness() - happinessDecrease));
        }
    }

    /**
     * Update visual and sound expressions based on emotion
     * @param pet The pet to update
     */
    private void updateExpressions(Pet pet) {
        String emotion = pet.getEmotion();
        String petType = pet.getType();
        
        // Update sound expression
        updateSoundExpression(pet, emotion, petType);
        
        // Update visual expression
        updateVisualExpression(pet, emotion, petType);
    }

    /**
     * Update sound expression based on emotion and pet type
     * @param pet The pet to update
     * @param emotion Current emotion
     * @param petType Type of pet
     */
    private void updateSoundExpression(Pet pet, String emotion, String petType) {
        String sound = "silent";
        
        // Determine sound based on emotion and pet type
        switch (emotion) {
            case "happy":
                if ("dog".equals(petType)) {
                    sound = random.nextBoolean() ? "barking" : "tail_wagging_sound";
                } else if ("cat".equals(petType)) {
                    sound = random.nextBoolean() ? "purring" : "meowing";
                }
                break;
            case "sad":
                if ("dog".equals(petType)) {
                    sound = "whining";
                } else if ("cat".equals(petType)) {
                    sound = "sad_meowing";
                }
                break;
            case "anxious":
                if ("dog".equals(petType)) {
                    sound = "nervous_barking";
                } else if ("cat".equals(petType)) {
                    sound = "anxious_meowing";
                }
                break;
            case "excited":
                if ("dog".equals(petType)) {
                    sound = "excited_barking";
                } else if ("cat".equals(petType)) {
                    sound = "excited_meowing";
                }
                break;
        }
        
        // Add randomness to make it more realistic
        if (random.nextInt(100) < 30) { // 30% chance to make sound
            pet.setCurrentSound(sound);
            pet.setMakingSound(true);
        } else {
            pet.setCurrentSound("silent");
            pet.setMakingSound(false);
        }
    }

    /**
     * Update visual expression based on emotion and pet type
     * @param pet The pet to update
     * @param emotion Current emotion
     * @param petType Type of pet
     */
    private void updateVisualExpression(Pet pet, String emotion, String petType) {
        String expression = "neutral";
        
        // Determine visual expression based on emotion and pet type
        switch (emotion) {
            case "happy":
                if ("dog".equals(petType)) {
                    expression = "tail_wagging";
                } else if ("cat".equals(petType)) {
                    expression = "bright_eyes";
                }
                break;
            case "sad":
                if ("dog".equals(petType)) {
                    expression = "droopy_ears";
                } else if ("cat".equals(petType)) {
                    expression = "droopy_ears";
                }
                break;
            case "anxious":
                if ("dog".equals(petType)) {
                    expression = "ears_back";
                } else if ("cat".equals(petType)) {
                    expression = "flattened_ears";
                }
                break;
            case "excited":
                if ("dog".equals(petType)) {
                    expression = "perked_ears";
                } else if ("cat".equals(petType)) {
                    expression = "alert_eyes";
                }
                break;
            case "calm":
                expression = "relaxed_posture";
                break;
        }
        
        pet.setVisualExpression(expression);
        pet.setExpressingEmotion(true);
    }

    /**
     * Update movement behavior based on energy and emotion
     * @param pet The pet to update
     */
    private void updateMovement(Pet pet) {
        // Pet should not move around the house randomly
        // Only move when there's interaction or specific triggers
        
        if (pet.getEmotion().equals("excited") && pet.getEnergy() > 50) {
            // Move when excited and has energy
            pet.setMoving(true);
            pet.setMovementType("running");
        } else if (pet.getEmotion().equals("happy") && pet.getEnergy() > 30) {
            // Gentle movement when happy
            pet.setMoving(true);
            pet.setMovementType("walking");
        } else if (pet.getNeglectLevel() > 40) {
            // Restless movement when neglected
            pet.setMoving(true);
            pet.setMovementType("pacing");
        } else {
            // Stay in place when calm or tired
            pet.setMoving(false);
            pet.setMovementType("sitting");
        }
    }

    /**
     * Process interaction with pet
     * @param pet The pet to interact with
     * @param interactionType Type of interaction
     * @return Response message
     */
    public Map<String, Object> processInteraction(Pet pet, String interactionType) {
        Map<String, Object> response = new HashMap<>();
        
        // Update pet state based on interaction
        updatePetEmotion(pet, true);
        pet.setLastAttentionTime(LocalDateTime.now());
        
        String petName = pet.getName();
        String emotion = pet.getEmotion();
        String sound = pet.getCurrentSound();
        String expression = pet.getVisualExpression();
        
        // Generate response based on interaction type
        switch (interactionType.toLowerCase()) {
            case "pet":
            case "stroke":
                response.put("message", petName + " is " + emotion + "! " + 
                           (sound.equals("silent") ? "" : "Making " + sound + " sounds. ") +
                           "Showing " + expression + ".");
                break;
            case "play":
                pet.setEmotion("excited");
                pet.setEnergy(Math.min(100, pet.getEnergy() + 10));
                response.put("message", petName + " is excited to play! " +
                           "Energy increased. " + pet.getCurrentSound() + " sounds.");
                break;
            case "feed":
                pet.setHealth(Math.min(100, pet.getHealth() + 5));
                response.put("message", petName + " is happy to be fed! " +
                           "Health improved. " + expression + " expression.");
                break;
            case "talk":
                response.put("message", petName + " is listening attentively! " +
                           "Shows " + expression + " and " + 
                           (sound.equals("silent") ? "stays quiet" : "responds with " + sound));
                break;
            default:
                response.put("message", petName + " responds to your attention with " + 
                           expression + " and " + sound + " sounds.");
        }
        
        response.put("emotion", emotion);
        response.put("sound", sound);
        response.put("expression", expression);
        response.put("energy", pet.getEnergy());
        response.put("happiness", pet.getHappiness());
        response.put("needsAttention", pet.isNeedsAttention());
        
        return response;
    }

    /**
     * Get pet's current emotional state summary
     * @param pet The pet to analyze
     * @return Emotional state summary
     */
    public Map<String, Object> getEmotionalState(Pet pet) {
        Map<String, Object> state = new HashMap<>();
        
        state.put("emotion", pet.getEmotion());
        state.put("happiness", pet.getHappiness());
        state.put("energy", pet.getEnergy());
        state.put("health", pet.getHealth());
        state.put("neglectLevel", pet.getNeglectLevel());
        state.put("needsAttention", pet.isNeedsAttention());
        state.put("currentSound", pet.getCurrentSound());
        state.put("visualExpression", pet.getVisualExpression());
        state.put("isMoving", pet.isMoving());
        state.put("movementType", pet.getMovementType());
        state.put("currentLocation", pet.getCurrentLocation());
        
        // Generate emotional description
        String description = generateEmotionalDescription(pet);
        state.put("description", description);
        
        return state;
    }

    /**
     * Generate human-readable emotional description
     * @param pet The pet to describe
     * @return Emotional description
     */
    private String generateEmotionalDescription(Pet pet) {
        String name = pet.getName();
        String emotion = pet.getEmotion();
        String sound = pet.getCurrentSound();
        String expression = pet.getVisualExpression();
        
        StringBuilder description = new StringBuilder();
        description.append(name).append(" is currently feeling ").append(emotion).append(". ");
        
        if (!sound.equals("silent")) {
            description.append("The pet is making ").append(sound).append(" sounds. ");
        }
        
        description.append("Visually, ").append(name).append(" is showing ").append(expression).append(". ");
        
        if (pet.isNeedsAttention()) {
            description.append(name).append(" seems to need some attention and care. ");
        }
        
        if (pet.isMoving()) {
            description.append(name).append(" is ").append(pet.getMovementType()).append(" around. ");
        } else {
            description.append(name).append(" is staying in place. ");
        }
        
        return description.toString();
    }
} 