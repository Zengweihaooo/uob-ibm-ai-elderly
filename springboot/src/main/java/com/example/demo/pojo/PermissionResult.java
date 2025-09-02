package com.example.demo.pojo;

/**
 * Permission check result class
 * 
 * @author AI Assistant
 * @version 1.0
 */
public class PermissionResult {
    private boolean allowed;
    private String message;
    
    public PermissionResult() {
    }
    
    public PermissionResult(boolean allowed, String message) {
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