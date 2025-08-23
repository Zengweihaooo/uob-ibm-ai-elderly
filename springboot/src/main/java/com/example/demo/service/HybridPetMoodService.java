package com.example.demo.service;

import com.example.demo.pojo.PetMood;
import com.example.demo.repository.PetMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 混合宠物情绪服务
 * 支持本地SQLite和云端DynamoDB双重存储
 * 实现渐进式数据迁移和同步
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
     * 获取或初始化用户的宠物情绪
     * 优先从主数据源获取，支持后台同步
     */
    public PetMood getOrInitPetMood(Long userId) {
        try {
            // 1. 从主数据源获取
            Optional<PetMood> petMoodOpt = primaryRepository.findByUserId(userId);
            
            if (petMoodOpt.isPresent()) {
                PetMood petMood = petMoodOpt.get();
                
                // 2. 后台同步到云端（如果启用）
                if (cloudRepository != null && cloudRepository != primaryRepository) {
                    CompletableFuture.runAsync(() -> {
                        syncToCloud(userId, petMood);
                    });
                }
                
                return petMood;
            }
            
            // 3. 创建新的宠物情绪记录
            PetMood newPetMood = new PetMood(userId);
            PetMood savedPetMood = primaryRepository.save(newPetMood);
            
            // 4. 同步到云端
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
     * 调整情绪分数
     * 同时更新本地和云端数据
     */
    public int adjustMood(Long userId, int delta) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            int currentScore = petMood.getMoodScore();
            int newScore = Math.max(-100, Math.min(100, currentScore + delta));
            
            petMood.setMoodScore(newScore);
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // 更新主数据源
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // 后台同步到云端
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
     * 获取情绪分数
     */
    public int getMood(Long userId) {
        PetMood petMood = getOrInitPetMood(userId);
        return petMood.getMoodScore();
    }
    
    /**
     * 根据分数获取情绪状态
     */
    public String moodState(int score) {
        if (score <= -20) return "sad";
        if (score >= 20) return "happy";
        return "neutral";
    }
    
    /**
     * 更新宠物属性并重新计算情绪
     */
    public PetMood updatePetAttributes(Long userId, int happiness, int health, int energy) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            
            // 更新属性
            petMood.setHappiness(Math.max(0, Math.min(100, happiness)));
            petMood.setHealth(Math.max(0, Math.min(100, health)));
            petMood.setEnergy(Math.max(0, Math.min(100, energy)));
            
            // 重新计算情绪分数
            int avgStats = (petMood.getHappiness() + petMood.getHealth() + petMood.getEnergy()) / 3;
            int moodScore = (avgStats - 50) * 2;
            petMood.setMoodScore(Math.max(-100, Math.min(100, moodScore)));
            
            // 更新情绪表情和状态
            updateMoodDisplay(petMood);
            
            // 更新时间
            petMood.setLastInteraction(LocalDateTime.now());
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // 更新主数据源
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // 后台同步到云端
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
     * 增加经验值
     */
    public int addExperience(Long userId, int exp) {
        try {
            PetMood petMood = getOrInitPetMood(userId);
            int currentExp = petMood.getExperience();
            int newExp = currentExp + exp;
            
            petMood.setExperience(newExp);
            petMood.setUpdatedAt(LocalDateTime.now());
            
            // 检查是否升级
            checkLevelUp(petMood);
            
            // 更新主数据源
            PetMood updatedPetMood = primaryRepository.update(petMood);
            
            // 后台同步到云端
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
     * 数据迁移：从本地迁移到云端
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
     * 数据同步：将本地数据同步到云端
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
     * 更新情绪显示（表情和状态描述）
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
     * 检查是否升级
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

