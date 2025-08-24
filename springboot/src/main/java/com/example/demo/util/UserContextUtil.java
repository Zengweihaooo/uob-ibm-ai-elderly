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
            return jwtUtil.isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}
