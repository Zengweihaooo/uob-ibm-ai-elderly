package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.util.JwtUtil;

/**
 * JWT测试控制器，用于生成和测试token
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@RestController
@RequestMapping("/api/test/jwt")
@CrossOrigin(origins = "*")
public class JwtTestController {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 生成测试token
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = (String) request.get("username");
            Long userId = Long.valueOf(request.get("userId").toString());
            
            if (username == null || userId == null) {
                response.put("success", false);
                response.put("message", "Username and userId are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            String token = jwtUtil.generateToken(username, userId);
            
            response.put("success", true);
            response.put("token", token);
            response.put("username", username);
            response.put("userId", userId);
            response.put("message", "Token generated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to generate token: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 验证token
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = (String) request.get("token");
            String username = (String) request.get("username");
            
            if (token == null || username == null) {
                response.put("success", false);
                response.put("message", "Token and username are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 验证token
            boolean isValid = jwtUtil.validateToken(token, username);
            
            if (isValid) {
                // 提取用户ID
                Long userId = jwtUtil.extractUserId(token);
                
                response.put("success", true);
                response.put("valid", true);
                response.put("username", username);
                response.put("userId", userId);
                response.put("message", "Token is valid");
            } else {
                response.put("success", true);
                response.put("valid", false);
                response.put("message", "Token is invalid or expired");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to verify token: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 从Authorization header提取用户ID
     */
    @GetMapping("/extract-user")
    public ResponseEntity<Map<String, Object>> extractUserFromHeader(
            @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "Authorization header is required and must start with 'Bearer '");
                return ResponseEntity.badRequest().body(response);
            }
            
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            String username = jwtUtil.extractUsername(jwtUtil.extractTokenFromHeader(authHeader));
            
            if (userId != null) {
                response.put("success", true);
                response.put("userId", userId);
                response.put("username", username);
                response.put("message", "User ID extracted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to extract user ID from token");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to extract user ID: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
