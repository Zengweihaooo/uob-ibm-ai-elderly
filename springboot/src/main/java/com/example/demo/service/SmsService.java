package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * SMS Service - supports mock and real SMS sending
 * 
 * Provides SMS sending for the IBM AI Elderly Project, supporting
 * mock sending in development and real sending in production.
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@Service
public class SmsService {
    
    // Configuration: enable mock mode
    @Value("${app.sms.mock:true}")
    private boolean mockMode;
    
    // Configuration: SMS provider
    @Value("${app.sms.provider:mock}")
    private String smsProvider;
    

    
    // Twilio config properties - injected from TwilioConfig bean
    @Autowired
    @Qualifier("twilioAccountSid")
    private String twilioAccountSid;
    
    @Autowired
    @Qualifier("twilioAuthToken") 
    private String twilioAuthToken;
    
    @Autowired
    @Qualifier("twilioFromNumber")
    private String twilioFromNumber;
    
    // Validation and rate/length limits
    @Value("${app.sms.phone.validation.pattern:^\\+[1-9]\\d{1,14}$}")
    private String phoneValidationPattern;
    
    @Value("${app.sms.max.message.length:160}")
    private int maxMessageLength;
    
    @Value("${app.sms.test.phone.numbers:}")
    private String testPhoneNumbers;
    
    @Value("${app.sms.debug.enabled:false}")
    private boolean debugEnabled;
    
    // In-memory SMS logs (demo purpose; DB recommended in production)
    private final List<Map<String, Object>> smsLogs = new ArrayList<>();
    private long smsIdCounter = 1;
    
    // Phone number validation pattern
    private Pattern phonePattern;
    
    /**
     * Send SMS
     * 
     * @param toPhoneNumber recipient phone number
     * @param message SMS content
     * @return send result
     */
    public Map<String, Object> sendSMS(String toPhoneNumber, String message) {
        return sendSMS(toPhoneNumber, message, "GENERAL");
    }
    
    /**
     * Send SMS (with message type)
     * 
     * @param toPhoneNumber recipient phone number
     * @param message SMS content
     * @param messageType message type (HEALTH_ALERT, EMERGENCY, GENERAL, etc.)
     * @return send result
     */
    public Map<String, Object> sendSMS(String toPhoneNumber, String message, String messageType) {
        // Validate inputs
        if (toPhoneNumber == null || toPhoneNumber.trim().isEmpty()) {
            return createErrorResponse("Phone number must not be empty");
        }
        
        if (message == null || message.trim().isEmpty()) {
            return createErrorResponse("Message content must not be empty");
        }
        
        // Format phone number
        String formattedPhone = formatPhoneNumber(toPhoneNumber.trim());
        
        try {
            Map<String, Object> result;
            
            if (mockMode) {
                // Mock SMS
                result = sendMockSMS(formattedPhone, message, messageType);
            } else {
                // Real SMS
                result = sendRealSMS(formattedPhone, message, messageType);
            }
            
            // Log SMS
            logSmsRecord(formattedPhone, message, messageType, result);
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = "SMS send failed: " + e.getMessage();
            System.err.println(errorMsg);
            
            // Log failure
            Map<String, Object> errorResult = createErrorResponse(errorMsg);
            logSmsRecord(formattedPhone, message, messageType, errorResult);
            
            return errorResult;
        }
    }
    
    /**
     * Mock SMS sending
     */
    private Map<String, Object> sendMockSMS(String phoneNumber, String message, String messageType) {
        System.out.println("=".repeat(50));
        System.out.println("📱 Mock SMS Send");
        System.out.println("=".repeat(50));
        System.out.println("To: " + phoneNumber);
        System.out.println("Type: " + messageType);
        System.out.println("Content: " + message);
        System.out.println("Sent At: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Status: ✅ Mock Sent");
        System.out.println("=".repeat(50));
        
        // Simulate network delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return createSuccessResponse("mock_" + System.currentTimeMillis(), "SMS mock sent successfully");
    }
    
    /**
     * Real SMS sending (Twilio API)
     */
    private Map<String, Object> sendRealSMS(String phoneNumber, String message, String messageType) {
        if ("twilio".equalsIgnoreCase(smsProvider)) {
            return sendTwilioSMS(phoneNumber, message, messageType);
        } else {
            throw new UnsupportedOperationException("Only Twilio SMS provider is supported, current: " + smsProvider);
        }
    }
    
    /**
     * Send SMS via Twilio
     */
    private Map<String, Object> sendTwilioSMS(String phoneNumber, String message, String messageType) {
        // Validate Twilio config
        if (twilioAccountSid.isEmpty() || twilioAuthToken.isEmpty()) {
            String errorMsg = "Twilio configuration incomplete, check account-sid and auth-token";
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        if (twilioFromNumber.isEmpty()) {
            String errorMsg = "Twilio from-number is not configured";
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        // Validate phone number format
        if (!isValidPhoneNumber(phoneNumber)) {
            String errorMsg = "Invalid phone number format: " + phoneNumber + ", use E.164 format (e.g., +1234567890)";
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        // Validate message length
        if (message.length() > maxMessageLength) {
            String errorMsg = "Message too long, length: " + message.length() + ", max allowed: " + maxMessageLength;
            System.err.println("❌ " + errorMsg);
            return createErrorResponse(errorMsg);
        }
        
        // Check test number
        if (isTestPhoneNumber(phoneNumber)) {
            if (debugEnabled) {
                System.out.println("⚠️  Test number detected, forcing mock mode: " + phoneNumber);
            }
            return sendMockSMS(phoneNumber, message, messageType);
        }
        
        try {
            if (debugEnabled) {
                System.out.println("📱 Twilio SMS Send");
                System.out.println("Account SID: " + twilioAccountSid.substring(0, 10) + "...");
                System.out.println("From: " + twilioFromNumber);
                System.out.println("To: " + phoneNumber);
                System.out.println("Type: " + messageType);
                System.out.println("Content: " + message);
            }
            
            // Init Twilio client
            Twilio.init(twilioAccountSid, twilioAuthToken);
            
            // Send SMS
            Message twilioMessage = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioFromNumber),
                message
            ).create();
            
            String messageSid = twilioMessage.getSid();
            String status = twilioMessage.getStatus().toString();
            
            if (debugEnabled) {
                System.out.println("✅ Twilio SMS Sent");
                System.out.println("MessageSID: " + messageSid);
                System.out.println("Status: " + status);
            }
            
            Map<String, Object> result = createSuccessResponse(messageSid, "Twilio SMS sent successfully");
            result.put("status", status);
            result.put("provider", "twilio");
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = "Twilio SMS send failed: " + e.getMessage();
            System.err.println("❌ " + errorMsg);
            if (debugEnabled) {
                e.printStackTrace();
            }
            return createErrorResponse(errorMsg);
        }
    }
    

    
    /** Initialize phone number pattern */
    private void initPhonePattern() {
        if (phonePattern == null) {
            phonePattern = Pattern.compile(phoneValidationPattern);
        }
    }
    
    /** Validate phone number format */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        initPhonePattern();
        return phonePattern.matcher(phoneNumber.trim()).matches();
    }
    
    /** Check if phone number is a test number */
    private boolean isTestPhoneNumber(String phoneNumber) {
        if (testPhoneNumbers == null || testPhoneNumbers.trim().isEmpty()) {
            return false;
        }
        
        String[] testNumbers = testPhoneNumbers.split(",");
        for (String testNumber : testNumbers) {
            if (phoneNumber.trim().equals(testNumber.trim())) {
                return true;
            }
        }
        return false;
    }
    
    /** Format phone number */
    private String formatPhoneNumber(String phoneNumber) {
        // Remove non-digit characters
        String digits = phoneNumber.replaceAll("[^0-9+]", "");
        
        // If it's a Mainland China mobile and missing country code, add +86
        if (digits.matches("^1[3-9]\\d{9}$")) {
            return "+86" + digits;
        }
        
        // Ensure leading + exists
        if (!digits.startsWith("+")) {
            return "+" + digits;
        }
        
        return digits;
    }
    
    /** Create success response */
    private Map<String, Object> createSuccessResponse(String messageId, String statusMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messageId", messageId);
        response.put("status", "SENT");
        response.put("message", statusMessage);
        response.put("timestamp", LocalDateTime.now());
        response.put("mockMode", mockMode);
        return response;
    }
    
    /** Create error response */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "FAILED");
        response.put("message", errorMessage);
        response.put("timestamp", LocalDateTime.now());
        response.put("mockMode", mockMode);
        return response;
    }
    
    /** Log SMS record */
    private void logSmsRecord(String phoneNumber, String message, String messageType, Map<String, Object> result) {
        Map<String, Object> log = new HashMap<>();
        log.put("id", smsIdCounter++);
        log.put("phoneNumber", phoneNumber);
        log.put("message", message);
        log.put("messageType", messageType);
        log.put("success", result.get("success"));
        log.put("status", result.get("status"));
        log.put("messageId", result.get("messageId"));
        log.put("timestamp", LocalDateTime.now());
        log.put("provider", mockMode ? "MOCK" : smsProvider.toUpperCase());
        
        smsLogs.add(log);
        
        // Limit log size (keep last 1000 records)
        if (smsLogs.size() > 1000) {
            smsLogs.remove(0);
        }
    }
    
    /**
     * Get SMS history
     * 
     * @return SMS history list
     */
    public List<Map<String, Object>> getSmsHistory() {
        return new ArrayList<>(smsLogs);
    }
    
    /**
     * Get SMS statistics
     * 
     * @return statistics map
     */
    public Map<String, Object> getSmsStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalSent = smsLogs.stream()
                .mapToLong(log -> (Boolean) log.get("success") ? 1 : 0)
                .sum();
        
        long totalFailed = smsLogs.size() - totalSent;
        
        stats.put("totalMessages", smsLogs.size());
        stats.put("successfulSent", totalSent);
        stats.put("failed", totalFailed);
        stats.put("successRate", smsLogs.isEmpty() ? 0 : (double) totalSent / smsLogs.size() * 100);
        stats.put("mockMode", mockMode);
        stats.put("provider", mockMode ? "MOCK" : smsProvider);
        
        return stats;
    }
    
    /**
     * Send health alert SMS
     * 
     * @param phoneNumber phone number
     * @param healthData health data info
     * @return result
     */
    public Map<String, Object> sendHealthAlertSMS(String phoneNumber, String healthData) {
        String message = String.format(
            "[Health Alert] Abnormal health data detected: %s. Please check immediately. - AI Elderly Companion System", 
            healthData
        );
        
        return sendSMS(phoneNumber, message, "HEALTH_ALERT");
    }
    
    /**
     * Send emergency SMS
     * 
     * @param phoneNumber phone number
     * @param emergencyInfo emergency info
     * @return result
     */
    public Map<String, Object> sendEmergencySMS(String phoneNumber, String emergencyInfo) {
        String message = String.format(
            "[Emergency Alert] %s Please contact immediately to confirm safety. - AI Elderly Companion System", 
            emergencyInfo
        );
        
        return sendSMS(phoneNumber, message, "EMERGENCY");
    }
}