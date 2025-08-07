package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.VerificationCodeGenerator;

/**
 * Service class for managing user registration and verification
 * 
 * This service handles user registration, email verification, and user status management
 * for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EmailService emailService;
    
    // Verification code expiry time in minutes
    private static final int CODE_EXPIRY_MINUTES = 15;
    
    /**
     * Register a new user or resend verification code
     * 
     * @param email The email address to register
     * @return The user object
     * @throws IllegalArgumentException if email is invalid
     */
    public User registerUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        email = email.trim().toLowerCase();
        
        User user = userMapper.findByEmail(email);
        if (user == null) {
            user = new User(email);
            userMapper.insert(user);
        }
        
        // Generate new verification code
        String verificationCode = VerificationCodeGenerator.generateCode();
        LocalDateTime codeExpiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
        
        // Update verification code in database
        userMapper.updateVerificationCode(user.getId(), verificationCode, codeExpiresAt);
        
        // Update user object
        user.setVerificationCode(verificationCode);
        user.setStatus(User.UserStatus.PENDING);
        user.setCodeExpiresAt(codeExpiresAt);
        
        // Send verification email
        emailService.sendVerificationEmail(email, verificationCode);
        
        return user;
    }
    
    /**
     * Verify a user's email with the provided verification code
     * 
     * @param email The email address
     * @param code The verification code
     * @return true if verification successful, false otherwise
     */
    public boolean verifyUser(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        User user = userMapper.findByEmail(email);
        
        if (user == null || user.getStatus() != User.UserStatus.PENDING) {
            return false;
        }
        
        if (user.isCodeExpired()) {
            return false;
        }
        
        if (code.equals(user.getVerificationCode())) {
            // Update verification status in database
            userMapper.updateVerificationStatus(user.getId(), "VERIFIED", LocalDateTime.now());
            
            // Update user object
            user.verify();
            return true;
        }
        
        return false;
    }
    
    /**
     * Delete a user by email
     * 
     * @param email The email address
     * @return true if deleted, false if not found
     */
    public boolean deleteUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        User user = userMapper.findByEmail(email);
        
        if (user == null) {
            return false;
        }
        
        int result = userMapper.deleteById(user.getId());
        return result > 0;
    }
    
    /**
     * Delete multiple users by email list
     * 
     * @param emails List of email addresses
     * @return Number of users deleted
     */
    public int deleteUsers(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (String email : emails) {
            if (deleteUser(email)) {
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
    
    /**
     * Delete users by status
     * 
     * @param status The user status to delete
     * @return Number of users deleted
     */
    public int deleteUsersByStatus(User.UserStatus status) {
        List<User> usersToDelete = userMapper.findByStatus(status.name());
        int deletedCount = 0;
        
        for (User user : usersToDelete) {
            int result = userMapper.deleteById(user.getId());
            if (result > 0) {
                deletedCount++;
            }
        }
        
        return deletedCount;
    }
    
    /**
     * Get user by email
     * 
     * @param email The email address
     * @return The user, or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        return userMapper.findByEmail(email.trim().toLowerCase());
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
    
    /**
     * Get users by status
     * 
     * @param status The user status
     * @return List of users with the specified status
     */
    public List<User> getUsersByStatus(User.UserStatus status) {
        return userMapper.findByStatus(status.name());
    }
    
    /**
     * Clean up expired verification codes
     */
    public void cleanupExpiredCodes() {
        List<User> expiredUsers = userMapper.findUsersWithExpiredCodes();
        for (User user : expiredUsers) {
            // Update status to UNREGISTERED for expired codes
            userMapper.updateVerificationStatus(user.getId(), "UNREGISTERED", null);
        }
    }
    
    /**
     * Get registration statistics
     * 
     * @return Map containing registration statistics
     */
    public Map<String, Integer> getRegistrationStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        stats.put("total", userMapper.countByStatus("VERIFIED"));
        stats.put("pending", userMapper.countByStatus("PENDING"));
        stats.put("unregistered", userMapper.countByStatus("UNREGISTERED"));
        
        return stats;
    }
    
    /**
     * Get all doctors
     * 
     * @return List of doctors
     */
    public List<User> getDoctors() {
        return userMapper.findByRole("DOCTOR");
    }
    
    /**
     * Get all family members
     * 
     * @return List of family members
     */
    public List<User> getFamilyMembers() {
        return userMapper.findByRole("FAMILY");
    }
    
    /**
     * Check if user is a doctor
     * 
     * @param userId The user ID
     * @return true if user is a doctor, false otherwise
     */
    public boolean isDoctor(Long userId) {
        User user = userMapper.findById(userId);
        return user != null && user.getRole() == User.UserRole.DOCTOR;
    }
    
    /**
     * Check if user is a family member
     * 
     * @param userId The user ID
     * @return true if user is a family member, false otherwise
     */
    public boolean isFamilyMember(Long userId) {
        User user = userMapper.findById(userId);
        return user != null && user.getRole() == User.UserRole.FAMILY;
    }
    
    /**
     * Get user by ID
     * 
     * @param userId The user ID
     * @return The user, or null if not found
     */
    public User getUserById(Long userId) {
        return userMapper.findById(userId);
    }
    
    /**
     * Set user role
     * 
     * @param email The user email
     * @param role The new role
     * @return true if updated, false otherwise
     */
    public boolean setUserRole(String email, User.UserRole role) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return false;
        }
        
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        int result = userMapper.update(user);
        return result > 0;
    }
    
    /**
     * Get user role
     * 
     * @param userId The user ID
     * @return The user role, or null if user not found
     */
    public User.UserRole getUserRole(Long userId) {
        User user = userMapper.findById(userId);
        return user != null ? user.getRole() : null;
    }
} 