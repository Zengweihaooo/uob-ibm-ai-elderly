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
     * 获取或初始化用户的情感陪伴
     * @param userId 用户ID
     * @return EmotionCompanion 实例
     */
    public EmotionCompanion getOrInit(Long userId) {
        // 先查询是否已存在
        EmotionCompanion existing = emotionCompanionMapper.findByUserId(userId);
        if (existing != null) {
            return existing;
        }
        
        // 不存在则创建默认实例
        EmotionCompanion companion = createDefaultCompanion(userId);
        LocalDateTime now = LocalDateTime.now();
        companion.setCreatedAt(now);
        companion.setUpdatedAt(now);
        
        // 使用 upsert 插入
        emotionCompanionMapper.upsert(companion);
        
        // 重新查询并返回（获取生成的ID等）
        return emotionCompanionMapper.findByUserId(userId);
    }

    /**
     * 更新情感陪伴状态
     * @param patch 包含更新字段的EmotionCompanion对象
     * @return 更新后的EmotionCompanion
     */
    public EmotionCompanion updateState(EmotionCompanion patch) {
        // 设置更新时间
        patch.setUpdatedAt(LocalDateTime.now());
        
        // 执行更新
        emotionCompanionMapper.updateState(patch);
        
        // 重新查询并返回最新状态
        return emotionCompanionMapper.findByUserId(patch.getUserId());
    }

    /**
     * 更新用户交互时间
     * @param userId 用户ID
     */
    public void touchInteraction(Long userId) {
        emotionCompanionMapper.touchInteraction(userId, LocalDateTime.now());
    }

    /**
     * 更新用户聊天时间
     * @param userId 用户ID
     */
    public void touchChat(Long userId) {
        emotionCompanionMapper.touchChat(userId, LocalDateTime.now());
    }

    /**
     * 重置用户的情感陪伴（删除记录）
     * @param userId 用户ID
     */
    public void resetForUser(Long userId) {
        emotionCompanionMapper.deleteByUserId(userId);
    }

    /**
     * 创建默认情感陪伴实例
     * @param userId 用户ID
     * @return 默认配置的EmotionCompanion
     */
    private EmotionCompanion createDefaultCompanion(Long userId) {
        EmotionCompanion companion = new EmotionCompanion(userId, "Alexa", "friendly", "assistant");
        
        // 基础设置
        companion.setUserId(userId);
        companion.setEmotion("happy");
        companion.setHappiness(85);
        companion.setEnergy(78);
        companion.setResponsiveness(90);
        
        // 交互计数初始化
        companion.setInteractionCount(0);
        companion.setChatCount(0);
        
        // 位置和活动状态
        companion.setCurrentLocation("home_screen");
        companion.setActive(true);
        companion.setActivityMode("listening");
        companion.setCurrentTask("Ready to help");
        
        // 表达方式
        companion.setCurrentSound("chime");
        companion.setVisualExpression("happy_led");
        companion.setMakingSound(false);
        companion.setExpressingEmotion(true);
        companion.setLedColor("green");
        
        // 关注度设置
        LocalDateTime now = LocalDateTime.now();
        companion.setLastAttentionTime(now);
        companion.setLastInteraction(now);
        companion.setLastChat(now);
        companion.setLastCommand(now);
        companion.setNeglectLevel(0);
        companion.setNeedsAttention(false);
        companion.setLonely(false);
        
        // AI特性
        companion.setLearning(false);
        companion.setHelpfulness(95);
        
        return companion;
    }
} 