package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * 播客收藏实体类
 * 用于存储用户收藏的播客信息
 */
public class PodcastFavorite {
    
    // 主键ID
    private Long id;
    
    // 用户ID
    private Long userId;
    
    // 播客ID（来自外部API）
    private String podcastId;
    
    // 播客标题
    private String title;
    
    // 播客描述
    private String description;
    
    // 播客发布者
    private String publisher;
    
    // 播客封面图片URL
    private String image;
    
    // 播客缩略图URL
    private String thumbnail;
    
    // 播客语言
    private String language;
    
    // 播客国家/地区
    private String country;
    
    // 播客类型
    private String type;
    
    // 总集数
    private Integer totalEpisodes;
    
    // 播客分类标签（JSON格式存储）
    private String genres;
    
    // 播客RSS链接
    private String rss;
    
    // 播客官网
    private String website;
    
    // 收藏时间
    private LocalDateTime createdAt;
    
    // 更新时间
    private LocalDateTime updatedAt;
    
    // 是否激活（软删除标记）
    private Boolean isActive;
    
    // 默认构造函数
    public PodcastFavorite() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 带参数的构造函数
     * @param userId 用户ID
     * @param podcastId 播客ID
     * @param title 播客标题
     * @param description 播客描述
     * @param publisher 播客发布者
     */
    public PodcastFavorite(Long userId, String podcastId, String title, String description, String publisher) {
        this();
        this.userId = userId;
        this.podcastId = podcastId;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
    }
    
    // ==================== Getter和Setter方法 ====================
    
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
    
    public String getPodcastId() {
        return podcastId;
    }
    
    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
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
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getThumbnail() {
        return thumbnail;
    }
    
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getTotalEpisodes() {
        return totalEpisodes;
    }
    
    public void setTotalEpisodes(Integer totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }
    
    public String getGenres() {
        return genres;
    }
    
    public void setGenres(String genres) {
        this.genres = genres;
    }
    
    public String getRss() {
        return rss;
    }
    
    public void setRss(String rss) {
        this.rss = rss;
    }
    
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "PodcastFavorite{" +
                "id=" + id +
                ", userId=" + userId +
                ", podcastId='" + podcastId + '\'' +
                ", title='" + title + '\'' +
                ", publisher='" + publisher + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
} 