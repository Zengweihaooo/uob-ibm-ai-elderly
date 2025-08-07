package com.example.demo.pojo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 备忘录实体类
 * 用于存储老年用户的日常备忘录信息
 */
public class Memo {
    
    private Long id;                    // 备忘录ID
    private Long userId;                // 用户ID
    private String title;               // 备忘录标题
    private String content;             // 备忘录内容
    private String type;                // 备忘录类型：general(一般), important(重要), todo(待办)
    private boolean isImportant;        // 是否重要（重要备忘录）
    private String pinCode;             // PIN码（用于重要备忘录）
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
    private boolean isDeleted;          // 是否已删除（软删除）
    
    // 默认构造函数
    public Memo() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.isDeleted = false;
        this.isImportant = false;
        this.type = "general";
    }
    
    // 带参数的构造函数
    public Memo(Long userId, String title, String content, String type) {
        this();
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.type = type;
        
        // 如果是重要类型，自动设置为重要
        if ("important".equals(type)) {
            this.isImportant = true;
        }
    }
    
    // Getter和Setter方法
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
     * 获取格式化的创建时间字符串
     * @return 格式化的时间字符串
     */
    public String getFormattedCreateTime() {
        if (createTime != null) {
            return createTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "";
    }
    
    /**
     * 获取格式化的更新时间字符串
     * @return 格式化的时间字符串
     */
    public String getFormattedUpdateTime() {
        if (updateTime != null) {
            return updateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        return "";
    }
    
    /**
     * 获取备忘录类型的英文描述
     * @return 类型描述
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
     * 获取备忘录的图标
     * @return 图标字符串
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