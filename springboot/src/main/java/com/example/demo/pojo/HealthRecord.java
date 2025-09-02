package com.example.demo.pojo;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

public class HealthRecord {
    private Long id;
    private Long userId;
    private String type;
    private String value;
    private String unit;
    private LocalDateTime recordTime;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // ========== Added fields for sharing feature ==========
    // Whether shared
    private Boolean shared;
    // Shared-to user ID
    private Long sharedWithUserId;
    // Shared target role (family or doctor)
    private String sharedWithRole;
    // Shared timestamp
    private LocalDateTime sharedAt;
    // ========== End of added fields ==========

    // Constructors
    public HealthRecord() {}

    public HealthRecord(Long userId, String type, String value) {
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.recordTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // ========== Initialize sharing-related fields ==========
        this.shared = false;
        this.sharedWithUserId = null;
        this.sharedWithRole = null;
        this.sharedAt = null;
        // ========== Initialization end ==========
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getRecordTime() {
        return recordTime;
    }
    
    // Additional string-format getter for JSON serialization
    public String getRecordTimeStr() {
        return recordTime != null ? recordTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Additional string-format getter for JSON serialization
    public String getCreatedAtStr() {
        return createdAt != null ? createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Additional string-format getter for JSON serialization
    public String getUpdatedAtStr() {
        return updatedAt != null ? updatedAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========== Getters and setters for sharing feature ==========
    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public Long getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(Long sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getSharedWithRole() {
        return sharedWithRole;
    }

    public void setSharedWithRole(String sharedWithRole) {
        this.sharedWithRole = sharedWithRole;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
    // ========== End of getters and setters for sharing feature ==========

    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", unit='" + unit + '\'' +
                ", recordTime=" + recordTime +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", shared=" + shared +
                ", sharedWithUserId=" + sharedWithUserId +
                ", sharedWithRole='" + sharedWithRole + '\'' +
                ", sharedAt=" + sharedAt +
                '}';
    }
} 