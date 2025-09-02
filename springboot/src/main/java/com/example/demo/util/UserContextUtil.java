package com.example.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User Context Utility Class
 * Used to extract user ID from Authorization header
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Component
public class UserContextUtil {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Extract user ID from Authorization header
     * 
     * @param authHeader Authorization header
     * @return User ID, returns null if invalid
     */
    public Long getUserIdFromAuthHeader(String authHeader) {
        // Check if header is null or empty
        if (authHeader == null || authHeader.trim().isEmpty()) {
            System.err.println("DEBUG: Authorization header is null or empty");
            return null;
        }
        
        // Check header format
        if (!authHeader.startsWith("Bearer ")) {
            System.err.println("DEBUG: Authorization header doesn't start with 'Bearer '");
            return null;
        }
        
        try {
            String token = authHeader.substring(7).trim(); // Remove "Bearer " prefix and trim spaces
            
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token)) {
                System.err.println("DEBUG: JWT token is null, empty, or 'null' string");
                return null;
            }
            
            // Validate token format (JWT should contain two dots)
            if (!isValidJwtFormat(token)) {
                System.err.println("DEBUG: JWT token format is invalid: " + token);
                return null;
            }
            
            // Only process JWT tokens
            if (jwtUtil.isValidToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            } else {
                System.err.println("DEBUG: JWT token validation failed");
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            System.err.println("DEBUG: Error extracting user ID from token: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Extract user email from Authorization header
     * 
     * @param authHeader Authorization header
     * @return User email, returns null if invalid
     */
    public String getEmailFromAuthHeader(String authHeader) {
        // Check if header is null or empty
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        
        // Check header format
        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            String token = authHeader.substring(7).trim(); // Remove "Bearer " prefix and trim spaces
            
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token)) {
                return null;
            }
            
            // Validate token format
            if (!isValidJwtFormat(token)) {
                return null;
            }
            
            if (jwtUtil.isValidToken(token)) {
                return jwtUtil.getEmailFromToken(token);
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            System.err.println("DEBUG: Error extracting email from token: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Validate if Authorization header is valid
     * 
     * @param authHeader Authorization header
     * @return Whether valid
     */
    public boolean isValidAuthHeader(String authHeader) {
        // Check if header is null or empty
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return false;
        }
        
        // Check header format
        if (!authHeader.startsWith("Bearer ")) {
            return false;
        }
        
        try {
            String token = authHeader.substring(7).trim();
            
            // Check if token is null or empty
            if (token == null || token.trim().isEmpty() || "null".equalsIgnoreCase(token)) {
                return false;
            }
            
            // Validate token format
            if (!isValidJwtFormat(token)) {
                return false;
            }
            
            // Only validate JWT token
            return jwtUtil.isValidToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate if JWT token format is valid
     * JWT token should contain two dot separators
     * 
     * @param token JWT token string
     * @return Whether format is valid
     */
    private boolean isValidJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // Count the number of dots
        long dotCount = token.chars().filter(ch -> ch == '.').count();
        
        // JWS (JWT Signature) should contain 2 dots
        // JWE (JWT Encryption) should contain 4 dots
        // We mainly handle JWS, so check for 2 dots
        if (dotCount != 2) {
            System.err.println("DEBUG: JWT format invalid - expected 2 dots, found " + dotCount);
            return false;
        }
        
        // Check token length (JWT typically at least 50 characters)
        if (token.length() < 50) {
            System.err.println("DEBUG: JWT token too short: " + token.length() + " characters");
            return false;
        }
        
        return true;
    }
    
    /**
     * Debug method to log token extraction process
     * 
     * @param authHeader Authorization header
     * @return Debug information
     */
    public String debugTokenExtraction(String authHeader) {
        if (authHeader == null) {
            return "Auth header is null";
        }
        
        if (authHeader.trim().isEmpty()) {
            return "Auth header is empty";
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "Auth header doesn't start with 'Bearer '";
        }
        
        String token = authHeader.substring(7).trim();
        
        if (token == null || token.isEmpty() || "null".equalsIgnoreCase(token)) {
            return "JWT token is null, empty, or 'null' string";
        }
        
        if (!isValidJwtFormat(token)) {
            return "JWT token format is invalid: " + token;
        }
        
        try {
            if (jwtUtil.isValidToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                return "Valid JWT token: " + token + " -> User ID: " + userId;
            } else {
                return "Invalid JWT token: " + token;
            }
        } catch (Exception e) {
            return "Error processing JWT token: " + e.getMessage();
        }
    }
    
    /**
     * Enhanced debug method with detailed format as requested
     * 
     * @param authHeader Authorization header
     * @return Detailed debug information
     */
    public String debugTokenExtractionDetailed(String authHeader) {
        StringBuilder debugInfo = new StringBuilder();
        debugInfo.append("=== Family API Debug Info ===\n");
        
        // 1. JwtUtil instance
        debugInfo.append("1. JwtUtil instance: ").append(jwtUtil).append("\n");
        
        // 2. JwtUtil class
        debugInfo.append("2. JwtUtil class: ").append(jwtUtil.getClass().getName()).append("\n");
        
        // 3. Auth header received
        debugInfo.append("3. Auth header received: ").append(authHeader).append("\n");
        
        // 4. Auth header length
        debugInfo.append("4. Auth header length: ").append(authHeader != null ? authHeader.length() : 0).append("\n");
        
        if (authHeader == null) {
            debugInfo.append("5. Auth header is null\n");
            debugInfo.append("6. Cannot proceed with token extraction\n");
            debugInfo.append("7. Returning null userId\n");
            return debugInfo.toString();
        }
        
        if (authHeader.trim().isEmpty()) {
            debugInfo.append("5. Auth header is empty\n");
            debugInfo.append("6. Cannot proceed with token extraction\n");
            debugInfo.append("7. Returning null userId\n");
            return debugInfo.toString();
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            debugInfo.append("5. Auth header format: INVALID (doesn't start with 'Bearer ')\n");
            debugInfo.append("6. Cannot proceed with token extraction\n");
            debugInfo.append("7. Returning null userId\n");
            return debugInfo.toString();
        }
        
        // 5. Auth header format check
        debugInfo.append("5. Auth header format: VALID (starts with 'Bearer ')\n");
        
        // 6. Extract token
        String token = authHeader.substring(7).trim();
        debugInfo.append("6. Extracted token: '").append(token).append("'\n");
        
        // 7. Token validation
        if (token == null || token.isEmpty() || "null".equalsIgnoreCase(token)) {
            debugInfo.append("7. Token validation: FAILED (token is null, empty, or 'null' string)\n");
            debugInfo.append("8. Cannot proceed with JWT validation\n");
            debugInfo.append("9. Returning null userId\n");
            return debugInfo.toString();
        }
        
        if (!isValidJwtFormat(token)) {
            debugInfo.append("7. Token format validation: FAILED\n");
            debugInfo.append("8. Cannot proceed with JWT validation\n");
            debugInfo.append("9. Returning null userId\n");
            return debugInfo.toString();
        }
        
        debugInfo.append("7. Token format validation: PASSED\n");
        
        // 8. JWT validation
        debugInfo.append("8. Calling jwtUtil.isValidToken...\n");
        
        try {
            if (jwtUtil.isValidToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                debugInfo.append("9. JWT validation: SUCCESS\n");
                debugInfo.append("10. Extracted userId: ").append(userId).append("\n");
                debugInfo.append("11. Proceeding with request\n");
            } else {
                debugInfo.append("9. JWT validation: FAILED\n");
                debugInfo.append("10. Extracted userId: null\n");
                debugInfo.append("11. UserId is null, returning 401\n");
            }
        } catch (Exception e) {
            debugInfo.append("9. JWT validation: ERROR\n");
            debugInfo.append("10. Exception occurred: ").append(e.getMessage()).append("\n");
            debugInfo.append("11. UserId is null, returning 401\n");
        }
        
        return debugInfo.toString();
    }

}
