package com.example.demo.pojo.memoir;

/**
 * 回忆录项目实体
 * 中文注释：用于存储回忆录项目的基础信息
 */
public class MemoirProject {
    private Integer id;
    private String title;
    private String owner;      // 拥有者（可选：用户名/用户ID）
    private String locale;     // 语言，如 en-US
    private String pinHash;    // PIN 哈希（可选）
    private String createdAt;
    private String updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
