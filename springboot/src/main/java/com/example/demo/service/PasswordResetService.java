package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.User;
import com.example.demo.util.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Password Reset Service
 * 
 * Handles password reset functionality including sending reset codes
 * and validating reset requests.
 * 
 * @author System Generated
 * @version 1.0
 */
@Service
public class PasswordResetService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EmailService emailService;
    
    // Reset code expiry time in minutes
    private static final int RESET_CODE_EXPIRY_MINUTES = 30;
    
    /**
     * Send password reset code to user's email
     * 
     * @param email The user's email address
     * @return Result map with success status and message
     */
    public Map<String, Object> sendPasswordResetCode(String email) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            email = email.trim().toLowerCase();
            
            // Check if user exists and is verified
            User user = userMapper.findByEmail(email);
            if (user == null) {
                result.put("success", false);
                result.put("message", "Email address not found. Please register first.");
                return result;
            }
            
            if (user.getStatus() != User.UserStatus.VERIFIED) {
                result.put("success", false);
                result.put("message", "Email address is not verified. Please complete registration first.");
                return result;
            }
            
            // Generate reset code
            String resetCode = VerificationCodeGenerator.generateCode();
            LocalDateTime codeExpiresAt = LocalDateTime.now().plusMinutes(RESET_CODE_EXPIRY_MINUTES);
            
            // Send reset email
            emailService.sendPasswordResetEmail(email, resetCode);
            
            // Update user with reset code
            userMapper.updateVerificationCode(user.getId(), resetCode, codeExpiresAt);
            
            result.put("success", true);
            result.put("message", "Password reset code sent to your email. Please check your inbox!");
            result.put("email", email);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to send reset code: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Reset password with verification code
     * 
     * @param email The user's email address
     * @param resetCode The reset verification code
     * @param newPassword The new password
     * @return Result map with success status and message
     */
    public Map<String, Object> resetPassword(String email, String resetCode, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            email = email.trim().toLowerCase();
            
            // Find user
            User user = userMapper.findByEmail(email);
            if (user == null) {
                result.put("success", false);
                result.put("message", "Email address not found.");
                return result;
            }
            
            // Check if user is verified
            if (user.getStatus() != User.UserStatus.VERIFIED) {
                result.put("success", false);
                result.put("message", "Email address is not verified.");
                return result;
            }
            
            // Check reset code
            if (resetCode == null || !resetCode.equals(user.getVerificationCode())) {
                result.put("success", false);
                result.put("message", "Invalid reset code. Please request a new reset code.");
                return result;
            }
            
            // Check if code is expired
            if (user.isCodeExpired()) {
                result.put("success", false);
                result.put("message", "Reset code has expired. Please request a new reset code.");
                return result;
            }
            
            // Validate new password
            if (newPassword == null || newPassword.trim().length() < 6) {
                result.put("success", false);
                result.put("message", "Password must be at least 6 characters long.");
                return result;
            }
            
            // Update password (in production, should hash the password)
            user.setPasswordHash(newPassword.trim());
            user.setUpdatedAt(LocalDateTime.now());
            user.setVerificationCode(null);
            user.setCodeExpiresAt(null);
            
            // Update in database
            userMapper.update(user);
            
            result.put("success", true);
            result.put("message", "Password reset successful! You can now login with your new password.");
            result.put("email", email);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to reset password: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Quick password reset for existing users (admin function)
     * 
     * @param email The user's email address
     * @param newPassword The new password
     * @return Result map with success status and message
     */
    public Map<String, Object> adminResetPassword(String email, String newPassword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            email = email.trim().toLowerCase();
            
            User user = userMapper.findByEmail(email);
            if (user == null) {
                result.put("success", false);
                result.put("message", "User not found.");
                return result;
            }
            
            // Update password
            user.setPasswordHash(newPassword);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            
            result.put("success", true);
            result.put("message", "Password reset successful.");
            result.put("email", email);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to reset password: " + e.getMessage());
        }
        
        return result;
    }
}
