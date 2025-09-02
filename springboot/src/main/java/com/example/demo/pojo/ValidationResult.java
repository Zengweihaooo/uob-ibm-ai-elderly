package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * Validation result class
 * 
 * @author AI Assistant
 * @version 1.0
 */
public class ValidationResult {
    private boolean valid;
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    
    public ValidationResult() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ValidationResult(boolean valid, String message) {
        this();
        this.valid = valid;
        this.message = message;
    }
    
    public ValidationResult(boolean valid, String message, String errorCode) {
        this(valid, message);
        this.errorCode = errorCode;
    }
    
    // Getters and Setters
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}