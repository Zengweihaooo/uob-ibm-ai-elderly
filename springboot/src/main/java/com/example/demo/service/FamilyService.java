package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.FamilyContact;
import com.example.demo.pojo.HealthRecord;

/**
 * Service class for family contact management
 * 
 * This service handles all business logic related to family contacts,
 * including CRUD operations, message sending, and contact management.
 * 
 * @author Your Name
 * @version 1.0
 */
@Service
public class FamilyService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;

    // In-memory storage for family contacts (consider using database in production)
    private List<FamilyContact> familyContacts = new ArrayList<>();
    private Long contactIdCounter = 1L;

    /**
     * Add a new family contact
     * 
     * @param userId The elderly user's ID
     * @param name Contact name
     * @param phoneNumber Phone number
     * @param email Email address
     * @param relationship Relationship type
     * @param notificationPreference Notification preference
     * @param isEmergencyContact Whether this is an emergency contact
     * @param notes Additional notes
     * @return Created family contact
     */
    public FamilyContact addFamilyContact(Long userId, String name, String phoneNumber, String email,
                                        String relationship, String notificationPreference,
                                        Boolean isEmergencyContact, String notes) {
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name is required");
        }

        if ((phoneNumber == null || phoneNumber.trim().isEmpty()) && 
            (email == null || email.trim().isEmpty())) {
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        // Create new contact
        FamilyContact contact = new FamilyContact();
        contact.setId(contactIdCounter++);
        contact.setUserId(userId);
        contact.setName(name.trim());
        contact.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : null);
        contact.setEmail(email != null ? email.trim() : null);
        contact.setRelationship(parseRelationship(relationship));
        contact.setNotificationPreference(parseNotificationPreference(notificationPreference));
        contact.setEmergencyContact(isEmergencyContact != null ? isEmergencyContact : false);
        contact.setNotes(notes != null ? notes.trim() : null);
        contact.setActive(true);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        // Add to storage
        familyContacts.add(contact);

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
        return familyContacts.stream()
                .filter(contact -> contact.getUserId().equals(userId) && contact.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Get a specific family contact by ID
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @return Family contact or null if not found
     */
    public FamilyContact getFamilyContact(Long userId, Long contactId) {
        return familyContacts.stream()
                .filter(contact -> contact.getUserId().equals(userId) && 
                                 contact.getId().equals(contactId) && 
                                 contact.isActive())
                .findFirst()
                .orElse(null);
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

        if (contactData.containsKey("phoneNumber")) {
            contact.setPhoneNumber((String) contactData.get("phoneNumber"));
        }

        if (contactData.containsKey("email")) {
            contact.setEmail((String) contactData.get("email"));
        }

        if (contactData.containsKey("relationship")) {
            contact.setRelationship(parseRelationship((String) contactData.get("relationship")));
        }

        if (contactData.containsKey("notificationPreference")) {
            contact.setNotificationPreference(parseNotificationPreference((String) contactData.get("notificationPreference")));
        }

        if (contactData.containsKey("isEmergencyContact")) {
            contact.setEmergencyContact((Boolean) contactData.get("isEmergencyContact"));
        }

        if (contactData.containsKey("notes")) {
            contact.setNotes((String) contactData.get("notes"));
        }

        if (contactData.containsKey("isActive")) {
            contact.setActive((Boolean) contactData.get("isActive"));
        }

        // Validate that at least one contact method exists
        if (!contact.hasValidContactInfo()) {
            throw new IllegalArgumentException("Either phone number or email is required");
        }

        contact.setUpdatedAt(LocalDateTime.now());

        System.out.println("Family contact updated: " + contact.getName() + " for user " + userId);
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

        contact.setActive(false);
        contact.setUpdatedAt(LocalDateTime.now());

        System.out.println("Family contact deleted: " + contact.getName() + " for user " + userId);
        return true;
    }

    /**
     * Send message to family contact
     * 
     * @param userId User ID
     * @param contactId Contact ID
     * @param message Message content
     * @param messageType Message type
     * @return true if sent successfully, false otherwise
     */
    public boolean sendMessageToFamily(Long userId, Long contactId, String message, String messageType) {
        FamilyContact contact = getFamilyContact(userId, contactId);
        if (contact == null) {
            return false;
        }

        // Check if contact should receive this type of message
        if (!contact.shouldReceiveNotification(messageType)) {
            System.out.println("Contact " + contact.getName() + " has notification preference that excludes " + messageType);
            return false;
        }

        try {
            // Send email if available
            if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                String subject = buildMessageSubject(messageType, contact.getName());
                String emailContent = buildMessageContent(message, messageType, contact.getName());
                
                emailService.sendHealthAlertEmail(contact.getEmail(), subject, emailContent);
                System.out.println("Message sent via email to " + contact.getEmail());
            }

            // Send SMS if phone number is available
            if (contact.getPhoneNumber() != null && !contact.getPhoneNumber().trim().isEmpty()) {
                try {
                    // 发送SMS，使用传入的消息内容
                    Map<String, Object> smsResult = smsService.sendSMS(contact.getPhoneNumber(), message, messageType);
                    
                    if ((Boolean) smsResult.get("success")) {
                        System.out.println("SMS发送成功到 " + contact.getPhoneNumber() + 
                                         ", MessageID: " + smsResult.get("messageId"));
                    } else {
                        System.err.println("SMS发送失败到 " + contact.getPhoneNumber() + 
                                         ": " + smsResult.get("message"));
                    }
                } catch (Exception e) {
                    System.err.println("SMS发送异常: " + e.getMessage());
                }
            }

            // Update last contacted time
            contact.updateLastContacted();

            return true;

        } catch (Exception e) {
            System.err.println("Failed to send message to family contact: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get emergency contacts for a user
     * 
     * @param userId User ID
     * @return List of emergency contacts
     */
    public List<FamilyContact> getEmergencyContacts(Long userId) {
        return familyContacts.stream()
                .filter(contact -> contact.getUserId().equals(userId) && 
                                 contact.isEmergencyContact() && 
                                 contact.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Get family contact statistics
     * 
     * @param userId User ID
     * @return Statistics map
     */
    public Map<String, Object> getFamilyStats(Long userId) {
        List<FamilyContact> userContacts = getFamilyContacts(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalContacts", userContacts.size());
        stats.put("emergencyContacts", getEmergencyContacts(userId).size());
        stats.put("activeContacts", userContacts.stream().filter(FamilyContact::isActive).count());
        
        // Count by relationship type
        Map<String, Long> relationshipStats = userContacts.stream()
                .collect(Collectors.groupingBy(
                    contact -> contact.getRelationship().name(),
                    Collectors.counting()
                ));
        stats.put("byRelationship", relationshipStats);
        
        // Count by notification preference
        Map<String, Long> notificationStats = userContacts.stream()
                .collect(Collectors.groupingBy(
                    contact -> contact.getNotificationPreference().name(),
                    Collectors.counting()
                ));
        stats.put("byNotificationPreference", notificationStats);
        
        return stats;
    }

    /**
     * Send emergency notification to all emergency contacts
     * 
     * @param userId User ID
     * @param emergencyType Emergency type
     * @param description Emergency description
     * @return Number of contacts notified
     */
    public int sendEmergencyNotification(Long userId, String emergencyType, String description) {
        List<FamilyContact> emergencyContacts = getEmergencyContacts(userId);
        int notifiedCount = 0;

        for (FamilyContact contact : emergencyContacts) {
            String message = buildEmergencyMessage(emergencyType, description, contact.getName());
            if (sendMessageToFamily(userId, contact.getId(), message, "emergency")) {
                notifiedCount++;
            }
        }

        System.out.println("Emergency notification sent to " + notifiedCount + " contacts for user " + userId);
        return notifiedCount;
    }

    // Helper methods

    private FamilyContact.RelationshipType parseRelationship(String relationship) {
        if (relationship == null) {
            return FamilyContact.RelationshipType.OTHER;
        }
        
        try {
            return FamilyContact.RelationshipType.valueOf(relationship.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FamilyContact.RelationshipType.OTHER;
        }
    }

    private FamilyContact.NotificationPreference parseNotificationPreference(String preference) {
        if (preference == null) {
            return FamilyContact.NotificationPreference.ALL;
        }
        
        try {
            return FamilyContact.NotificationPreference.valueOf(preference.toUpperCase());
        } catch (IllegalArgumentException e) {
            return FamilyContact.NotificationPreference.ALL;
        }
    }

    private String buildMessageSubject(String messageType, String contactName) {
        switch (messageType.toLowerCase()) {
            case "emergency":
                return "🚨 Emergency Alert - " + contactName;
            case "health":
                return "🏥 Health Update - " + contactName;
            case "daily_summary":
                return "📋 Daily Summary - " + contactName;
            default:
                return "�� Message from Elderly Care App - " + contactName;
        }
    }

    private String buildMessageContent(String message, String messageType, String contactName) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(contactName).append(",\n\n");
        
        switch (messageType.toLowerCase()) {
            case "emergency":
                content.append("�� EMERGENCY ALERT 🚨\n\n");
                break;
            case "health":
                content.append("🏥 Health Update\n\n");
                break;
            case "daily_summary":
                content.append("📋 Daily Summary\n\n");
                break;
        }
        
        content.append(message).append("\n\n");
        content.append("Best regards,\nElderly Care App Team");
        
        return content.toString();
    }


    private String buildEmergencyMessage(String emergencyType, String description, String contactName) {
        return "Emergency situation detected: " + emergencyType + "\n\n" +
               "Description: " + description + "\n\n" +
               "Please check on your loved one immediately and contact emergency services if necessary.";
    }

    // Utility methods for testing and debugging

    public List<FamilyContact> getAllContacts() {
        return new ArrayList<>(familyContacts);
    }

    public void clearAllContacts() {
        familyContacts.clear();
        contactIdCounter = 1L;
    }
    
    // ========== 新增家庭成员验证和共享相关功能 ==========
    
    /**
     * 验证目标用户是否为家庭成员
     * @param userId 老年人用户ID
     * @param targetUserId 目标用户ID
     * @return 是否为家庭成员
     */
    public boolean isFamilyMember(Long userId, Long targetUserId) {
        List<FamilyContact> familyContacts = getFamilyContacts(userId);
        // 检查目标用户是否在家庭成员列表中
        return familyContacts.stream()
                .anyMatch(contact -> contact.getUserId().equals(targetUserId) || 
                                   (contact.getEmail() != null && 
                                    getUserEmailById(targetUserId) != null &&
                                    contact.getEmail().equals(getUserEmailById(targetUserId))));
    }
    
    /**
     * 获取家庭成员列表（用于前端选择共享对象）
     * @param userId 老年人用户ID
     * @return 家庭成员列表
     */
    public List<Map<String, Object>> getFamilyMembersForSharing(Long userId) {
        List<FamilyContact> contacts = getFamilyContacts(userId);
        List<Map<String, Object>> familyMembers = new ArrayList<>();
        
        for (FamilyContact contact : contacts) {
            Map<String, Object> member = new HashMap<>();
            member.put("id", contact.getId());
            member.put("name", contact.getName());
            member.put("email", contact.getEmail());
            member.put("phoneNumber", contact.getPhoneNumber());
            member.put("relationship", contact.getRelationship());
            member.put("isEmergencyContact", contact.isEmergencyContact());
            familyMembers.add(member);
        }
        
        return familyMembers;
    }
    
    /**
     * 根据用户ID获取用户邮箱（辅助方法）
     * @param userId 用户ID
     * @return 用户邮箱
     */
    private String getUserEmailById(Long userId) {
        // 这里需要注入UserService来获取用户信息
        // 暂时返回null，实际使用时需要注入UserService
        return null;
    }
    
    /**
     * 发送健康数据共享通知给家庭成员
     * @param userId 老年人用户ID
     * @param healthRecord 健康记录
     * @param sharedWithUserId 共享目标用户ID
     */
    public void notifyFamilyMemberOfHealthShare(Long userId, HealthRecord healthRecord, Long sharedWithUserId) {
        List<FamilyContact> contacts = getFamilyContacts(userId);
        
        for (FamilyContact contact : contacts) {
            if (contact.getEmail() != null) {
                String subject = "健康数据共享通知";
                String message = buildHealthShareNotificationMessage(healthRecord, contact.getName());
                
                try {
                    emailService.sendHealthAlertEmail(contact.getEmail(), subject, message);
                    System.out.println("健康数据共享通知已发送给: " + contact.getName());
                } catch (Exception e) {
                    System.err.println("发送健康数据共享通知失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 构建健康数据共享通知消息
     * @param healthRecord 健康记录
     * @param contactName 联系人姓名
     * @return 通知消息
     */
    private String buildHealthShareNotificationMessage(HealthRecord healthRecord, String contactName) {
        StringBuilder message = new StringBuilder();
        message.append("亲爱的 ").append(contactName).append("：\n\n");
        message.append("您的家人刚刚共享了一条健康数据记录：\n\n");
        message.append("数据类型：").append(getTypeDisplayName(healthRecord.getType())).append("\n");
        message.append("数据值：").append(healthRecord.getValue()).append("\n");
        message.append("记录时间：").append(healthRecord.getRecordTime()).append("\n");
        message.append("共享时间：").append(healthRecord.getSharedAt()).append("\n\n");
        message.append("请及时查看并关注家人的健康状况。\n\n");
        message.append("此邮件由IBM AI Elderly系统自动发送。");
        
        return message.toString();
    }
    
    /**
     * 获取健康数据类型的显示名称
     * @param type 数据类型
     * @return 显示名称
     */
    private String getTypeDisplayName(String type) {
        switch (type.toLowerCase()) {
            case "bloodpressure":
                return "血压";
            case "bloodsugar":
                return "血糖";
            case "steps":
                return "步数";
            default:
                return type;
        }
    }
    
    
    /**
     * If the user does not submit health/schedule data, notify their family contacts
     */
    public int notifyFamilyIfNoSubmission(Long userId, boolean isHealthCheck) {
        String type = isHealthCheck ? "health" : "schedule";
        String message = isHealthCheck
            ? "Your family member has not submitted their health data yet today. Please pay attention to their status."
            : "Your family member has not submitted their schedule yet today. Please pay attention to their status.";

        int notified = 0;
        for (FamilyContact contact : getFamilyContacts(userId)) {
            if (contact.getEmail() != null) {
                emailService.sendHealthAlertEmail(contact.getEmail(), "Notification - " + type, message);
                notified++;
            }
        }
        return notified;
    }
}
