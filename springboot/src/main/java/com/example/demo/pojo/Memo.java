package com.example.demo.pojo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Memo entity class
 * Used to store daily memo information for elderly users
 */
public class Memo {
    
    private Long id;                    // Memo ID
    private Long userId;                // User ID
    private String title;               // Memo title
    private String content;             // Memo content
    private String type;                // Memo type: general, important, todo
    private boolean isImportant;        // Whether it's important (important memo)
    private String pinCode;             // PIN code (for important memos)
    private LocalDateTime createTime;   // Creation time
    private LocalDateTime updateTime;   // Update time
    private boolean isDeleted;          // Whether deleted (soft delete)
    
    // Default constructor
    public Memo() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.isDeleted = false;
        this.isImportant = false;
        this.type = "general";
    }
    
    // Constructor with parameters
    public Memo(Long userId, String title, String content, String type) {
        this();
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        
        // If it's important type, automatically set as important
        if ("important".equals(type)) {
            this.isImportant = true;
        }
    }
    
    // Getter and Setter methods
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public boolean isImportant() {
        return isImportant;
    }
    
    public void setImportant(boolean important) {
        isImportant = important;
    }
    
    public String getPinCode() {
        return pinCode;
    }
    
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
    
    /**
     * Get formatted creation time string
     * @return formatted time string
     */
    public String getFormattedCreateTime() {
        if (createTime != null) {
            return createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "";
    }
    
    /**
     * Get formatted update time string
     * @return formatted time string
     */
    public String getFormattedUpdateTime() {
        if (updateTime != null) {
            return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "";
    }
    
    /**
     * Get memo type description in English
     * @return type description
     */
    public String getTypeDescription() {
        switch (type) {
            case "important":
                return "Important";
            case "todo":
                return "Todo";
            case "general":
            default:
                return "General";
        }
    }
    
    /**
     * Get memo icon
     * @return icon string
     */
    public String getTypeIcon() {
        switch (type) {
            case "important":
                return "❗";
            case "todo":
                return "📝";
            case "general":
            default:
                return "📄";
        }
    }
    
    @Override
    public String toString() {
        return "Memo{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", isImportant=" + isImportant +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isDeleted=" + isDeleted +
                '}';
    }
} 