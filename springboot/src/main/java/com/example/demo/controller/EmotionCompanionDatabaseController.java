package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.EmotionCompanion;
import com.example.demo.service.EmotionCompanionService;

@RestController
@RequestMapping("/api/emotion")
@CrossOrigin(origins = "*")
public class EmotionCompanionDatabaseController {

    @Autowired
    private EmotionCompanionService emotionCompanionService;

    /**
     * 获取或初始化用户的情感陪伴状态
     * @param userId 用户ID
     * @return 情感陪伴状态
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionState(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            EmotionCompanion companion = emotionCompanionService.getOrInit(userId);
            
            response.put("success", true);
            response.put("companion", companion);
            response.put("message", "Emotion companion state retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving emotion companion state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新情感陪伴状态
     * @param companion 包含更新字段的EmotionCompanion对象（必须包含userId）
     * @return 更新后的状态
     */
    @PatchMapping("/state")
    public ResponseEntity<Map<String, Object>> updateEmotionCompanionState(
            @RequestBody EmotionCompanion companion) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (companion.getUserId() == null) {
                response.put("success", false);
                response.put("message", "UserId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            EmotionCompanion updatedCompanion = emotionCompanionService.updateState(companion);
            
            response.put("success", true);
            response.put("companion", updatedCompanion);
            response.put("message", "Emotion companion state updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating emotion companion state: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新用户交互时间
     * @param requestData 包含userId的请求体
     * @return 操作结果
     */
    @PostMapping("/touch/interaction")
    public ResponseEntity<Map<String, Object>> touchInteraction(
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            emotionCompanionService.touchInteraction(userId);
            
            response.put("success", true);
            response.put("message", "Interaction time updated successfully");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating interaction time: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新用户聊天时间
     * @param requestData 包含userId的请求体
     * @return 操作结果
     */
    @PostMapping("/touch/chat")
    public ResponseEntity<Map<String, Object>> touchChat(
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            emotionCompanionService.touchChat(userId);
            
            response.put("success", true);
            response.put("message", "Chat time updated successfully");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating chat time: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 重置用户的情感陪伴（删除记录）
     * @param requestData 包含userId的请求体
     * @return 操作结果
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetEmotionCompanion(
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            emotionCompanionService.resetForUser(userId);
            
            response.put("success", true);
            response.put("message", "Emotion companion reset successfully");
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting emotion companion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
