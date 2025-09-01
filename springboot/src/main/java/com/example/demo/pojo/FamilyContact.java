package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * Family Contact entity for the IBM AI Elderly Project
 * 
 * This class represents a family contact that can be notified
 * when the elderly user needs assistance or has health issues.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
public class FamilyContact {
    
    private Long id;
    private Long userId;  // 关联的用户ID
    private String name;  // 联系人姓名
    private String relationship;  // 关系（儿子、女儿、配偶等）
    private String phone;  // 电话号码
    private String email;  // 邮箱地址
    private String address;  // 地址
    private Boolean isEmergencyContact;  // 是否为紧急联系人
    private Boolean isActive;  // 是否激活
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
    
    // 默认构造函数
    public FamilyContact() {
        this.isActive = true;
        this.isEmergencyContact = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 带参数的构造函数
    public FamilyContact(Long userId, String name, String relationship, String phone, String email) {
        this();
        this.userId = userId;
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
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
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Boolean getIsEmergencyContact() {
        return isEmergencyContact;
    }
    
    public void setIsEmergencyContact(Boolean isEmergencyContact) {
        this.isEmergencyContact = isEmergencyContact;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    @Override
    public String toString() {
        return "FamilyContact{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", relationship='" + relationship + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", isEmergencyContact=" + isEmergencyContact +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}