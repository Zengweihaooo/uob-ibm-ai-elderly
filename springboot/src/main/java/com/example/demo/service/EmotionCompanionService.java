package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.EmotionCompanionMapper;
import com.example.demo.pojo.EmotionCompanion;

@Service
public class EmotionCompanionService {

    @Autowired
    private EmotionCompanionMapper emotionCompanionMapper;

    /**
     * Get or initialize user's emotion companion
     * @param userId user ID
     * @return EmotionCompanion instance
     */
    public EmotionCompanion getOrInit(Long userId) {
        // First check whether it already exists
        EmotionCompanion existing = emotionCompanionMapper.findByUserId(userId);
        if (existing != null) {
            return existing;
        }
        
        // Create a default instance if it does not exist
        EmotionCompanion companion = createDefaultCompanion(userId);
        LocalDateTime now = LocalDateTime.now();
        companion.setCreatedAt(now);
        companion.setUpdatedAt(now);
        
        // Insert using upsert
        emotionCompanionMapper.upsert(companion);
        
        // Re-query and return (obtain generated ID, etc.)
        return emotionCompanionMapper.findByUserId(userId);
    }

    /**
     * Update emotion companion state
     * @param patch EmotionCompanion object containing fields to update
     * @return updated EmotionCompanion
     */
    public EmotionCompanion updateState(EmotionCompanion patch) {
        // Set update time
        patch.setUpdatedAt(LocalDateTime.now());
        
        // Execute update
        emotionCompanionMapper.updateState(patch);
        
        // Re-query and return the latest state
        return emotionCompanionMapper.findByUserId(patch.getUserId());
    }

    /**
     * Update user's interaction time
     * @param userId user ID
     */
    public void touchInteraction(Long userId) {
        emotionCompanionMapper.touchInteraction(userId, LocalDateTime.now());
    }

    /**
     * Update user's chat time
     * @param userId user ID
     */
    public void touchChat(Long userId) {
        emotionCompanionMapper.touchChat(userId, LocalDateTime.now());
    }

    /**
     * Reset user's emotion companion (delete record)
     * @param userId user ID
     */
    public void resetForUser(Long userId) {
        emotionCompanionMapper.deleteByUserId(userId);
    }

    /**
     * Create a default emotion companion instance
     * @param userId user ID
     * @return EmotionCompanion with default configuration
     */
    private EmotionCompanion createDefaultCompanion(Long userId) {
        EmotionCompanion companion = new EmotionCompanion(userId, "Alexa", "friendly", "assistant");
        
        // Basic settings
        companion.setUserId(userId);
        companion.setEmotion("happy");
        companion.setHappiness(85);
        companion.setEnergy(78);
        companion.setResponsiveness(90);
        
        // Initialize interaction counters
        companion.setInteractionCount(0);
        companion.setChatCount(0);
        
        // Location and activity status
        companion.setCurrentLocation("home_screen");
        companion.setActive(true);
        companion.setActivityMode("listening");
        companion.setCurrentTask("Ready to help");
        
        // Expression settings
        companion.setCurrentSound("chime");
        companion.setVisualExpression("happy_led");
        companion.setMakingSound(false);
        companion.setExpressingEmotion(true);
        companion.setLedColor("green");
        
        // Attention settings
        LocalDateTime now = LocalDateTime.now();
        companion.setLastAttentionTime(now);
        companion.setLastInteraction(now);
        companion.setLastChat(now);
        companion.setLastCommand(now);
        companion.setNeglectLevel(0);
        companion.setNeedsAttention(false);
        companion.setLonely(false);
        
        // AI traits
        companion.setLearning(false);
        companion.setHelpfulness(95);
        
        return companion;
    }
} 