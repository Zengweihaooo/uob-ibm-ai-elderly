package com.example.demo.pojo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Schedule entity representing a user's daily activity
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
public class Schedule {
    private Long id;
    private Long userId;
    private LocalDate scheduleDate;
    private LocalTime activityTime;
    private String title;
    private String description;
    private String category; // morning, afternoon, evening, medication
    private boolean completed;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // Constructors
    public Schedule() {}

    public Schedule(Long userId, LocalDate scheduleDate, LocalTime activityTime, 
                   String title, String description, String category) {
        this.userId = userId;
        this.scheduleDate = scheduleDate;
        this.activityTime = activityTime;
        this.title = title;
        this.description = description;
        this.category = category;
        this.completed = false;
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
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

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalTime activityTime) {
        this.activityTime = activityTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", userId=" + userId +
                ", scheduleDate=" + scheduleDate +
                ", activityTime=" + activityTime +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 