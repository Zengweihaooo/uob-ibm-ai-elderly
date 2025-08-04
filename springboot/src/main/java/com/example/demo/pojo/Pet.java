package com.example.demo.pojo;

import java.time.LocalDateTime;

public class Pet {
    private Long id;
    private Long userId;
    private String name;
    private String type; // "dog", "cat", "bird", etc.
    private String breed;
    private int age;
    
    // Emotional and behavioral states
    private String emotion; // "happy", "sad", "excited", "calm", "anxious"
    private int happiness; // 0-100
    private int energy; // 0-100
    private int health; // 0-100
    
    // Activity and interaction tracking
    private LocalDateTime lastInteraction;
    private LocalDateTime lastFed;
    private LocalDateTime lastPlayed;
    private int interactionCount; // Daily interaction count
    
    // Location and movement
    private String currentLocation; // "living_room", "bedroom", "kitchen", "garden"
    private boolean isMoving; // Whether pet is currently moving
    private String movementType; // "walking", "running", "sitting", "sleeping"
    
    // Sound and visual expressions
    private String currentSound; // "barking", "meowing", "purring", "whining", "silent"
    private String visualExpression; // "tail_wagging", "ears_back", "bright_eyes", "droopy_ears"
    private boolean isMakingSound;
    private boolean isExpressingEmotion;
    
    // Neglect tracking
    private LocalDateTime lastAttentionTime;
    private int neglectLevel; // 0-100, increases when ignored
    private boolean needsAttention;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Pet() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.happiness = 80;
        this.energy = 70;
        this.health = 90;
        this.emotion = "happy";
        this.currentLocation = "living_room";
        this.isMoving = false;
        this.movementType = "sitting";
        this.currentSound = "silent";
        this.visualExpression = "bright_eyes";
        this.isMakingSound = false;
        this.isExpressingEmotion = false;
        this.neglectLevel = 0;
        this.needsAttention = false;
        this.interactionCount = 0;
    }

    public Pet(Long userId, String name, String type, String breed, int age) {
        this();
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
        this.updatedAt = LocalDateTime.now();
    }

    public int getHappiness() {
        return happiness;
    }

    public void setHappiness(int happiness) {
        this.happiness = Math.max(0, Math.min(100, happiness));
        this.updatedAt = LocalDateTime.now();
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = Math.max(0, Math.min(100, energy));
        this.updatedAt = LocalDateTime.now();
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(100, health));
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(LocalDateTime lastInteraction) {
        this.lastInteraction = lastInteraction;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastFed() {
        return lastFed;
    }

    public void setLastFed(LocalDateTime lastFed) {
        this.lastFed = lastFed;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(LocalDateTime lastPlayed) {
        this.lastPlayed = lastPlayed;
        this.updatedAt = LocalDateTime.now();
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrentSound() {
        return currentSound;
    }

    public void setCurrentSound(String currentSound) {
        this.currentSound = currentSound;
        this.isMakingSound = !"silent".equals(currentSound);
        this.updatedAt = LocalDateTime.now();
    }

    public String getVisualExpression() {
        return visualExpression;
    }

    public void setVisualExpression(String visualExpression) {
        this.visualExpression = visualExpression;
        this.isExpressingEmotion = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isMakingSound() {
        return isMakingSound;
    }

    public void setMakingSound(boolean makingSound) {
        isMakingSound = makingSound;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpressingEmotion() {
        return isExpressingEmotion;
    }

    public void setExpressingEmotion(boolean expressingEmotion) {
        isExpressingEmotion = expressingEmotion;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastAttentionTime() {
        return lastAttentionTime;
    }

    public void setLastAttentionTime(LocalDateTime lastAttentionTime) {
        this.lastAttentionTime = lastAttentionTime;
        this.updatedAt = LocalDateTime.now();
    }

    public int getNeglectLevel() {
        return neglectLevel;
    }

    public void setNeglectLevel(int neglectLevel) {
        this.neglectLevel = Math.max(0, Math.min(100, neglectLevel));
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isNeedsAttention() {
        return needsAttention;
    }

    public void setNeedsAttention(boolean needsAttention) {
        this.needsAttention = needsAttention;
        this.updatedAt = LocalDateTime.now();
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
        return "Pet{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", breed='" + breed + '\'' +
                ", age=" + age +
                ", emotion='" + emotion + '\'' +
                ", happiness=" + happiness +
                ", energy=" + energy +
                ", health=" + health +
                ", currentLocation='" + currentLocation + '\'' +
                ", isMoving=" + isMoving +
                ", currentSound='" + currentSound + '\'' +
                ", visualExpression='" + visualExpression + '\'' +
                ", neglectLevel=" + neglectLevel +
                ", needsAttention=" + needsAttention +
                '}';
    }
} 