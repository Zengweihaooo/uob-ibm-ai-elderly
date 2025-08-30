package com.example.demo.controller;

import com.example.demo.pojo.User;
import com.example.demo.pojo.EmailVerificationResult;
import com.example.demo.pojo.PermissionResult;
import com.example.demo.pojo.RateLimitResult;
import com.example.demo.service.UserService;
import com.example.demo.service.EmailVerificationService;
import com.example.demo.service.PermissionService;
import com.example.demo.service.PasswordResetService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for handling user registration and verification
 * 
 * This controller manages user registration, email verification, and user status
 * for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Controller
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Display the user registration page
     * 
     * @param model Spring MVC model to add attributes
     * @return The registration template name
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Register a new user and send verification email (HTML form submission)
     * 
     * @param email The email address to register
     * @param model Spring MVC model to add attributes
     * @return The registration template name with success/error message
     */
    @PostMapping("/register")
    public String registerUser(@RequestParam("email") String email, Model model) {
        try {
            User user = userService.registerUser(email);
            model.addAttribute("message", "Verification email sent to: " + email + ". Please check your inbox and enter the 6-digit code.");
            model.addAttribute("messageType", "success");
            model.addAttribute("showVerification", true);
            model.addAttribute("userEmail", email);
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("messageType", "error");
        } catch (Exception e) {
            model.addAttribute("message", "Failed to send verification email. Please try again.");
            model.addAttribute("messageType", "error");
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Register a new user and send verification email (API endpoint for AJAX)
     * 
     * @param email The email address to register
     * @return JSON response with result
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUserAPI(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.registerUser(email);
            response.put("success", true);
            response.put("message", "Verification email sent to: " + email + ". Please check your inbox!");
            response.put("email", email);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send verification email. Please try again.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Verify user email with verification code (HTML form submission)
     * 
     * @param email The email address
     * @param code The verification code
     * @param model Spring MVC model to add attributes
     * @return The registration template name with verification result
     */
    @PostMapping("/verify")
    public String verifyUser(@RequestParam("email") String email, 
                           @RequestParam("code") String code, 
                           Model model) {
        boolean verified = userService.verifyUser(email, code);
        
        if (verified) {
            model.addAttribute("message", "Email verification successful! Welcome to Pet Reminder App.");
            model.addAttribute("messageType", "success");
        } else {
            model.addAttribute("message", "Invalid or expired verification code. Please try again or request a new code.");
            model.addAttribute("messageType", "error");
            model.addAttribute("showVerification", true);
            model.addAttribute("userEmail", email);
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Verify user email with verification code (API endpoint for AJAX)
     * 
     * @param email The email address
     * @param code The verification code
     * @return JSON response with result
     */
    @PostMapping("/api/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyUserAPI(@RequestParam("email") String email, 
                                                           @RequestParam("code") String code) {
        Map<String, Object> response = new HashMap<>();
        boolean verified = userService.verifyUser(email, code);
        
        if (verified) {
            response.put("success", true);
            response.put("message", "Email verification successful! Welcome to Pet Reminder App.");
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired verification code. Please try again or request a new code.");
        }
        
        response.put("email", email);
        return ResponseEntity.ok(response);
    }

    /**
     * Resend verification code (HTML form submission)
     * 
     * @param email The email address
     * @param model Spring MVC model to add attributes
     * @return The registration template name with result message
     */
    @PostMapping("/resend")
    public String resendVerificationCode(@RequestParam("email") String email, Model model) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                model.addAttribute("message", "Email not found. Please register first.");
                model.addAttribute("messageType", "error");
            } else if (user.getStatus() == User.UserStatus.VERIFIED) {
                model.addAttribute("message", "Email is already verified.");
                model.addAttribute("messageType", "info");
            } else {
                userService.registerUser(email); // This will generate and send a new code
                model.addAttribute("message", "New verification code sent to: " + email);
                model.addAttribute("messageType", "success");
                model.addAttribute("showVerification", true);
                model.addAttribute("userEmail", email);
            }
        } catch (Exception e) {
            model.addAttribute("message", "Failed to resend verification code. Please try again.");
            model.addAttribute("messageType", "error");
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Resend verification code (API endpoint for AJAX)
     * 
     * @param email The email address
     * @return JSON response with result
     */
    @PostMapping("/api/resend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendVerificationCodeAPI(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Email not found. Please register first.");
                return ResponseEntity.badRequest().body(response);
            } else if (user.getStatus() == User.UserStatus.VERIFIED) {
                response.put("success", false);
                response.put("message", "Email is already verified.");
                return ResponseEntity.badRequest().body(response);
            } else {
                userService.registerUser(email); // This will generate and send a new code
                response.put("success", true);
                response.put("message", "New verification code sent to: " + email);
                response.put("email", email);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to resend verification code. Please try again.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * User login API endpoint
     * 
     * @param email The email address
     * @param password The password
     * @return JSON response with login result
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUser(@RequestParam("email") String email, 
                                                        @RequestParam("password") String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean loginSuccess = userService.loginUser(email, password);
            
            if (loginSuccess) {
                User user = userService.getUserByEmail(email);
                
                // 生成JWT token
                String token = jwtUtil.generateToken(user.getId(), user.getEmail());
                
                response.put("success", true);
                response.put("message", "Login successful! Welcome back.");
                response.put("token", token);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "name", user.getName() != null ? user.getName() : user.getUsername(),
                    "role", user.getRole().toString()
                ));
            } else {
                response.put("success", false);
                response.put("message", "Invalid email or password. Please check your credentials.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Complete user registration API endpoint
     * 
     * @param email The email address
     * @param name The user's full name
     * @param password The password
     * @param role The user role
     * @return JSON response with completion result
     */
    @PostMapping("/api/complete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completeRegistration(@RequestParam("email") String email,
                                                                   @RequestParam("name") String name,
                                                                   @RequestParam("password") String password,
                                                                   @RequestParam("role") String role) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean completed = userService.completeRegistration(email, name, password, role);
            
            if (completed) {
                response.put("success", true);
                response.put("message", "Registration completed successfully! You can now sign in.");
                response.put("email", email);
            } else {
                response.put("success", false);
                response.put("message", "Registration completion failed. Please verify your email first.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration completion failed: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a single user by email
     * 
     * @param email The email address of the user to delete
     * @param model Spring MVC model to add attributes
     * @return The registration template name with result message
     */
    @PostMapping("/delete")
    public String deleteUser(@RequestParam("email") String email, Model model) {
        boolean deleted = userService.deleteUser(email);
        
        if (deleted) {
            model.addAttribute("message", "User " + email + " has been successfully deleted.");
            model.addAttribute("messageType", "success");
        } else {
            model.addAttribute("message", "User " + email + " not found or could not be deleted.");
            model.addAttribute("messageType", "error");
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Delete multiple users by email addresses
     * 
     * @param emails Comma-separated list of email addresses
     * @param model Spring MVC model to add attributes
     * @return The registration template name with result message
     */
    @PostMapping("/delete-multiple")
    public String deleteMultipleUsers(@RequestParam("emails") String emails, Model model) {
        if (emails == null || emails.trim().isEmpty()) {
            model.addAttribute("message", "No email addresses provided for deletion.");
            model.addAttribute("messageType", "error");
        } else {
            List<String> emailList = Arrays.asList(emails.split(","));
            int deletedCount = userService.deleteUsers(emailList);
            
            if (deletedCount > 0) {
                model.addAttribute("message", "Successfully deleted " + deletedCount + " user(s).");
                model.addAttribute("messageType", "success");
            } else {
                model.addAttribute("message", "No users were deleted. Please check the email addresses.");
                model.addAttribute("messageType", "error");
            }
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Delete all users with a specific status
     * 
     * @param status The user status (PENDING, VERIFIED, UNREGISTERED)
     * @param model Spring MVC model to add attributes
     * @return The registration template name with result message
     */
    @PostMapping("/delete-by-status")
    public String deleteUsersByStatus(@RequestParam("status") String status, Model model) {
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status.toUpperCase());
            int deletedCount = userService.deleteUsersByStatus(userStatus);
            
            if (deletedCount > 0) {
                model.addAttribute("message", "Successfully deleted " + deletedCount + " user(s) with status: " + status);
                model.addAttribute("messageType", "success");
            } else {
                model.addAttribute("message", "No users found with status: " + status);
                model.addAttribute("messageType", "info");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", "Invalid status: " + status + ". Valid statuses are: PENDING, VERIFIED, UNREGISTERED");
            model.addAttribute("messageType", "error");
        }
        
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("stats", userService.getRegistrationStats());
        return "registration";
    }

    /**
     * Get user information as JSON (for API calls)
     * 
     * @param email The email address
     * @return User information
     */
    @GetMapping("/info")
    @ResponseBody
    public Map<String, Object> getUserInfo(@RequestParam("email") String email) {
        User user = userService.getUserByEmail(email);
        Map<String, Object> response = Map.of(
            "exists", user != null,
            "status", user != null ? user.getStatus().toString() : "NOT_FOUND",
            "email", email
        );
        return response;
    }

    /**
     * Get registration statistics as JSON
     * 
     * @return Registration statistics
     */
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Integer> getStats() {
        return userService.getRegistrationStats();
    }

    /**
     * Delete user via API (for AJAX calls)
     * 
     * @param email The email address to delete
     * @return JSON response with result
     */
    @DeleteMapping("/api/delete")
    @ResponseBody
    public Map<String, Object> deleteUserAPI(@RequestParam("email") String email) {
        boolean deleted = userService.deleteUser(email);
        return Map.of(
            "success", deleted,
            "message", deleted ? "User deleted successfully" : "User not found",
            "email", email
        );
    }

    /**
     * Verify authentication token (for schedule page)
     * 
     * @param authHeader Authorization header with Bearer token
     * @return JSON response with user info or error
     */
    @PostMapping("/api/verify-token")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check if authorization header is present and valid
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Invalid or missing authorization token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        
        // For demo purposes, accept any token that starts with "demo-token-"
        // In a real application, you would validate the JWT token properly
        if (token.startsWith("demo-token-")) {
            response.put("success", true);
            response.put("user", Map.of(
                "name", "Demo User",
                "email", "demo@example.com",
                "id", 1
            ));
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Invalid token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * 邮箱验证API
     */
    @PostMapping("/api/validate-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> validateEmail(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 权限验证
            PermissionResult permissionResult = permissionService.validateRegistrationPermission(authHeader);
            if (!permissionResult.isAllowed()) {
                response.put("success", false);
                response.put("message", "权限不足: " + permissionResult.getMessage());
                response.put("errorCode", "PERMISSION_DENIED");
                return ResponseEntity.status(403).body(response);
            }
            
            // 2. 输入验证
            String email = (String) request.get("email");
            String context = (String) request.getOrDefault("context", "USER_REGISTRATION");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email address cannot be empty");
                response.put("errorCode", "EMAIL_EMPTY");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 3. 请求频率限制
            RateLimitResult rateLimitResult = permissionService.checkRateLimit(authHeader, "email_validation");
            if (!rateLimitResult.isAllowed()) {
                response.put("success", false);
                response.put("message", "Request too frequent, please try again later");
                response.put("errorCode", "RATE_LIMIT_EXCEEDED");
                return ResponseEntity.status(429).body(response);
            }
            
            // 4. 邮箱验证
            EmailVerificationResult verificationResult = emailVerificationService.verifyEmailForRegistration(email.trim());
            
            response.put("success", verificationResult.isSuccess());
            response.put("message", verificationResult.getMessage());
            response.put("errorCode", verificationResult.getErrorCode());
            response.put("email", verificationResult.getEmail());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error occurred during validation: " + e.getMessage());
            response.put("errorCode", "VALIDATION_ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 增强版用户注册API
     */
    @PostMapping("/api/register-enhanced")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUserEnhanced(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. 权限验证
            PermissionResult permissionResult = permissionService.validateRegistrationPermission(authHeader);
            if (!permissionResult.isAllowed()) {
                response.put("success", false);
                response.put("message", "Insufficient permissions: " + permissionResult.getMessage());
                response.put("errorCode", "PERMISSION_DENIED");
                return ResponseEntity.status(403).body(response);
            }
            
            // 2. 输入验证
            String email = (String) request.get("email");
            String timestamp = (String) request.get("timestamp");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email address cannot be empty");
                response.put("errorCode", "EMAIL_EMPTY");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 3. 时间戳验证（防止重放攻击）
            if (!isValidTimestamp(timestamp)) {
                response.put("success", false);
                response.put("message", "Request timestamp is invalid");
                response.put("errorCode", "INVALID_TIMESTAMP");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 4. 请求频率限制
            RateLimitResult rateLimitResult = permissionService.checkRateLimit(authHeader, "user_registration");
            if (!rateLimitResult.isAllowed()) {
                response.put("success", false);
                response.put("message", "Registration request too frequent, please try again later");
                response.put("errorCode", "RATE_LIMIT_EXCEEDED");
                return ResponseEntity.status(429).body(response);
            }
            
            // 5. 邮箱验证（再次验证）
            EmailVerificationResult verificationResult = emailVerificationService.verifyEmailForRegistration(email.trim());
            if (!verificationResult.isSuccess()) {
                response.put("success", false);
                response.put("message", verificationResult.getMessage());
                response.put("errorCode", verificationResult.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }
            
            // 6. 业务逻辑处理
            User user = userService.registerUser(email.trim());
            
            response.put("success", true);
            response.put("message", "Registration successful! Verification email sent to: " + user.getEmail());
            response.put("user", user);
            response.put("email", user.getEmail());
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            response.put("errorCode", "VALIDATION_ERROR");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed, please try again later");
            response.put("errorCode", "REGISTRATION_ERROR");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Send password reset code to user's email
     * 
     * @param email The email address to send reset code to
     * @return JSON response with result
     */
    @PostMapping("/api/forgot-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam("email") String email) {
        Map<String, Object> result = passwordResetService.sendPasswordResetCode(email);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Reset password with verification code
     * 
     * @param email The user's email address
     * @param resetCode The reset verification code
     * @param newPassword The new password
     * @return JSON response with result
     */
    @PostMapping("/api/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestParam("email") String email,
            @RequestParam("resetCode") String resetCode,
            @RequestParam("newPassword") String newPassword) {
        
        Map<String, Object> result = passwordResetService.resetPassword(email, resetCode, newPassword);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * Admin function to quickly reset user password (for your current situation)
     * 
     * @param email The user's email address
     * @param newPassword The new password
     * @return JSON response with result
     */
    @PostMapping("/api/admin/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminResetPassword(
            @RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {
        
        Map<String, Object> result = passwordResetService.adminResetPassword(email, newPassword);
        
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 验证时间戳
     */
    private boolean isValidTimestamp(String timestamp) {
        try {
            if (timestamp == null) return false;
            
            LocalDateTime requestTime = LocalDateTime.parse(timestamp);
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(requestTime, now);
            
            // Allow 5 minutes time difference
            return Math.abs(duration.toMinutes()) <= 5;
        } catch (Exception e) {
            return false;
        }
    }
} 