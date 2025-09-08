package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;

/**
 * Service class for managing family contacts and family-related operations
 * 
 * This service handles family contact management, emergency notifications,
 * and family member communication for the IBM AI Elderly Project.
 * 
 * @author Weihao Zeng
 * @version 2.0 - Data persistence and per-user data isolation implemented
 */
@Service
public class FamilyService {

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private SmsService smsService;

    @Autowired
    private FamilyContactMapper familyContactMapper;

    // Debug flag
    private static final boolean DEBUG_ENABLED = true;
    
    // 手机号验证正则表达式
    private static final String PHONE_VALIDATION_PATTERN = "^\\+?[1-9]\\d{1,14}$";
    private static final String CHINA_MOBILE_REGEX = "^(\\+86)?1[3-9]\\d{9}$";
    private static final String EMAIL_VALIDATION_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_VALIDATION_PATTERN);
    private static final Pattern CHINA_MOBILE_PATTERN = Pattern.compile(CHINA_MOBILE_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_VALIDATION_REGEX);

    /**
     * Add a new family contact
     * 
     * @param userId User ID
     * @param name Contact name
     * @param phone Phone number
     * @param email Email address
     * @param relationship Relationship type
     * @param isEmergencyContact Whether this is an emergency contact
     * @return Created family contact
     */
    public FamilyContact addFamilyContact(Long userId, String name, String phone, String email,
                                       String relationship, Boolean isEmergencyContact) {
        
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.addFamilyContact DEBUG ====");
            System.out.println("User ID: " + userId);
            System.out.println("Contact name: " + name);
            System.out.println("Phone: " + phone);
            System.out.println("Email: " + email);
            System.out.println("Relationship: " + relationship);
            System.out.println("Is emergency contact: " + isEmergencyContact);
            System.out.println("Existing contacts count: " + getFamilyContacts(userId).size());
        }
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Validation failed - empty contact name");
            }
            throw new IllegalArgumentException("Contact name is required");
        }

        if ((phone == null || phone.trim().isEmpty()) && 
            (email == null || email.trim().isEmpty())) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Validation failed - both phone and email are empty");
            }
            throw new IllegalArgumentException("Either phone number or email is required");
        }
        
        // 验证手机号格式（如果提供了手机号）
        if (phone != null && !phone.trim().isEmpty()) {
            String validationError = getPhoneValidationError(phone);
            if (validationError != null) {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: Validation failed - " + validationError + ": " + phone);
                }
                throw new IllegalArgumentException(validationError);
            }
        }
        
        // 验证邮箱格式（如果提供了邮箱）
        if (email != null && !email.trim().isEmpty()) {
            if (!isValidEmail(email)) {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: Validation failed - invalid email format: " + email);
                }
                throw new IllegalArgumentException("Invalid email format. Please provide a valid email address");
            }
        }

        // Create new contact
        FamilyContact contact = new FamilyContact();
        contact.setUserId(userId);
        contact.setName(name.trim());
        contact.setPhone(phone != null ? formatPhoneNumber(phone.trim()) : null);
        contact.setEmail(email != null ? email.trim() : null);
        contact.setRelationship(relationship != null ? relationship.trim() : "other");
        contact.setIsEmergencyContact(isEmergencyContact != null ? isEmergencyContact : false);
        contact.setIsActive(true);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        // Save to database
        try {
            int result = familyContactMapper.insert(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Contact saved to database successfully");
                    System.out.println("DB result: " + result);
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: DB insert failed");
                }
                throw new RuntimeException("Failed to save contact to database");
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: DB operation exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            throw new RuntimeException("Database operation failed: " + e.getMessage());
        }

        if (DEBUG_ENABLED) {
            System.out.println("DEBUG: Contact created successfully");
            System.out.println("Assigned contact ID: " + contact.getId());
            System.out.println("Contact created at: " + contact.getCreatedAt());
            System.out.println("Current contact count: " + getFamilyContacts(userId).size());
            System.out.println("============================================");
        }

        System.out.println("Family contact added: " + contact.getName() + " for user " + userId);
        return contact;
    }

    /**
     * Get all family contacts for a user
     * 
     * @param userId User ID
     * @return List of family contacts
     */
    public List<FamilyContact> getFamilyContacts(Long userId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.getFamilyContacts DEBUG ====");
            System.out.println("Query user ID: " + userId);
        }
        
        try {
            List<FamilyContact> userContacts = familyContactMapper.findActiveByUserId(userId);
            
            if (DEBUG_ENABLED) {
                System.out.println("Contact count for user: " + userContacts.size());
                System.out.println("Contact details:");
                for (FamilyContact contact : userContacts) {
                    System.out.println("  - ID: " + contact.getId() + ", Name: " + contact.getName() + 
                                     ", Relationship: " + contact.getRelationship() + 
                                     ", Emergency: " + contact.getIsEmergencyContact());
                }
                System.out.println("==============================================");
            }
            
            return userContacts;
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: DB query exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            // Return empty list instead of throwing, to keep stability
            return new ArrayList<>();
        }
    }

    /**
     * Get a specific family contact by ID
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return Family contact or null if not found
     */
    public FamilyContact getFamilyContact(Long userId, Long contactId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.getFamilyContact DEBUG ====");
            System.out.println("Query user ID: " + userId);
            System.out.println("Query contact ID: " + contactId);
        }
        
        try {
            FamilyContact contact = familyContactMapper.findById(contactId);
            
            // Validate data isolation: only access own contacts
            if (contact != null && contact.getUserId().equals(userId) && contact.getIsActive()) {
                if (DEBUG_ENABLED) {
                    System.out.println("Found contact: " + contact.getName() + ", Relationship: " + contact.getRelationship());
                }
                return contact;
            } else {
                if (DEBUG_ENABLED) {
                    if (contact == null) {
                        System.out.println("Contact not found");
                    } else if (!contact.getUserId().equals(userId)) {
                        System.out.println("Data isolation check failed: user ID mismatch");
                    } else if (!contact.getIsActive()) {
                        System.out.println("Contact is deleted/inactive");
                    }
                }
                return null;
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: DB query exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Update a family contact
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @param contactData Updated contact data
     * @return Updated family contact or null if not found
     */
    public FamilyContact updateFamilyContact(Long userId, Long contactId, Map<String, Object> contactData) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.updateFamilyContact DEBUG ====");
            System.out.println("Update user ID: " + userId);
            System.out.println("Update contact ID: " + contactId);
            System.out.println("Update data: " + contactData);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Contact not found for update");
                System.out.println("===========================================");
            }
            return null;
        }

        if (DEBUG_ENABLED) {
            System.out.println("Contact before update: " + contact.getName());
        }

        // Update fields if provided
        if (contactData.containsKey("name")) {
            String name = (String) contactData.get("name");
            if (name != null && !name.trim().isEmpty()) {
                contact.setName(name.trim());
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Update name to: " + name.trim());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: Name validation failed - must not be empty");
                }
                throw new IllegalArgumentException("Contact name cannot be empty");
            }
        }

        if (contactData.containsKey("phone")) {
            String newPhone = (String) contactData.get("phone");
            if (newPhone != null && !newPhone.trim().isEmpty()) {
                String validationError = getPhoneValidationError(newPhone);
                if (validationError != null) {
                    if (DEBUG_ENABLED) {
                        System.err.println("DEBUG: Phone validation failed - " + validationError + ": " + newPhone);
                    }
                    throw new IllegalArgumentException(validationError);
                }
                contact.setPhone(formatPhoneNumber(newPhone.trim()));
            } else {
                contact.setPhone(null);
            }
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Update phone to: " + contact.getPhone());
            }
        }

        if (contactData.containsKey("email")) {
            String newEmail = (String) contactData.get("email");
            if (newEmail != null && !newEmail.trim().isEmpty()) {
                if (!isValidEmail(newEmail)) {
                    if (DEBUG_ENABLED) {
                        System.err.println("DEBUG: Email validation failed - invalid format: " + newEmail);
                    }
                    throw new IllegalArgumentException("Invalid email format. Please provide a valid email address");
                }
                contact.setEmail(newEmail.trim());
            } else {
                contact.setEmail(null);
            }
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Update email to: " + contact.getEmail());
            }
        }

        if (contactData.containsKey("relationship")) {
            String newRelationship = (String) contactData.get("relationship");
            contact.setRelationship(newRelationship);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Update relationship to: " + newRelationship);
            }
        }

        if (contactData.containsKey("isEmergencyContact")) {
            Boolean isEmergency = (Boolean) contactData.get("isEmergencyContact");
            contact.setIsEmergencyContact(isEmergency);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Update emergency contact to: " + isEmergency);
            }
        }

        if (contactData.containsKey("address")) {
            String newAddress = (String) contactData.get("address");
            contact.setAddress(newAddress);
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Update address to: " + newAddress);
            }
        }

        // Validate contact has at least phone or email
        if ((contact.getPhone() == null || contact.getPhone().trim().isEmpty()) &&
            (contact.getEmail() == null || contact.getEmail().trim().isEmpty())) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Validation failed - both phone and email are empty");
            }
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        contact.setUpdatedAt(LocalDateTime.now());
        
        // Save updates to database
        try {
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Contact updated successfully");
                    System.out.println("DB update result: " + result);
                    System.out.println("Updated at: " + contact.getUpdatedAt());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: DB update failed");
                }
                throw new RuntimeException("Failed to update contact in database");
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: DB update exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            throw new RuntimeException("Database update failed: " + e.getMessage());
        }
        
        if (DEBUG_ENABLED) {
            System.out.println("==========================================");
        }
        
        return contact;
    }

    /**
     * Delete a family contact (soft delete)
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return true if deleted, false if not found
     */
    public boolean deleteFamilyContact(Long userId, Long contactId) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.deleteFamilyContact DEBUG ====");
            System.out.println("Delete user ID: " + userId);
            System.out.println("Delete contact ID: " + contactId);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Contact not found for deletion");
                System.out.println("============================================");
            }
            return false;
        }

        if (DEBUG_ENABLED) {
            System.out.println("Delete contact: " + contact.getName());
        }

        contact.setIsActive(false);
        contact.setUpdatedAt(LocalDateTime.now());
        
        // Save deletion to database
        try {
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Contact soft-deleted successfully");
                    System.out.println("DB update result: " + result);
                    System.out.println("Deleted at: " + contact.getUpdatedAt());
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: DB delete failed");
                }
                return false;
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: DB delete exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        }
        
        if (DEBUG_ENABLED) {
            System.out.println("=========================================");
        }
        
        return true;
    }

    /**
     * Send message to a family contact
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @param message Message content
     * @param messageType Message type (email/sms)
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMessageToFamily(Long userId, Long contactId, String message, String messageType) {
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.sendMessageToFamily DEBUG ====");
            System.out.println("Send message user ID: " + userId);
            System.out.println("Recipient contact ID: " + contactId);
            System.out.println("Message content: " + message);
            System.out.println("Message type: " + messageType);
        }
        
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Contact not found, cannot send message");
                System.out.println("==========================================");
            }
            return false;
        }

        if (DEBUG_ENABLED) {
            System.out.println("Target contact: " + contact.getName());
            System.out.println("Contact phone: " + contact.getPhone());
            System.out.println("Contact email: " + contact.getEmail());
        }

        try {
            if ("email".equalsIgnoreCase(messageType) && contact.getEmail() != null) {
                String subject = buildMessageSubject(messageType, contact.getName());
                String content = buildMessageContent(message, messageType, contact.getName());
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Sending email");
                    System.out.println("Email subject: " + subject);
                    System.out.println("Email content length: " + content.length());
                }
                
                emailService.sendHealthAlertEmail(contact.getEmail(), subject, content);
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Email sent successfully");
                    System.out.println("=========================================");
                }
                return true;
            } else if ("sms".equalsIgnoreCase(messageType) && contact.getPhone() != null) {
                String content = buildMessageContent(message, messageType, contact.getName());
                String phoneNumber = formatPhoneNumber(contact.getPhone());
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: Sending SMS");
                    System.out.println("Raw phone: " + contact.getPhone());
                    System.out.println("Formatted phone: " + phoneNumber);
                    System.out.println("SMS content length: " + content.length());
                }
                
                Map<String, Object> smsResult = null;
                boolean smsSuccess = false;
                if (smsService != null) {
                    smsResult = smsService.sendSMS(phoneNumber, content);
                    smsSuccess = (Boolean) smsResult.getOrDefault("success", false);
                }
                
                if (DEBUG_ENABLED) {
                    System.out.println("DEBUG: SMS send result: " + smsSuccess);
                    System.out.println("SMS response: " + smsResult);
                    System.out.println("========================================");
                }
                
                return smsSuccess;
            } else if ("general".equalsIgnoreCase(messageType)) {
                // For 'general', prefer SMS; fallback to email if no phone
                if (contact.getPhone() != null && !contact.getPhone().trim().isEmpty()) {
                    String content = buildMessageContent(message, "sms", contact.getName());
                    String phoneNumber = formatPhoneNumber(contact.getPhone());
                    
                    if (DEBUG_ENABLED) {
                        System.out.println("DEBUG: general - sending SMS");
                        System.out.println("Raw phone: " + contact.getPhone());
                        System.out.println("Formatted phone: " + phoneNumber);
                        System.out.println("SMS content length: " + content.length());
                    }
                    
                    Map<String, Object> smsResult = null;
                    boolean smsSuccess = false;
                    if (smsService != null) {
                        smsResult = smsService.sendSMS(phoneNumber, content);
                        smsSuccess = (Boolean) smsResult.getOrDefault("success", false);
                    }
                    
                    if (DEBUG_ENABLED) {
                        System.out.println("DEBUG: SMS send result: " + smsSuccess);
                        System.out.println("SMS response: " + smsResult);
                        System.out.println("========================================");
                    }
                    
                    return smsSuccess;
                } else if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                    String subject = buildMessageSubject("email", contact.getName());
                    String content = buildMessageContent(message, "email", contact.getName());
                    
                    if (DEBUG_ENABLED) {
                        System.out.println("DEBUG: general - sending email");
                        System.out.println("Email subject: " + subject);
                        System.out.println("Email content length: " + content.length());
                    }
                    
                    emailService.sendHealthAlertEmail(contact.getEmail(), subject, content);
                    
                    if (DEBUG_ENABLED) {
                        System.out.println("DEBUG: Email sent successfully");
                        System.out.println("=========================================");
                    }
                    return true;
                } else {
                    if (DEBUG_ENABLED) {
                        System.err.println("DEBUG: general - contact has neither phone nor email");
                        System.out.println("Contact phone: " + contact.getPhone());
                        System.out.println("Contact email: " + contact.getEmail());
                    }
                }
            } else {
                if (DEBUG_ENABLED) {
                    System.err.println("DEBUG: Cannot send - contact method mismatch or missing");
                    System.out.println("Message type: " + messageType);
                    System.out.println("Contact phone: " + contact.getPhone());
                    System.out.println("Contact email: " + contact.getEmail());
                }
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Exception when sending message");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            System.err.println("Failed to send message to family contact: " + e.getMessage());
        }

        if (DEBUG_ENABLED) {
            System.out.println("==========================================");
        }
        
        return false;
    }

    /**
     * Get emergency contacts for a user
     * 
     * @param userId User ID
     * @return List of emergency contacts
     */
    public List<FamilyContact> getEmergencyContacts(Long userId) {
        return familyContactMapper.findActiveByUserId(userId).stream()
                .filter(contact -> contact.getIsEmergencyContact())
                .collect(Collectors.toList());
    }

    /**
     * Get family statistics for a user
     * 
     * @param userId User ID
     * @return Map containing family statistics
     */
    public Map<String, Object> getFamilyStats(Long userId) {
        List<FamilyContact> userContacts = getFamilyContacts(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", userContacts.size());
        stats.put("emergencyContacts", userContacts.stream()
                .filter(contact -> contact.getIsEmergencyContact())
                .count());
        stats.put("activeContacts", userContacts.size());
        
        return stats;
    }

    /**
     * Send emergency notification to all emergency contacts
     * 
     * @param userId User ID
     * @param emergencyType Type of emergency
     * @param description Emergency description
     * @return Number of contacts notified
     */
    public int sendEmergencyNotification(Long userId, String emergencyType, String description) {
        List<FamilyContact> emergencyContacts = getEmergencyContacts(userId);
        int notifiedCount = 0;

        for (FamilyContact contact : emergencyContacts) {
            try {
                String message = buildEmergencyMessage(emergencyType, description, contact.getName());
                
                if (contact.getPhone() != null) {
                    if (smsService != null) {
                    smsService.sendSMS(contact.getPhone(), message);
                }
                    notifiedCount++;
                }
                
                if (contact.getEmail() != null) {
                    String subject = "Emergency Notification - " + emergencyType;
                    emailService.sendHealthAlertEmail(contact.getEmail(), subject, message);
                    notifiedCount++;
                }
            } catch (Exception e) {
                System.err.println("Failed to notify emergency contact " + contact.getName() + ": " + e.getMessage());
            }
        }

        return notifiedCount;
    }

    /**
     * Build message subject
     */
    private String buildMessageSubject(String messageType, String contactName) {
        switch (messageType.toLowerCase()) {
            case "health":
                return "Health Status Update - " + contactName;
            case "schedule":
                return "Schedule Reminder - " + contactName;
            case "emergency":
                return "Emergency Notification - " + contactName;
            default:
                return "Message Notification - " + contactName;
        }
    }

    /** Format phone number to international format */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return phoneNumber;
        }
        
        // Remove non-digit characters
        String digits = phoneNumber.replaceAll("[^0-9+]", "");
        
        // If Mainland China mobile without country code, add +86
        if (digits.matches("^1[3-9]\\d{9}$")) {
            return "+86" + digits;
        }
        
        // If US number without country code, add +1
        if (digits.matches("^[2-9]\\d{9}$")) {
            return "+1" + digits;
        }
        
        // If missing +, add +1 as default (US)
        if (!digits.startsWith("+")) {
            return "+1" + digits;
        }
        
        return digits;
    }

    /** Build message content */
    private String buildMessageContent(String message, String messageType, String contactName) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(contactName).append(",\n\n");
        content.append(message).append("\n\n");
        content.append("This message was sent automatically by the AI Elderly Companion System.\n");
        content.append("Sent at: ").append(LocalDateTime.now().toString());
        
        return content.toString();
    }

    /** Build emergency message */
    private String buildEmergencyMessage(String emergencyType, String description, String contactName) {
        return "Emergency Notification:\n" + emergencyType + "\n" + description + "\n\nContact: " + contactName;
    }

    /**
     * Get all contacts (for testing)
     */
    public List<FamilyContact> getAllContacts() {
        return familyContactMapper.findAll();
    }

    /** Clear all contacts (for testing) */
    public void clearAllContacts() {
        // Note: For testing only. Use with caution in production.
        // Implemented by soft-deleting all contacts.
        try {
            List<FamilyContact> allContacts = familyContactMapper.findAll();
            for (FamilyContact contact : allContacts) {
                contact.setIsActive(false);
                contact.setUpdatedAt(LocalDateTime.now());
                familyContactMapper.update(contact);
            }
            if (DEBUG_ENABLED) {
                System.out.println("DEBUG: Soft-deleted all contacts, count: " + allContacts.size());
            }
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Failed to clear contacts: " + e.getMessage());
            }
        }
    }

    /**
     * Validate multi-user data isolation
     * Used to test if data is correctly isolated per user after JWT integration
     * 
     * @return test result map
     */
    public Map<String, Object> testUserDataIsolation() {
        Map<String, Object> testResult = new HashMap<>();
        
        if (DEBUG_ENABLED) {
            System.out.println("==== FamilyService.testUserDataIsolation DEBUG ====");
            System.out.println("Start testing multi-user data isolation");
        }
        
        try {
            // Clean existing data
            clearAllContacts();
            
            // Create contacts for test user 1
            Long user1Id = 100L;
            FamilyContact user1Contact1 = addFamilyContact(user1Id, "张三", "13800000001", "zhangsan@example.com", "儿子", true);
            addFamilyContact(user1Id, "李四", "13800000002", "lisi@example.com", "女儿", false);
            
            // Create contacts for test user 2
            Long user2Id = 200L;
            FamilyContact user2Contact1 = addFamilyContact(user2Id, "王五", "13800000003", "wangwu@example.com", "配偶", true);
            addFamilyContact(user2Id, "赵六", "13800000004", "zhaoliu@example.com", "朋友", false);
            
            // Validate user 1 can only see own contacts
            List<FamilyContact> user1Contacts = getFamilyContacts(user1Id);
            List<FamilyContact> user2Contacts = getFamilyContacts(user2Id);
            
            // Validate isolation
            boolean isolationTest1 = user1Contacts.size() == 2;
            boolean isolationTest2 = user2Contacts.size() == 2;
            boolean isolationTest3 = user1Contacts.stream().allMatch(c -> c.getUserId().equals(user1Id));
            boolean isolationTest4 = user2Contacts.stream().allMatch(c -> c.getUserId().equals(user2Id));
            
            // Cross-user access checks
            FamilyContact crossUserAccess1 = getFamilyContact(user1Id, user2Contact1.getId());
            FamilyContact crossUserAccess2 = getFamilyContact(user2Id, user1Contact1.getId());
            boolean isolationTest5 = (crossUserAccess1 == null);
            boolean isolationTest6 = (crossUserAccess2 == null);
            
            // Summarize results
            boolean allTestsPassed = isolationTest1 && isolationTest2 && isolationTest3 && 
                                    isolationTest4 && isolationTest5 && isolationTest6;
            
            testResult.put("success", allTestsPassed);
            testResult.put("user1ContactCount", user1Contacts.size());
            testResult.put("user2ContactCount", user2Contacts.size());
            testResult.put("user1CanAccessOwnData", isolationTest1 && isolationTest3);
            testResult.put("user2CanAccessOwnData", isolationTest2 && isolationTest4);
            testResult.put("crossUserAccessBlocked", isolationTest5 && isolationTest6);
            testResult.put("totalContactsInSystem", familyContactMapper.findAll().size());
            
            if (DEBUG_ENABLED) {
                System.out.println("Test result:");
                System.out.println("User1 contact count: " + user1Contacts.size());
                System.out.println("User2 contact count: " + user2Contacts.size());
                System.out.println("Data isolation success: " + allTestsPassed);
                System.out.println("Cross-user access blocked: " + (isolationTest5 && isolationTest6));
                System.out.println("Total contacts in system: " + familyContactMapper.findAll().size());
                System.out.println("===============================================");
            }
            
            return testResult;
            
        } catch (Exception e) {
            if (DEBUG_ENABLED) {
                System.err.println("DEBUG: Data isolation test exception");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
            return testResult;
        }
    }
    
    /**
     * 获取手机号验证错误信息
     * 
     * @param phone 手机号
     * @return 错误信息，如果有效则返回null
     */
    private String getPhoneValidationError(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "手机号不能为空";
        }
        
        String cleanPhone = phone.trim().replaceAll("\\s+|-", "");
        
        // 基本格式检查
        if (!cleanPhone.matches("^\\+?[0-9]+$")) {
            return "手机号只能包含数字、空格、连字符和加号";
        }
        
        // 检查是否是中国手机号
        if (CHINA_MOBILE_PATTERN.matcher(cleanPhone).matches()) {
            return getChinaMobileValidationError(cleanPhone);
        }
        
        // 检查是否是国际格式手机号
        if (PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return getInternationalPhoneValidationError(cleanPhone);
        }
        
        return "手机号格式不正确，请使用有效的手机号码（如：13800138000 或 +8613800138000）";
    }
    
    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @return 是否有效
     */
    private boolean isValidPhoneNumber(String phone) {
        return getPhoneValidationError(phone) == null;
    }
    
    /**
     * 获取中国手机号验证错误信息
     * 
     * @param phone 已清理的手机号
     * @return 错误信息，如果有效则返回null
     */
    private String getChinaMobileValidationError(String phone) {
        // 移除国家代码进行检查
        String mobileNumber = phone.startsWith("+86") ? phone.substring(3) : phone;
        
        // 检查是否为简单重复数字（如：11111111111）
        if (isRepeatingDigits(mobileNumber)) {
            return "手机号不能是重复的相同数字（如：11111111111）";
        }
        
        // 检查是否为连续数字（如：12345678901）
        if (isConsecutiveDigits(mobileNumber)) {
            return "手机号不能是连续数字（如：12345678901）";
        }
        
        // 检查中国手机号段的合理性
        if (mobileNumber.length() == 11) {
            String prefix = mobileNumber.substring(0, 3);
            // 常见的中国手机号段
            String[] validPrefixes = {
                "130", "131", "132", "133", "134", "135", "136", "137", "138", "139", // 中国联通/移动
                "145", "147", "148", "149", // 中国移动
                "150", "151", "152", "153", "155", "156", "157", "158", "159", // 中国移动/联通
                "162", "165", "166", "167", // 中国联通
                "170", "171", "172", "173", "174", "175", "176", "177", "178", "179", // 虚拟运营商/电信
                "180", "181", "182", "183", "184", "185", "186", "187", "188", "189", // 中国电信/移动/联通
                "190", "191", "192", "193", "195", "196", "197", "198", "199" // 新号段
            };
            
            for (String validPrefix : validPrefixes) {
                if (prefix.equals(validPrefix)) {
                    return null; // 有效
                }
            }
            return "手机号段 " + prefix + " 不是有效的中国手机号段";
        }
        
        return null; // 其他长度的中国号码暂时通过
    }
    
    /**
     * 验证中国手机号的合理性
     * 
     * @param phone 已清理的手机号
     * @return 是否有效
     */
    private boolean isValidChinaMobile(String phone) {
        return getChinaMobileValidationError(phone) == null;
    }
    
    /**
     * 获取国际手机号验证错误信息
     * 
     * @param phone 已清理的手机号
     * @return 错误信息，如果有效则返回null
     */
    private String getInternationalPhoneValidationError(String phone) {
        // 移除+号进行检查
        String number = phone.startsWith("+") ? phone.substring(1) : phone;
        
        // 检查是否为简单重复数字
        if (isRepeatingDigits(number)) {
            return "国际手机号不能是重复的相同数字";
        }
        
        // 检查是否为连续数字
        if (isConsecutiveDigits(number)) {
            return "国际手机号不能是连续数字";
        }
        
        // 长度检查：国际手机号通常在7-15位之间
        if (number.length() < 7) {
            return "国际手机号长度不能少于7位";
        }
        
        if (number.length() > 15) {
            return "国际手机号长度不能超过15位";
        }
        
        return null; // 有效
    }
    
    /**
     * 验证国际手机号的合理性
     * 
     * @param phone 已清理的手机号
     * @return 是否有效
     */
    private boolean isValidInternationalPhone(String phone) {
        return getInternationalPhoneValidationError(phone) == null;
    }
    
    /**
     * 检查是否为重复数字（如：111111, 888888）
     * 
     * @param number 数字字符串
     * @return 是否为重复数字
     */
    private boolean isRepeatingDigits(String number) {
        if (number == null || number.length() < 6) {
            return false;
        }
        
        // 检查是否所有数字都相同
        char firstDigit = number.charAt(0);
        for (int i = 1; i < number.length(); i++) {
            if (number.charAt(i) != firstDigit) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查是否为连续数字（如：123456789, 987654321）
     * 
     * @param number 数字字符串
     * @return 是否为连续数字
     */
    private boolean isConsecutiveDigits(String number) {
        if (number == null || number.length() < 6) {
            return false;
        }
        
        // 检查是否为连续递增数字
        boolean isIncreasing = true;
        boolean isDecreasing = true;
        
        for (int i = 1; i < number.length(); i++) {
            int current = Character.getNumericValue(number.charAt(i));
            int previous = Character.getNumericValue(number.charAt(i - 1));
            
            if (current != previous + 1) {
                isIncreasing = false;
            }
            if (current != previous - 1) {
                isDecreasing = false;
            }
            
            if (!isIncreasing && !isDecreasing) {
                break;
            }
        }
        
        return isIncreasing || isDecreasing;
    }
    
    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱地址
     * @return 是否有效
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
}
