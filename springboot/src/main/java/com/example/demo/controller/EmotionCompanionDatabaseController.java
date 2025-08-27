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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.EmotionCompanion;
import com.example.demo.service.EmotionCompanionService;
import com.example.demo.util.UserContextUtil;

@RestController
@RequestMapping("/api/emotion")
@CrossOrigin(origins = "*")
public class EmotionCompanionDatabaseController {

    @Autowired
    private EmotionCompanionService emotionCompanionService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    /**
     * 获取或初始化用户的情感陪伴状态
     * @param userId 用户ID (可选，如果提供则验证与token中的用户ID是否一致)
     * @param authorization Authorization header (Bearer token)
     * @return 情感陪伴状态
     */
    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getEmotionCompanionState(
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证JWT token
            if (authorization == null || !userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid or missing authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 从token中提取用户ID
            Long tokenUserId = userContextUtil.getUserIdFromAuthHeader(authorization);
            if (tokenUserId == null) {
                response.put("success", false);
                response.put("message", "Unable to extract user ID from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 如果请求参数中提供了userId，验证是否与token中的用户ID一致
            if (userId != null && !userId.equals(tokenUserId)) {
                response.put("success", false);
                response.put("message", "User ID mismatch: token user ID does not match requested user ID");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // 使用token中的用户ID
            Long targetUserId = userId != null ? userId : tokenUserId;
            EmotionCompanion companion = emotionCompanionService.getOrInit(targetUserId);
            
            response.put("success", true);
            response.put("companion", companion);
            response.put("message", "Emotion companion state retrieved successfully");
            response.put("userId", targetUserId);
            
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
     * @param authorization Authorization header (Bearer token)
     * @return 更新后的状态
     */
    @PatchMapping("/state")
    public ResponseEntity<Map<String, Object>> updateEmotionCompanionState(
            @RequestBody EmotionCompanion companion,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证JWT token
            if (authorization == null || !userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid or missing authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 从token中提取用户ID
            Long tokenUserId = userContextUtil.getUserIdFromAuthHeader(authorization);
            if (tokenUserId == null) {
                response.put("success", false);
                response.put("message", "Unable to extract user ID from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 验证请求体中的用户ID
            if (companion.getUserId() == null) {
                response.put("success", false);
                response.put("message", "UserId is required in request body");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证用户ID是否与token中的用户ID一致
            if (!companion.getUserId().equals(tokenUserId)) {
                response.put("success", false);
                response.put("message", "User ID mismatch: token user ID does not match request body user ID");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            EmotionCompanion updatedCompanion = emotionCompanionService.updateState(companion);
            
            response.put("success", true);
            response.put("companion", updatedCompanion);
            response.put("message", "Emotion companion state updated successfully");
            response.put("userId", tokenUserId);
            
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
     * @param authorization Authorization header (Bearer token)
     * @return 操作结果
     */
    @PostMapping("/touch/interaction")
    public ResponseEntity<Map<String, Object>> touchInteraction(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证JWT token
            if (authorization == null || !userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid or missing authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 从token中提取用户ID
            Long tokenUserId = userContextUtil.getUserIdFromAuthHeader(authorization);
            if (tokenUserId == null) {
                response.put("success", false);
                response.put("message", "Unable to extract user ID from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 验证请求体中的用户ID
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required in request body");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long requestUserId = Long.valueOf(userIdObj.toString());
            
            // 验证用户ID是否与token中的用户ID一致
            if (!requestUserId.equals(tokenUserId)) {
                response.put("success", false);
                response.put("message", "User ID mismatch: token user ID does not match request body user ID");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            emotionCompanionService.touchInteraction(requestUserId);
            
            response.put("success", true);
            response.put("message", "Interaction time updated successfully");
            response.put("userId", requestUserId);
            
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
     * @param authorization Authorization header (Bearer token)
     * @return 操作结果
     */
    @PostMapping("/touch/chat")
    public ResponseEntity<Map<String, Object>> touchChat(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证JWT token
            if (authorization == null || !userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid or missing authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 从token中提取用户ID
            Long tokenUserId = userContextUtil.getUserIdFromAuthHeader(authorization);
            if (tokenUserId == null) {
                response.put("success", false);
                response.put("message", "Unable to extract user ID from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 验证请求体中的用户ID
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required in request body");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long requestUserId = Long.valueOf(userIdObj.toString());
            
            // 验证用户ID是否与token中的用户ID一致
            if (!requestUserId.equals(tokenUserId)) {
                response.put("success", false);
                response.put("message", "User ID mismatch: token user ID does not match request body user ID");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            emotionCompanionService.touchChat(requestUserId);
            
            response.put("success", true);
            response.put("message", "Chat time updated successfully");
            response.put("userId", requestUserId);
            
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
     * @param authorization Authorization header (Bearer token)
     * @return 操作结果
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetEmotionCompanion(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证JWT token
            if (authorization == null || !userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid or missing authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 从token中提取用户ID
            Long tokenUserId = userContextUtil.getUserIdFromAuthHeader(authorization);
            if (tokenUserId == null) {
                response.put("success", false);
                response.put("message", "Unable to extract user ID from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 验证请求体中的用户ID
            Object userIdObj = requestData.get("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "UserId is required in request body");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long requestUserId = Long.valueOf(userIdObj.toString());
            
            // 验证用户ID是否与token中的用户ID一致
            if (!requestUserId.equals(tokenUserId)) {
                response.put("success", false);
                response.put("message", "User ID mismatch: token user ID does not match request body user ID");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            emotionCompanionService.resetForUser(requestUserId);
            
            response.put("success", true);
            response.put("message", "Emotion companion reset successfully");
            response.put("userId", requestUserId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resetting emotion companion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * JWT测试端点 - 用于验证JWT功能
     * @param authorization Authorization header (Bearer token)
     * @return JWT验证结果
     */
    @GetMapping("/jwt-test")
    public ResponseEntity<Map<String, Object>> testJwt(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authorization == null) {
                response.put("success", false);
                response.put("message", "Authorization header is missing");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (!userContextUtil.isValidAuthHeader(authorization)) {
                response.put("success", false);
                response.put("message", "Invalid authorization token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long userId = userContextUtil.getUserIdFromAuthHeader(authorization);
            String email = userContextUtil.getEmailFromAuthHeader(authorization);
            
            response.put("success", true);
            response.put("message", "JWT token is valid");
            response.put("userId", userId);
            response.put("email", email);
            response.put("debug", userContextUtil.debugTokenExtraction(authorization));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error testing JWT: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
