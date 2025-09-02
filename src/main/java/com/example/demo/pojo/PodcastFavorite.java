package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Podcast Favorite Entity
 * Represents a user's favorite podcast
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodcastFavorite {
    
    /**
     * Primary key
     */
    private Long id;
    
    /**
     * User ID who favorited this podcast
     */
    private Long userId;
    
    /**
     * Podcast ID
     */
    private String podcastId;
    
    /**
     * Podcast title
     */
    private String title;
    
    /**
     * Podcast description
     */
    private String description;
    
    /**
     * Podcast publisher
     */
    private String publisher;
    
    /**
     * Podcast image URL
     */
    private String image;
    
    /**
     * Podcast thumbnail URL
     */
    private String thumbnail;
    
    /**
     * Podcast language
     */
    private String language;
    
    /**
     * Podcast country
     */
    private String country;
    
    /**
     * Podcast type
     */
    private String type;
    
    /**
     * Total number of episodes
     */
    private Integer totalEpisodes;
    
    /**
     * Podcast genres
     */
    private String genres;
    
    /**
     * RSS feed URL
     */
    private String rss;
    
    /**
     * Website URL
     */
    private String website;
    
    /**
     * Whether the favorite is active
     */
    private Boolean isActive;
    
    /**
     * Creation timestamp
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;
}

