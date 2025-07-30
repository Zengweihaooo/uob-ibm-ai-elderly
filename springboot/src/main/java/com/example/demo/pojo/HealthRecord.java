package com.example.demo.pojo;

import java.time.LocalDateTime;

public class HealthRecord {
    private Long id;
    private Long userId;
    private String type;
    private String value;
    private LocalDateTime recordTime;
    private String notes;

    // Constructors
    public HealthRecord() {}

    public HealthRecord(Long userId, String type, String value) {
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.recordTime = LocalDateTime.now();
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

    @Override
    public String toString() {
        return "HealthRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", recordTime=" + recordTime +
                ", notes='" + notes + '\'' +
                '}';
    }
} 