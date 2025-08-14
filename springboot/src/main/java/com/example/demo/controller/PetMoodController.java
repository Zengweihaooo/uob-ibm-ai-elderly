package com.example.demo.controller;

import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.PetMoodService;
import com.example.demo.pojo.PetMood;

/**
 * 宠物情绪管理控制器
 * 提供宠物情绪相关的REST API接口
 */
@RestController
@RequestMapping("/api/pet/mood")
@CrossOrigin(origins = "*")
public class PetMoodController {
    
    private final PetMoodService moodService;
    
    @Autowired
    public PetMoodController(PetMoodService moodService) {
        this.moodService = moodService;
    }

    /**
     * 获取宠物情绪状态
     * @param userId 用户ID
     * @return 情绪分数和状态
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getMoodState(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int score = moodService.getMood(userId);
            String state = moodService.moodState(score);
            
            response.put("success", true);
            response.put("score", score);
            response.put("state", state);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving mood state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 调整宠物情绪分数
     * @param userId 用户ID
     * @param requestBody 包含delta的请求体
     * @return 调整后的分数和状态
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, Object>> adjustMood(
            @RequestParam Long userId, 
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object deltaObj = requestBody.getOrDefault("delta", 0);
            int delta;
            
            if (deltaObj instanceof Number) {
                delta = ((Number) deltaObj).intValue();
            } else {
                delta = Integer.parseInt(deltaObj.toString());
            }
            
            int score = moodService.adjustMood(userId, delta);
            String state = moodService.moodState(score);
            
            response.put("success", true);
            response.put("score", score);
            response.put("state", state);
            response.put("delta", delta);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adjusting mood: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取完整的宠物状态
     * @param userId 用户ID
     * @return 完整的宠物状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFullPetStatus(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> status = moodService.getFullPetStatus(userId);
            
            response.put("success", true);
            response.put("petStatus", status);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving pet status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新宠物属性
     * @param userId 用户ID
     * @param requestBody 包含happiness, health, energy的请求体
     * @return 更新后的宠物状态
     */
    @PostMapping("/attributes")
    public ResponseEntity<Map<String, Object>> updatePetAttributes(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int happiness = getIntValue(requestBody, "happiness", 85);
            int health = getIntValue(requestBody, "health", 92);
            int energy = getIntValue(requestBody, "energy", 78);
            
            PetMood updatedPet = moodService.updatePetAttributes(userId, happiness, health, energy);
            
            response.put("success", true);
            response.put("pet", updatedPet);
            response.put("message", "Pet attributes updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating pet attributes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 增加经验值
     * @param userId 用户ID
     * @param requestBody 包含exp的请求体
     * @return 更新后的经验值
     */
    @PostMapping("/experience")
    public ResponseEntity<Map<String, Object>> addExperience(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int exp = getIntValue(requestBody, "exp", 10);
            int newExp = moodService.addExperience(userId, exp);
            
            response.put("success", true);
            response.put("experience", newExp);
            response.put("addedExp", exp);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding experience: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重置宠物情绪（用于测试或重置）
     * @param userId 用户ID
     * @return 重置后的状态
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPetMood(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 重置为默认值
            PetMood resetPet = moodService.updatePetAttributes(userId, 85, 92, 78);
            
            response.put("success", true);
            response.put("pet", resetPet);
            response.put("message", "Pet mood reset to default values");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting pet mood: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 清除缓存（用于维护）
     * @param userId 用户ID（可选，如果不提供则清除所有缓存）
     * @return 操作结果
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, Object>> clearCache(
            @RequestParam(required = false) Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (userId != null) {
                moodService.clearCache(userId);
                response.put("message", "Cache cleared for user: " + userId);
            } else {
                moodService.clearAllCache();
                response.put("message", "All caches cleared");
            }
            
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 从请求体中安全地获取整数值
     * @param requestBody 请求体
     * @param key 键名
     * @param defaultValue 默认值
     * @return 整数值
     */
    private int getIntValue(Map<String, Object> requestBody, String key, int defaultValue) {
        Object value = requestBody.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
