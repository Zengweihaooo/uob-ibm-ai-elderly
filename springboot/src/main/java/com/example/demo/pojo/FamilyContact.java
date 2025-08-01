package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * FamilyContact entity representing a family member or emergency contact
 * for the elderly user in the IBM AI Elderly Project
 * 
 * This class manages family contact information, relationship types,
 * and communication preferences for the elderly care application.
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
public class FamilyContact {
    
    /**
     * Relationship types enumeration
     */
    public enum RelationshipType {
        SPOUSE,         // 配偶
        CHILD,          // 子女
        GRANDCHILD,     // 孙子女
        SIBLING,        // 兄弟姐妹
        PARENT,         // 父母
        FRIEND,         // 朋友
        NEIGHBOR,       // 邻居
        CAREGIVER,      // 护理人员
        DOCTOR,         // 医生
        OTHER           // 其他
    }
    
    /**
     * Notification preference enumeration
     */
    public enum NotificationPreference {
        ALL,            // 接收所有通知
        EMERGENCY_ONLY, // 仅接收紧急通知
        HEALTH_ONLY,    // 仅接收健康相关通知
        DAILY_SUMMARY,  // 仅接收每日摘要
        NONE            // 不接收通知
    }
    
    private Long id;
    private Long userId;                    // 关联的用户ID（老年人）
    private String name;                    // 联系人姓名
    private String phoneNumber;             // 电话号码
    private String email;                   // 邮箱地址
    private RelationshipType relationship;  // 关系类型
    private NotificationPreference notificationPreference; // 通知偏好
    private boolean isEmergencyContact;     // 是否为紧急联系人
    private boolean isActive;               // 是否激活
    private String notes;                   // 备注信息
    private LocalDateTime createdAt;        // 创建时间
    private LocalDateTime updatedAt;        // 更新时间
    private LocalDateTime lastContactedAt;  // 最后联系时间
    
    /**
     * Default constructor
     */
    public FamilyContact() {
        this.isEmergencyContact = false;
        this.isActive = true;
        this.notificationPreference = NotificationPreference.ALL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic information
     * 
     * @param userId The elderly user's ID
     * @param name Contact name
     * @param phoneNumber Phone number
     * @param relationship Relationship type
     */
    public FamilyContact(Long userId, String name, String phoneNumber, RelationshipType relationship) {
        this();
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.relationship = relationship;
    }
    
    /**
     * Constructor with all information
     * 
     * @param userId The elderly user's ID
     * @param name Contact name
     * @param phoneNumber Phone number
     * @param email Email address
     * @param relationship Relationship type
     * @param isEmergencyContact Whether this is an emergency contact
     */
    public FamilyContact(Long userId, String name, String phoneNumber, String email, 
                        RelationshipType relationship, boolean isEmergencyContact) {
        this(userId, name, phoneNumber, relationship);
        this.email = email;
        this.isEmergencyContact = isEmergencyContact;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public RelationshipType getRelationship() {
        return relationship;
    }
    
    public void setRelationship(RelationshipType relationship) {
        this.relationship = relationship;
    }
    
    public NotificationPreference getNotificationPreference() {
        return notificationPreference;
    }
    
    public void setNotificationPreference(NotificationPreference notificationPreference) {
        this.notificationPreference = notificationPreference;
    }
    
    public boolean isEmergencyContact() {
        return isEmergencyContact;
    }
    
    public void setEmergencyContact(boolean emergencyContact) {
        isEmergencyContact = emergencyContact;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastContactedAt() {
        return lastContactedAt;
    }
    
    public void setLastContactedAt(LocalDateTime lastContactedAt) {
        this.lastContactedAt = lastContactedAt;
    }
    
    /**
     * Update the last contacted time
     */
    public void updateLastContacted() {
        this.lastContactedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this contact should receive notifications
     * 
     * @param notificationType The type of notification
     * @return true if should receive, false otherwise
     */
    public boolean shouldReceiveNotification(String notificationType) {
        if (!isActive) {
            return false;
        }
        
        switch (notificationPreference) {
            case ALL:
                return true;
            case EMERGENCY_ONLY:
                return "emergency".equalsIgnoreCase(notificationType);
            case HEALTH_ONLY:
                return "health".equalsIgnoreCase(notificationType);
            case DAILY_SUMMARY:
                return "daily_summary".equalsIgnoreCase(notificationType);
            case NONE:
                return false;
            default:
                return false;
        }
    }
    
    /**
     * Get display name with relationship
     * 
     * @return Formatted display name
     */
    public String getDisplayName() {
        return name + " (" + relationship.name() + ")";
    }
    
    /**
     * Check if contact has valid contact information
     * 
     * @return true if has phone or email, false otherwise
     */
    public boolean hasValidContactInfo() {
        return (phoneNumber != null && !phoneNumber.trim().isEmpty()) ||
               (email != null && !email.trim().isEmpty());
    }
    
    @Override
    public String toString() {
        return "FamilyContact{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", relationship=" + relationship +
                ", notificationPreference=" + notificationPreference +
                ", isEmergencyContact=" + isEmergencyContact +
                ", isActive=" + isActive +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastContactedAt=" + lastContactedAt +
                '}';
    }
}