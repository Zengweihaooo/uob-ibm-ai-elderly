package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * User entity representing a user in the IBM AI Elderly Project
 * 
 * This class manages user registration states and verification codes
 * for the pet reminder application.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
public class User {
    
    /**
     * User registration status enumeration
     */
    public enum UserStatus {
        UNREGISTERED,    // User has not started registration
        PENDING,         // User registered but not verified email
        VERIFIED         // User has verified email and is fully registered
    }
    
    private String email;
    private String verificationCode;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime codeExpiresAt;
    
    /**
     * Default constructor
     */
    public User() {
        this.status = UserStatus.UNREGISTERED;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with email
     * 
     * @param email The user's email address
     */
    public User(String email) {
        this();
        this.email = email;
    }
    
    // Getters and Setters
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    public LocalDateTime getCodeExpiresAt() {
        return codeExpiresAt;
    }
    
    public void setCodeExpiresAt(LocalDateTime codeExpiresAt) {
        this.codeExpiresAt = codeExpiresAt;
    }
    
    /**
     * Check if the verification code has expired
     * 
     * @return true if code has expired, false otherwise
     */
    public boolean isCodeExpired() {
        return codeExpiresAt != null && LocalDateTime.now().isAfter(codeExpiresAt);
    }
    
    /**
     * Mark user as verified
     */
    public void verify() {
        this.status = UserStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
        this.verificationCode = null;
        this.codeExpiresAt = null;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", verifiedAt=" + verifiedAt +
                '}';
    }
} 