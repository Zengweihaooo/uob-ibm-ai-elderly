package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.PetMoodMapper;
import com.example.demo.pojo.PetMood;

/**
 * Service for managing the pet's mood score for each user.
 * 
 * - Stores mood scores in database using PetMoodMapper
 * - Allows adjusting the score by a given delta while keeping it within bounds
 * - Provides methods to retrieve the current score and translate it into a mood state
 * - Manages pet attributes (happiness, health, energy) and updates mood accordingly
 * 
 * @author Lepeng Zhou
 * @version 2.0
 */
@Service
public class PetMoodService {
    
    @Autowired
    private PetMoodMapper petMoodMapper;
    
    // Keep in-memory cache as a performance optimization
    private final Map<Long, PetMood> moodCache = new ConcurrentHashMap<>();

    /**
     * Get or initialize the user's pet mood
     * @param userId user ID
     * @return PetMood instance
     */
    public PetMood getOrInitPetMood(Long userId) {
        // Try cache first
        PetMood cached = moodCache.get(userId);
        if (cached != null) {
            return cached;
        }
        
        // Fetch from database
        PetMood petMood = petMoodMapper.findByUserId(userId);
        if (petMood == null) {
            // Create default instance
            petMood = new PetMood(userId);
            petMoodMapper.insert(petMood);
        }
        
        // Put into cache
        moodCache.put(userId, petMood);
        return petMood;
    }

    /**
     * Adjust mood score
     * @param userId user ID
     * @param delta adjustment value
     * @return adjusted score
     */
    public int adjustMood(Long userId, int delta) {
        PetMood petMood = getOrInitPetMood(userId);
        int currentScore = petMood.getMoodScore();
        int newScore = Math.max(-100, Math.min(100, currentScore + delta));
        
        petMood.setMoodScore(newScore);
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // Update database
        petMoodMapper.update(petMood);
        
        // Update cache
        moodCache.put(userId, petMood);
        
        return newScore;
    }

    /**
     * Get mood score
     * @param userId user ID
     * @return mood score
     */
    public int getMood(Long userId) {
        PetMood petMood = getOrInitPetMood(userId);
        return petMood.getMoodScore();
    }

    /**
     * Get mood state by score
     * @param score mood score
     * @return mood state description
     */
    public String moodState(int score) {
        if (score <= -20) return "sad";
        if (score >= 20) return "happy";
        return "neutral";
    }

    /**
     * Update pet attributes and recalculate mood
     * @param userId user ID
     * @param happiness happiness level
     * @param health health level
     * @param energy energy value
     * @return updated PetMood object
     */
    public PetMood updatePetAttributes(Long userId, int happiness, int health, int energy) {
        PetMood petMood = getOrInitPetMood(userId);
        
        // Update attributes
        petMood.setHappiness(Math.max(0, Math.min(100, happiness)));
        petMood.setHealth(Math.max(0, Math.min(100, health)));
        petMood.setEnergy(Math.max(0, Math.min(100, energy)));
        
        // Recalculate mood score
        int avgStats = (petMood.getHappiness() + petMood.getHealth() + petMood.getEnergy()) / 3;
        int moodScore = (avgStats - 50) * 2; // Convert 0-100 to -100 to 100
        petMood.setMoodScore(Math.max(-100, Math.min(100, moodScore)));
        
        // Update mood emoji and status
        updateMoodDisplay(petMood);
        
        // Update timestamps
        petMood.setLastInteraction(LocalDateTime.now());
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // Update database and cache
        petMoodMapper.update(petMood);
        moodCache.put(userId, petMood);
        
        return petMood;
    }

    /**
     * Update mood display (emoji and status description)
     * @param petMood PetMood object
     */
    private void updateMoodDisplay(PetMood petMood) {
        int score = petMood.getMoodScore();
        int avgStats = (petMood.getHappiness() + petMood.getHealth() + petMood.getEnergy()) / 3;
        
        String moodEmoji;
        String status;
        
        if (avgStats > 80) {
            moodEmoji = "😊";
            status = "Very Happy & Healthy!";
        } else if (avgStats > 60) {
            moodEmoji = "🙂";
            status = "Content & Well";
        } else if (avgStats > 40) {
            moodEmoji = "😐";
            status = "Okay, Could Be Better";
        } else {
            moodEmoji = "😢";
            status = "Needs Care & Attention";
        }
        
        petMood.setMoodEmoji(moodEmoji);
        petMood.setStatus(status);
    }

    /**
     * Add experience points
     * @param userId user ID
     * @param exp experience increment
     * @return updated experience value
     */
    public int addExperience(Long userId, int exp) {
        PetMood petMood = getOrInitPetMood(userId);
        int currentExp = petMood.getExperience();
        int newExp = currentExp + exp;
        
        petMood.setExperience(newExp);
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // Check whether to level up
        checkLevelUp(petMood);
        
        // Update database and cache
        petMoodMapper.update(petMood);
        moodCache.put(userId, petMood);
        
        return newExp;
    }

    /**
     * Check whether to level up
     * @param petMood PetMood object
     */
    private void checkLevelUp(PetMood petMood) {
        int currentLevel = petMood.getLevel();
        int currentExp = petMood.getExperience();
        int requiredExp = currentLevel * 100; // 100 experience points per level
        
        if (currentExp >= requiredExp) {
            petMood.setLevel(currentLevel + 1);
            petMood.setExperience(currentExp - requiredExp);
        }
    }

    /**
     * Get complete pet status
     * @param userId user ID
     * @return Map containing all pet information
     */
    public Map<String, Object> getFullPetStatus(Long userId) {
        PetMood petMood = getOrInitPetMood(userId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("moodScore", petMood.getMoodScore());
        status.put("moodState", moodState(petMood.getMoodScore()));
        status.put("happiness", petMood.getHappiness());
        status.put("health", petMood.getHealth());
        status.put("energy", petMood.getEnergy());
        status.put("moodEmoji", petMood.getMoodEmoji());
        status.put("status", petMood.getStatus());
        status.put("level", petMood.getLevel());
        status.put("experience", petMood.getExperience());
        status.put("lastInteraction", petMood.getLastInteraction());
        
        return status;
    }

    /**
     * Clear cache (for testing or maintenance)
     * @param userId user ID
     */
    public void clearCache(Long userId) {
        moodCache.remove(userId);
    }

    /**
     * Clear all cache
     */
    public void clearAllCache() {
        moodCache.clear();
    }
}
