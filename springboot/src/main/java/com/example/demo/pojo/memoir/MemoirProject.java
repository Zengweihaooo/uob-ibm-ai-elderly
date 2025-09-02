package com.example.demo.pojo.memoir;

/**
 * Memoir project entity
 * Used to store basic information of memoir projects
 */
public class MemoirProject {
    private Integer id;
    private String title;
    private String owner;      // Owner (optional: username/userId)
    private String locale;     // Language, e.g. en-US
    private String pinHash;    // PIN hash (optional)
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
