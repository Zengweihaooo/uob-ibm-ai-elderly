package com.example.demo.util;

import java.security.SecureRandom;

/**
 * Utility class for generating verification codes
 * 
 * This class provides methods to generate secure random verification codes
 * for user email verification in the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
public class VerificationCodeGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int MIN_VALUE = 100000; // 6-digit minimum
    private static final int MAX_VALUE = 999999; // 6-digit maximum
    
    /**
     * Generate a random 6-digit verification code
     * 
     * @return A 6-digit verification code as a String
     */
    public static String generateCode() {
        int code = random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
        return String.valueOf(code);
    }
    
    /**
     * Generate a random 6-digit verification code with custom length
     * 
     * @param length The desired length of the verification code
     * @return A verification code as a String with the specified length
     */
    public static String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Code length must be positive");
        }
        
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * Validate if a string is a valid verification code format
     * 
     * @param code The code to validate
     * @return true if the code is valid, false otherwise
     */
    public static boolean isValidCodeFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        
        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 