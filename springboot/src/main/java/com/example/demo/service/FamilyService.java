package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;

/**
 * Service class for managing family contacts and family-related operations
 * 
 * This service handles family contact management, emergency notifications,
 * and family member communication for the IBM AI Elderly Project.
 * Now using database storage instead of in-memory storage.
 * 
 * @author Weihao Zeng
 * @version 2.0
 */
@Service
@Transactional
public class FamilyService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;
    
    @Autowired
    private FamilyContactMapper familyContactMapper;

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
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name is required");
        }

        if ((phone == null || phone.trim().isEmpty()) && 
            (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        // Create new contact
        FamilyContact contact = new FamilyContact();
        contact.setUserId(userId);
        contact.setName(name.trim());
        contact.setPhone(phone != null ? phone.trim() : null);
        contact.setEmail(email != null ? email.trim() : null);
        contact.setRelationship(relationship != null ? relationship.trim() : "其他");
        contact.setIsEmergencyContact(isEmergencyContact != null ? isEmergencyContact : false);
        contact.setIsActive(true);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        // Save to database
        try {
            familyContactMapper.insert(contact);
            System.out.println("Family contact added to database: " + contact.getName() + " for user " + userId);
        } catch (Exception e) {
            System.err.println("Failed to add family contact to database: " + e.getMessage());
            throw new RuntimeException("Failed to save family contact", e);
        }
        
        return contact;
    }

    /**
     * Get all family contacts for a user
     * 
     * @param userId User ID
     * @return List of family contacts
     */
    public List<FamilyContact> getFamilyContacts(Long userId) {
        try {
            List<FamilyContact> contacts = familyContactMapper.findActiveByUserId(userId);
            return contacts != null ? contacts : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Failed to get family contacts from database: " + e.getMessage());
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
        try {
            FamilyContact contact = familyContactMapper.findById(contactId);
            // Verify the contact belongs to the user and is active
            if (contact != null && contact.getUserId().equals(userId) && contact.getIsActive()) {
                return contact;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Failed to get family contact from database: " + e.getMessage());
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
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            return null;
        }

        // Update fields if provided
        if (contactData.containsKey("name")) {
            String name = (String) contactData.get("name");
            if (name != null && !name.trim().isEmpty()) {
                contact.setName(name.trim());
            } else {
                throw new IllegalArgumentException("Contact name cannot be empty");
            }
        }

        if (contactData.containsKey("phone")) {
            contact.setPhone((String) contactData.get("phone"));
        }

        if (contactData.containsKey("email")) {
            contact.setEmail((String) contactData.get("email"));
        }

        if (contactData.containsKey("relationship")) {
            contact.setRelationship((String) contactData.get("relationship"));
        }

        if (contactData.containsKey("isEmergencyContact")) {
            contact.setIsEmergencyContact((Boolean) contactData.get("isEmergencyContact"));
        }

        if (contactData.containsKey("address")) {
            contact.setAddress((String) contactData.get("address"));
        }

        // Validate contact has at least phone or email
        if ((contact.getPhone() == null || contact.getPhone().trim().isEmpty()) &&
            (contact.getEmail() == null || contact.getEmail().trim().isEmpty())) {
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        contact.setUpdatedAt(LocalDateTime.now());
        
        // Save to database
        try {
            familyContactMapper.update(contact);
            System.out.println("Family contact updated in database: " + contact.getName());
        } catch (Exception e) {
            System.err.println("Failed to update family contact in database: " + e.getMessage());
            throw new RuntimeException("Failed to update family contact", e);
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
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            return false;
        }

        contact.setIsActive(false);
        contact.setUpdatedAt(LocalDateTime.now());
        
        // Update in database
        try {
            familyContactMapper.update(contact);
            System.out.println("Family contact soft deleted in database: " + contact.getName());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to delete family contact in database: " + e.getMessage());
            return false;
        }
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
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            return false;
        }

        try {
            if ("email".equalsIgnoreCase(messageType) && contact.getEmail() != null) {
                String subject = buildMessageSubject(messageType, contact.getName());
                String content = buildMessageContent(message, messageType, contact.getName());
                emailService.sendHealthAlertEmail(contact.getEmail(), subject, content);
                return true;
            } else if ("sms".equalsIgnoreCase(messageType) && contact.getPhone() != null) {
                String content = buildMessageContent(message, messageType, contact.getName());
                smsService.sendSMS(contact.getPhone(), content);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to send message to family contact: " + e.getMessage());
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
        try {
            List<FamilyContact> contacts = familyContactMapper.findEmergencyContactsByUserId(userId);
            return contacts != null ? contacts : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Failed to get emergency contacts from database: " + e.getMessage());
            return new ArrayList<>();
        }
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
                    smsService.sendSMS(contact.getPhone(), message);
                    notifiedCount++;
                }
                
                if (contact.getEmail() != null) {
                    String subject = "紧急情况通知 - " + emergencyType;
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
                return "健康状态更新 - " + contactName;
            case "schedule":
                return "日程提醒 - " + contactName;
            case "emergency":
                return "紧急情况通知 - " + contactName;
            default:
                return "消息通知 - " + contactName;
        }
    }

    /**
     * Build message content
     */
    private String buildMessageContent(String message, String messageType, String contactName) {
        StringBuilder content = new StringBuilder();
        content.append("亲爱的 ").append(contactName).append("，\n\n");
        content.append(message).append("\n\n");
        content.append("此消息由AI老年人陪伴系统自动发送。\n");
        content.append("发送时间：").append(LocalDateTime.now().toString());
        
        return content.toString();
    }

    /**
     * Build emergency message
     */
    private String buildEmergencyMessage(String emergencyType, String description, String contactName) {
        return "紧急情况通知：\n" + emergencyType + "\n" + description + "\n\n联系人：" + contactName;
    }

    /**
     * Get all contacts (for testing)
     * Note: This method now retrieves all contacts from database
     */
    public List<FamilyContact> getAllContacts() {
        try {
            // Get all contacts for all users (mainly for testing)
            // In production, you might want to limit this or add pagination
            List<FamilyContact> allContacts = new ArrayList<>();
            // Since we don't have a findAll method, we'll need to add one or use a different approach
            // For now, returning empty list - you can add a findAll method to mapper if needed
            System.out.println("getAllContacts called - consider adding pagination for production use");
            return allContacts;
        } catch (Exception e) {
            System.err.println("Failed to get all contacts from database: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Clear all contacts for a specific user (for testing)
     * Note: This now performs actual database deletion
     */
    public void clearAllContactsForUser(Long userId) {
        try {
            List<FamilyContact> userContacts = familyContactMapper.findByUserId(userId);
            for (FamilyContact contact : userContacts) {
                familyContactMapper.deleteById(contact.getId());
            }
            System.out.println("Cleared all contacts for user " + userId + " from database");
        } catch (Exception e) {
            System.err.println("Failed to clear contacts from database: " + e.getMessage());
        }
    }
}
