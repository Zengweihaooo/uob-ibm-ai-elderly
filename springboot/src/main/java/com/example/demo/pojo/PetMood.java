package com.example.demo.pojo;

import java.time.LocalDateTime;

/**
 * Pet mood entity
 * Used to store and manage pet mood state and related attributes
 */
public class PetMood {
    private Long id;
    private Long userId;
    private Integer moodScore;      // Mood score (-100 to 100)
    private Integer happiness;      // Happiness (0-100)
    private Integer health;         // Health (0-100)
    private Integer energy;         // Energy (0-100)
    private String moodEmoji;       // Mood emoji
    private String status;          // Status description
    private Integer level;          // Pet level
    private Integer experience;     // Experience points
    private LocalDateTime lastInteraction; // Last interaction time
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PetMood() {}

    public PetMood(Long userId) {
        this.userId = userId;
        this.moodScore = 0;
        this.happiness = 85;
        this.health = 92;
        this.energy = 78;
        this.moodEmoji = "😊";
        this.status = "Happy & Healthy";
        this.level = 1;
        this.experience = 0;
        this.lastInteraction = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }

    public Integer getHappiness() {
        return happiness;
    }

    public void setHappiness(Integer happiness) {
        this.happiness = happiness;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getEnergy() {
        return energy;
    }

    public void setEnergy(Integer energy) {
        this.energy = energy;
    }

    public String getMoodEmoji() {
        return moodEmoji;
    }

    public void setMoodEmoji(String moodEmoji) {
        this.moodEmoji = moodEmoji;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public LocalDateTime getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(LocalDateTime lastInteraction) {
        this.lastInteraction = lastInteraction;
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
        return "PetMood{" +
                "id=" + id +
                ", userId=" + userId +
                ", moodScore=" + moodScore +
                ", happiness=" + happiness +
                ", health=" + health +
                ", energy=" + energy +
                ", moodEmoji='" + moodEmoji + '\'' +
                ", status='" + status + '\'' +
                ", level=" + level +
                ", experience=" + experience +
                ", lastInteraction=" + lastInteraction +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
