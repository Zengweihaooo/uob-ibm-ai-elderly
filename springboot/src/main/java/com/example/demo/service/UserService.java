package com.example.demo.service;

import com.example.demo.pojo.User;
import com.example.demo.util.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // In-memory storage for users (consider using database in production)
    private final Map<String, User> users = new ConcurrentHashMap<>();
    
    // Verification code expiry time in minutes
    private static final int CODE_EXPIRY_MINUTES = 15;
    
    @Autowired
    private EmailService emailService;
    
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
        
        User user = users.get(email);
        if (user == null) {
            user = new User(email);
            users.put(email, user);
        }
        
        // Generate new verification code
        String verificationCode = VerificationCodeGenerator.generateCode();
        user.setVerificationCode(verificationCode);
        user.setStatus(User.UserStatus.PENDING);
        user.setCodeExpiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        
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
        User user = users.get(email);
        
        if (user == null || user.getStatus() != User.UserStatus.PENDING) {
            return false;
        }
        
        if (user.isCodeExpired()) {
            return false;
        }
        
        if (code.equals(user.getVerificationCode())) {
            user.verify();
            return true;
        }
        
        return false;
    }
    
    /**
     * Delete a user by email address
     * 
     * @param email The email address of the user to delete
     * @return true if user was deleted, false if user was not found
     */
    public boolean deleteUser(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        email = email.trim().toLowerCase();
        User removedUser = users.remove(email);
        return removedUser != null;
    }
    
    /**
     * Delete multiple users by email addresses
     * 
     * @param emails List of email addresses to delete
     * @return Number of users successfully deleted
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
     * Delete all users with a specific status
     * 
     * @param status The user status to filter by for deletion
     * @return Number of users deleted
     */
    public int deleteUsersByStatus(User.UserStatus status) {
        List<String> emailsToDelete = users.values().stream()
                .filter(user -> user.getStatus() == status)
                .map(User::getEmail)
                .toList();
        
        return deleteUsers(emailsToDelete);
    }
    
    /**
     * Get user by email
     * 
     * @param email The email address
     * @return The user object or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return users.get(email.trim().toLowerCase());
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Get users by status
     * 
     * @param status The user status to filter by
     * @return List of users with the specified status
     */
    public List<User> getUsersByStatus(User.UserStatus status) {
        return users.values().stream()
                .filter(user -> user.getStatus() == status)
                .toList();
    }
    
    /**
     * Delete expired verification codes
     * This method should be called periodically to clean up expired codes
     */
    public void cleanupExpiredCodes() {
        users.values().removeIf(user -> 
            user.getStatus() == User.UserStatus.PENDING && user.isCodeExpired()
        );
    }
    
    /**
     * Get registration statistics
     * 
     * @return Map containing user counts by status
     */
    public Map<String, Integer> getRegistrationStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", users.size());
        stats.put("pending", (int) users.values().stream().filter(u -> u.getStatus() == User.UserStatus.PENDING).count());
        stats.put("verified", (int) users.values().stream().filter(u -> u.getStatus() == User.UserStatus.VERIFIED).count());
        return stats;
    }
} 