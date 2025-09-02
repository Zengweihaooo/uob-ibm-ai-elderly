package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.PetMood;
import com.example.demo.repository.PetMoodRepository;

/**
 * Hybrid Pet Mood Service
 * Supports dual storage with local SQLite and cloud DynamoDB
 * Enables progressive data migration and synchronization
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
public class HybridPetMoodService {
    
    private static final Logger logger = Logger.getLogger(HybridPetMoodService.class.getName());
    
    @Autowired
    @Qualifier("sqlitePetMoodRepository")
    private PetMoodRepository primaryRepository;
    
    @Autowired(required = false)
    @Qualifier("dynamoDBPetMoodRepository")
    private PetMoodRepository cloudRepository;
    
    @Autowired(required = false)
    @Qualifier("sqlitePetMoodRepository")
    private PetMoodRepository localRepository;
    
    /**
     * Get or initialize user's pet mood
     * Prefer fetching from the primary data source, supports background sync
     */
    public PetMood getOrInitPetMood(Long userId) {
        try {
            // 1. Fetch from the primary data source
            Optional<PetMood> petMoodOpt = primaryRepository.findByUserId(userId);
            
            if (petMoodOpt.isPresent()) {
                PetMood petMood = petMoodOpt.get();
                
                // 2. Background sync to cloud (if enabled)
                if (cloudRepository != null && cloudRepository != primaryRepository) {
                    CompletableFuture.runAsync(() -> {
                        syncToCloud(userId, petMood);
                    });
                }
                
                return petMood;
            }
            
            // 3. Create a new pet mood record
            PetMood newPetMood = new PetMood(userId);
            PetMood savedPetMood = primaryRepository.save(newPetMood);
            
            // 4. Sync to cloud
            if (cloudRepository != null && cloudRepository != primaryRepository) {
                CompletableFuture.runAsync(() -> {
                    syncToCloud(userId, savedPetMood);
                });
            }
            
            return savedPetMood;
            
        } catch (Exception e) {
            logger.severe("Failed to get or init pet mood for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to get or init pet mood", e);
        }
    }
    
    /**
     * Adjust mood score
     * Update both local and cloud data
     */
    public int adjustMood(Long userId, int delta) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            int currentScore = petMood.getMoodScore();
            int newScore = Math.max(-100, Math.min(100, currentScore + delta));
            
            petMood.setMoodScore(newScore);
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // Update primary data source
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // Background sync to cloud
            if (cloudRepository != null && cloudRepository != primaryRepository) {
                CompletableFuture.runAsync(() -> {
                    syncToCloud(userId, updatedPetMood);
                });
            }
            
            return newScore;
            
        } catch (Exception e) {
            logger.severe("Failed to adjust mood for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to adjust mood", e);
        }
    }
    
    /**
     * Get mood score
     */
    public int getMood(Long userId) {
        PetMood petMood = getOrInitPetMood(userId);
        return petMood.getMoodScore();
    }
    
    /**
     * Get mood state by score
     */
    public String moodState(int score) {
        if (score <= -20) return "sad";
        if (score >= 20) return "happy";
        return "neutral";
    }
    
    /**
     * Update pet attributes and recalculate mood
     */
    public PetMood updatePetAttributes(Long userId, int happiness, int health, int energy) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            
            // Update attributes
            petMood.setHappiness(Math.max(0, Math.min(100, happiness)));
            petMood.setHealth(Math.max(0, Math.min(100, health)));
            petMood.setEnergy(Math.max(0, Math.min(100, energy)));
            
            // Recalculate mood score
            int avgStats = (petMood.getHappiness() + petMood.getHealth() + petMood.getEnergy()) / 3;
            int moodScore = (avgStats - 50) * 2;
            petMood.setMoodScore(Math.max(-100, Math.min(100, moodScore)));
            
            // Update mood emoji and status
            updateMoodDisplay(petMood);
            
            // Update time
            petMood.setLastInteraction(LocalDateTime.now());
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // Update primary data source
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // Background sync to cloud
            if (cloudRepository != null && cloudRepository != primaryRepository) {
                CompletableFuture.runAsync(() -> {
                    syncToCloud(userId, updatedPetMood);
                });
            }
            
            return updatedPetMood;
            
        } catch (Exception e) {
            logger.severe("Failed to update pet attributes for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to update pet attributes", e);
        }
    }
    
    /**
     * Add experience points
     */
    public int addExperience(Long userId, int exp) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            int currentExp = petMood.getExperience();
            int newExp = currentExp + exp;
            
            petMood.setExperience(newExp);
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // Check whether level up
            checkLevelUp(petMood);
            
            // Update primary data source
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // Background sync to cloud
            if (cloudRepository != null && cloudRepository != primaryRepository) {
                CompletableFuture.runAsync(() -> {
                    syncToCloud(userId, updatedPetMood);
                });
            }
            
            return newExp;
            
        } catch (Exception e) {
            logger.severe("Failed to add experience for user " + userId + ": " + e.getMessage());
            throw new RuntimeException("Failed to add experience", e);
        }
    }
    
    /**
     * Data migration: migrate from local to cloud
     */
    public void migrateToCloud() {
        if (cloudRepository == null || localRepository == null) {
            logger.warning("Cloud or local repository not available for migration");
            return;
        }
        
        try {
            logger.info("Starting data migration from local to cloud...");
            
            List<PetMood> localPetMoods = localRepository.findAll();
            logger.info("Found " + localPetMoods.size() + " local records to migrate");
            
            int successCount = 0;
            int failCount = 0;
            
            for (PetMood petMood : localPetMoods) {
                try {
                    cloudRepository.save(petMood);
                    successCount++;
                } catch (Exception e) {
                    logger.warning("Failed to migrate pet mood for user " + petMood.getUserId() + ": " + e.getMessage());
                    failCount++;
                }
            }
            
            logger.info("Migration completed. Success: " + successCount + ", Failed: " + failCount);
            
        } catch (Exception e) {
            logger.severe("Migration failed: " + e.getMessage());
            throw new RuntimeException("Migration failed", e);
        }
    }
    
    /**
     * Data synchronization: sync local data to cloud
     */
    private void syncToCloud(Long userId, PetMood petMood) {
        if (cloudRepository == null || cloudRepository == primaryRepository) {
            return;
        }
        
        try {
            cloudRepository.save(petMood);
            logger.fine("Successfully synced pet mood to cloud for user " + userId);
        } catch (Exception e) {
            logger.warning("Failed to sync pet mood to cloud for user " + userId + ": " + e.getMessage());
        }
    }
    
    /**
     * Update mood display (emoji and status description)
     */
    private void updateMoodDisplay(PetMood petMood) {
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
     * Check whether to level up
     */
    private void checkLevelUp(PetMood petMood) {
        int currentLevel = petMood.getLevel();
        int currentExp = petMood.getExperience();
        int requiredExp = currentLevel * 100;
        
        if (currentExp >= requiredExp) {
            petMood.setLevel(currentLevel + 1);
            petMood.setExperience(currentExp - requiredExp);
        }
    }
}

