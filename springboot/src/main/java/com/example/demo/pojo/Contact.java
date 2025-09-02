package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * Contact entity for storing user contacts
 * 
 * @author AI Assistant
 * @version 1.0
 */
public class Contact {
    
    private Long id;
    private Long userId; // Owning user ID
    private String name;
    private String email;
    private String phone;
    private String relationship; // Relationship: family, friend, doctor, etc.
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Contact() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Contact(Long userId, String name, String email) {
        this();
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
    
    public Contact(Long userId, String name, String email, String relationship) {
        this(userId, name, email);
        this.relationship = relationship;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
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
        return "Contact{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
