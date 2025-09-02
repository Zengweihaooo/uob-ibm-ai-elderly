package com.example.demo.pojo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Podcast data model class
 * Used to store podcast information retrieved from Listen Notes API
 */
public class Podcast {
    // Podcast unique identifier
    private String id;
    // Podcast title
    private String title;
    // Podcast description
    private String description;
    // Podcast publisher
    private String publisher;
    // Podcast cover image URL
    private String image;
    // Podcast thumbnail URL
    private String thumbnail;
    // Listen Notes website link
    private String listennotesUrl;
    // RSS subscription link
    private String rss;
    // Podcast language
    private String language;
    // Podcast country/region
    private String country;
    // Podcast official website
    private String website;
    // Whether claimed (podcast owner verification)
    private boolean isClaimed;
    // Podcast type
    private String type;
    // Total episodes
    private int totalEpisodes;
    // Podcast episode list
    private List<Episode> episodes;
    // Podcast category tags
    private List<String> genres;
    // Additional information (other fields returned by API)
    private Map<String, Object> extra;
    // Creation time
    private LocalDateTime createdAt;
    // Update time
    private LocalDateTime updatedAt;

    // Default constructor
    public Podcast() {}

    /**
     * Constructor with parameters
     * @param id podcast ID
     * @param title podcast title
     * @param description podcast description
     * @param publisher podcast publisher
     */
    public Podcast(String id, String title, String description, String publisher) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
    }

    // ==================== Getter and Setter methods ====================
    
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
     * Podcast episode inner class
     * Used to store information about a single podcast episode
     */
    public static class Episode {
        // Episode unique identifier
        private String id;
        // Episode title
        private String title;
        // Episode description
        private String description;
        // Audio file URL
        private String audio;
        // Episode cover image URL
        private String image;
        // Episode thumbnail URL
        private String thumbnail;
        // Listen Notes website link
        private String listennotesUrl;
        // Audio duration (seconds)
        private String audioLength;
        // Publication date
        private LocalDateTime publishedDate;
        // Episode language
        private String language;
        // Episode country/region
        private String country;
        // Episode website
        private String website;
        // Whether claimed
        private boolean isClaimed;
        // Episode type
        private String type;
        // Additional information
        private Map<String, Object> extra;

        // Default constructor
        public Episode() {}

        /**
         * Constructor with parameters
         * @param id episode ID
         * @param title episode title
         * @param description episode description
         * @param audio audio URL
         */
        public Episode(String id, String title, String description, String audio) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.audio = audio;
        }

        // ==================== Episode Getter and Setter methods ====================
        
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