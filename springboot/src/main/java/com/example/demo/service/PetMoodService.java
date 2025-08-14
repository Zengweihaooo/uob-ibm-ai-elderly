package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
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
    
    // 保留内存缓存作为性能优化
    private final Map<Long, PetMood> moodCache = new ConcurrentHashMap<>();

    /**
     * 获取或初始化用户的宠物情绪
     * @param userId 用户ID
     * @return PetMood 实例
     */
    public PetMood getOrInitPetMood(Long userId) {
        // 先从缓存获取
        PetMood cached = moodCache.get(userId);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库获取
        PetMood petMood = petMoodMapper.findByUserId(userId);
        if (petMood == null) {
            // 创建默认实例
            petMood = new PetMood(userId);
            petMoodMapper.insert(petMood);
        }
        
        // 放入缓存
        moodCache.put(userId, petMood);
        return petMood;
    }

    /**
     * 调整情绪分数
     * @param userId 用户ID
     * @param delta 调整值
     * @return 调整后的分数
     */
    public int adjustMood(Long userId, int delta) {
        PetMood petMood = getOrInitPetMood(userId);
        int currentScore = petMood.getMoodScore();
        int newScore = Math.max(-100, Math.min(100, currentScore + delta));
        
        petMood.setMoodScore(newScore);
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // 更新数据库
        petMoodMapper.update(petMood);
        
        // 更新缓存
        moodCache.put(userId, petMood);
        
        return newScore;
    }

    /**
     * 获取情绪分数
     * @param userId 用户ID
     * @return 情绪分数
     */
    public int getMood(Long userId) {
        PetMood petMood = getOrInitPetMood(userId);
        return petMood.getMoodScore();
    }

    /**
     * 根据分数获取情绪状态
     * @param score 情绪分数
     * @return 情绪状态描述
     */
    public String moodState(int score) {
        if (score <= -20) return "sad";
        if (score >= 20) return "happy";
        return "neutral";
    }

    /**
     * 更新宠物属性并重新计算情绪
     * @param userId 用户ID
     * @param happiness 快乐度
     * @param health 健康度
     * @param energy 精力值
     * @return 更新后的PetMood对象
     */
    public PetMood updatePetAttributes(Long userId, int happiness, int health, int energy) {
        PetMood petMood = getOrInitPetMood(userId);
        
        // 更新属性
        petMood.setHappiness(Math.max(0, Math.min(100, happiness)));
        petMood.setHealth(Math.max(0, Math.min(100, health)));
        petMood.setEnergy(Math.max(0, Math.min(100, energy)));
        
        // 重新计算情绪分数
        int avgStats = (petMood.getHappiness() + petMood.getHealth() + petMood.getEnergy()) / 3;
        int moodScore = (avgStats - 50) * 2; // 将0-100转换为-100到100
        petMood.setMoodScore(Math.max(-100, Math.min(100, moodScore)));
        
        // 更新情绪表情和状态
        updateMoodDisplay(petMood);
        
        // 更新时间
        petMood.setLastInteraction(LocalDateTime.now());
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // 更新数据库和缓存
        petMoodMapper.update(petMood);
        moodCache.put(userId, petMood);
        
        return petMood;
    }

    /**
     * 更新情绪显示（表情和状态描述）
     * @param petMood 宠物情绪对象
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
     * 增加经验值
     * @param userId 用户ID
     * @param exp 经验值增量
     * @return 更新后的经验值
     */
    public int addExperience(Long userId, int exp) {
        PetMood petMood = getOrInitPetMood(userId);
        int currentExp = petMood.getExperience();
        int newExp = currentExp + exp;
        
        petMood.setExperience(newExp);
        petMood.setUpdatedAt(LocalDateTime.now());
        
        // 检查是否升级
        checkLevelUp(petMood);
        
        // 更新数据库和缓存
        petMoodMapper.update(petMood);
        moodCache.put(userId, petMood);
        
        return newExp;
    }

    /**
     * 检查是否升级
     * @param petMood 宠物情绪对象
     */
    private void checkLevelUp(PetMood petMood) {
        int currentLevel = petMood.getLevel();
        int currentExp = petMood.getExperience();
        int requiredExp = currentLevel * 100; // 每级需要100经验值
        
        if (currentExp >= requiredExp) {
            petMood.setLevel(currentLevel + 1);
            petMood.setExperience(currentExp - requiredExp);
        }
    }

    /**
     * 获取完整的宠物状态
     * @param userId 用户ID
     * @return 包含所有宠物信息的Map
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
     * 清除缓存（用于测试或维护）
     * @param userId 用户ID
     */
    public void clearCache(Long userId) {
        moodCache.remove(userId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        moodCache.clear();
    }
}
