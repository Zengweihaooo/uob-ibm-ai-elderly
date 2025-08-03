package com.example.demo.pojo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 播客数据模型类
 * 用于存储从Listen Notes API获取的播客信息
 */
public class Podcast {
    // 播客唯一标识符
    private String id;
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
    // Listen Notes网站链接
    private String listennotesUrl;
    // RSS订阅链接
    private String rss;
    // 播客语言
    private String language;
    // 播客国家/地区
    private String country;
    // 播客官方网站
    private String website;
    // 是否已被认领（播客所有者是否验证）
    private boolean isClaimed;
    // 播客类型
    private String type;
    // 总集数
    private int totalEpisodes;
    // 播客剧集列表
    private List<Episode> episodes;
    // 播客分类标签
    private List<String> genres;
    // 额外信息（API返回的其他字段）
    private Map<String, Object> extra;
    // 创建时间
    private LocalDateTime createdAt;
    // 更新时间
    private LocalDateTime updatedAt;

    // 默认构造函数
    public Podcast() {}

    /**
     * 带参数的构造函数
     * @param id 播客ID
     * @param title 播客标题
     * @param description 播客描述
     * @param publisher 播客发布者
     */
    public Podcast(String id, String title, String description, String publisher) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
    }

    // ==================== Getter和Setter方法 ====================
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getListennotesUrl() {
        return listennotesUrl;
    }

    public void setListennotesUrl(String listennotesUrl) {
        this.listennotesUrl = listennotesUrl;
    }

    public String getRss() {
        return rss;
    }

    public void setRss(String rss) {
        this.rss = rss;
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isClaimed() {
        return isClaimed;
    }

    public void setClaimed(boolean claimed) {
        isClaimed = claimed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTotalEpisodes() {
        return totalEpisodes;
    }

    public void setTotalEpisodes(int totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
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

    /**
     * 播客剧集内部类
     * 用于存储单个播客剧集的信息
     */
    public static class Episode {
        // 剧集唯一标识符
        private String id;
        // 剧集标题
        private String title;
        // 剧集描述
        private String description;
        // 音频文件URL
        private String audio;
        // 剧集封面图片URL
        private String image;
        // 剧集缩略图URL
        private String thumbnail;
        // Listen Notes网站链接
        private String listennotesUrl;
        // 音频时长（秒）
        private String audioLength;
        // 发布时间
        private LocalDateTime publishedDate;
        // 剧集语言
        private String language;
        // 剧集国家/地区
        private String country;
        // 剧集网站
        private String website;
        // 是否已被认领
        private boolean isClaimed;
        // 剧集类型
        private String type;
        // 额外信息
        private Map<String, Object> extra;

        // 默认构造函数
        public Episode() {}

        /**
         * 带参数的构造函数
         * @param id 剧集ID
         * @param title 剧集标题
         * @param description 剧集描述
         * @param audio 音频URL
         */
        public Episode(String id, String title, String description, String audio) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.audio = audio;
        }

        // ==================== Episode的Getter和Setter方法 ====================
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getAudio() {
            return audio;
        }

        public void setAudio(String audio) {
            this.audio = audio;
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

        public String getListennotesUrl() {
            return listennotesUrl;
        }

        public void setListennotesUrl(String listennotesUrl) {
            this.listennotesUrl = listennotesUrl;
        }

        public String getAudioLength() {
            return audioLength;
        }

        public void setAudioLength(String audioLength) {
            this.audioLength = audioLength;
        }

        public LocalDateTime getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(LocalDateTime publishedDate) {
            this.publishedDate = publishedDate;
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

        public String getWebsite() {
            return website;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        public boolean isClaimed() {
            return isClaimed;
        }

        public void setClaimed(boolean claimed) {
            isClaimed = claimed;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(Map<String, Object> extra) {
            this.extra = extra;
        }
    }
} 