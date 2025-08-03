package com.example.demo.pojo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Important Date entity for managing birthdays, anniversaries, and holidays
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
public class ImportantDate {
    
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDate date;
    private String type; // birthday, anniversary, holiday, custom
    private String repeatCycle; // none, yearly
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public ImportantDate() {
        this.enabled = true;
        this.repeatCycle = "yearly";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public ImportantDate(Long userId, String title, LocalDate date, String type) {
        this();
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.type = type;
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getRepeatCycle() {
        return repeatCycle;
    }
    
    public void setRepeatCycle(String repeatCycle) {
        this.repeatCycle = repeatCycle;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        return "ImportantDate{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", repeatCycle='" + repeatCycle + '\'' +
                ", enabled=" + enabled +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 