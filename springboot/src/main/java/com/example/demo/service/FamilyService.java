package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @version 1.0
 */
@Service
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

        try {
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

            // 保存到数据库
            int result = familyContactMapper.insert(contact);
            if (result > 0) {
                System.out.println("Family contact added to database: " + contact.getName() + " for user " + userId);
                return contact;
            } else {
                throw new RuntimeException("Failed to insert family contact into database");
            }
        } catch (Exception e) {
            System.err.println("Error adding family contact: " + e.getMessage());
            throw new RuntimeException("Failed to add family contact: " + e.getMessage());
        }
    }

    /**
     * Get all family contacts for a user
     * 
     * @param userId User ID
     * @return List of family contacts
     */
    public List<FamilyContact> getFamilyContacts(Long userId) {
        try {
            return familyContactMapper.findActiveByUserId(userId);
        } catch (Exception e) {
            System.err.println("Error getting family contacts for user " + userId + ": " + e.getMessage());
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
            // 验证联系人属于指定用户且处于活跃状态
            if (contact != null && contact.getUserId().equals(userId) && contact.getIsActive()) {
                return contact;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error getting family contact " + contactId + " for user " + userId + ": " + e.getMessage());
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
        try {
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
            
            // 更新到数据库
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                System.out.println("Family contact updated in database: " + contact.getName() + " for user " + userId);
                return contact;
            } else {
                throw new RuntimeException("Failed to update family contact in database");
            }
        } catch (Exception e) {
            System.err.println("Error updating family contact: " + e.getMessage());
            throw new RuntimeException("Failed to update family contact: " + e.getMessage());
        }
    }

    /**
     * Delete a family contact (soft delete)
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return true if deleted, false if not found
     */
    public boolean deleteFamilyContact(Long userId, Long contactId) {
        try {
            FamilyContact contact = getFamilyContact(userId, contactId);
            if (contact == null) {
                return false;
            }

            contact.setIsActive(false);
            contact.setUpdatedAt(LocalDateTime.now());
            
            // 更新到数据库（软删除）
            int result = familyContactMapper.update(contact);
            if (result > 0) {
                System.out.println("Family contact soft deleted in database: " + contact.getName() + " for user " + userId);
                return true;
            } else {
                System.err.println("Failed to soft delete family contact in database");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error deleting family contact: " + e.getMessage());
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
            return familyContactMapper.findEmergencyContactsByUserId(userId);
        } catch (Exception e) {
            System.err.println("Error getting emergency contacts for user " + userId + ": " + e.getMessage());
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
     * Get all contacts (for testing/debugging)
     */
    public List<FamilyContact> getAllContacts() {
        try {
            // 返回所有联系人（包括非活跃的）用于调试
            return familyContactMapper.findByUserId(1L); // 暂时使用用户ID=1进行测试
        } catch (Exception e) {
            System.err.println("Error getting all contacts: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
