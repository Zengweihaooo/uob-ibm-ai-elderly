package com.example.demo.pojo;

import java.time.LocalDateTime;

public class HealthRecord {
    private Long id;
    private Long userId;
    private String type;
    private String value;
    private LocalDateTime recordTime;
    private String notes;
    
    // ========== 新增共享功能相关字段 ==========
    // 是否已共享
    private Boolean shared;
    // 共享目标用户ID
    private Long sharedWithUserId;
    // 共享对象角色（family 或 doctor）
    private String sharedWithRole;
    // 共享时间
    private LocalDateTime sharedAt;
    // ========== 新增字段结束 ==========

    // Constructors
    public HealthRecord() {}

    public HealthRecord(Long userId, String type, String value) {
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.recordTime = LocalDateTime.now();
        // ========== 初始化共享相关字段 ==========
        this.shared = false;
        this.sharedWithUserId = null;
        this.sharedWithRole = null;
        this.sharedAt = null;
        // ========== 初始化结束 ==========
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

    public LocalDateTime getRecordTime() {
        return recordTime;
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

    // ========== 共享功能相关getter和setter ==========
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

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
    // ========== 共享功能相关getter和setter结束 ==========

    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", recordTime=" + recordTime +
                ", notes='" + notes + '\'' +
                ", shared=" + shared +
                ", sharedWithUserId=" + sharedWithUserId +
                ", sharedWithRole='" + sharedWithRole + '\'' +
                ", sharedAt=" + sharedAt +
                '}';
    }
} 