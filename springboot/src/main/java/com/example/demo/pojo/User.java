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
    
    /**
     * User role enumeration
     */
    public enum UserRole {
        ELDERLY,         // 老年人用户
        FAMILY,          // 家庭成员
        DOCTOR,          // 医生
        ADMIN            // 管理员
    }
    
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String name;
    private String verificationCode;
    private UserStatus status;
    private UserRole role;
    private String phoneNumber;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime codeExpiresAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public User() {
        this.status = UserStatus.UNREGISTERED;
        this.role = UserRole.ELDERLY; // 默认为老年人用户
        this.isVerified = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with email
     * 
     * @param email The user's email address
     */
    public User(String email) {
        this();
        this.email = email;
        this.username = email.split("@")[0]; // Use email prefix as username
        this.name = email.split("@")[0]; // Use email prefix as name
    }

    /**
     * Constructor with email and name
     * 
     * @param email The user's email address
     * @param name The user's name
     */
    public User(String email, String name) {
        this(email);
        this.name = name;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.verificationCode = null;
        this.codeExpiresAt = null;
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", role=" + role +
                ", isVerified=" + isVerified +
                ", createdAt=" + createdAt +
                ", verifiedAt=" + verifiedAt +
                '}';
    }
} 