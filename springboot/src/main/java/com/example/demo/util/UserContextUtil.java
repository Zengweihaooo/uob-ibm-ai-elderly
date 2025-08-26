package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 用于从Authorization header中提取用户ID
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Component
public class UserContextUtil {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 从Authorization header中提取用户ID
     * 
     * @param authHeader Authorization header
     * @return 用户ID，如果无效则返回null
     */
    public Long getUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7); // 移除"Bearer "前缀
            
            // 只处理JWT token
            if (jwtUtil.isValidToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常
            System.err.println("Error extracting user ID from token: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 从Authorization header中提取用户邮箱
     * 
     * @param authHeader Authorization header
     * @return 用户邮箱，如果无效则返回null
     */
    public String getEmailFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7); // 移除"Bearer "前缀
            if (jwtUtil.isValidToken(token)) {
                return jwtUtil.getEmailFromToken(token);
            }
        } catch (Exception e) {
            // 记录日志但不抛出异常
            System.err.println("Error extracting email from token: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 验证Authorization header是否有效
     * 
     * @param authHeader Authorization header
     * @return 是否有效
     */
    public boolean isValidAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        
        try {
            String token = authHeader.substring(7);
            
            // 只验证JWT token
            return jwtUtil.isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Debug method to log token extraction process
     * 
     * @param authHeader Authorization header
     * @return Debug information
     */
    public String debugTokenExtraction(String authHeader) {
        if (authHeader == null) {
            return "Auth header is null";
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "Auth header doesn't start with 'Bearer '";
        }
        
        String token = authHeader.substring(7);
        
        try {
            if (jwtUtil.isValidToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return "Valid JWT token: " + token + " -> User ID: " + userId;
            } else {
                return "Invalid JWT token: " + token;
            }
        } catch (Exception e) {
            return "Error processing JWT token: " + e.getMessage();
        }
    }
}
