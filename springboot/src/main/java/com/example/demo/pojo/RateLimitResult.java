package com.example.demo.pojo;

/**
 * 频率限制结果类
 * 
 * @author AI Assistant
 * @version 1.0
 */
public class RateLimitResult {
    private boolean allowed;
    private String message;
    
    public RateLimitResult() {
    }
    
    public RateLimitResult(boolean allowed, String message) {
        this.allowed = allowed;
        this.message = message;
    }
    
    // Getters and Setters
    public boolean isAllowed() {
        return allowed;
    }
    
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}