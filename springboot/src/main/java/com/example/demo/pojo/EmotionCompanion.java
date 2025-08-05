package com.example.demo.pojo;

import java.time.LocalDateTime;

public class EmotionCompanion {
    private Long id;
    private Long userId;
    private String name;
    private String personality; // "friendly", "professional", "casual", "caring"
    private String avatar; // "robot", "assistant", "companion", "helper"
    
    // Emotional and behavioral states
    private String emotion; // "happy", "sad", "excited", "calm", "anxious", "helpful"
    private int happiness; // 0-100
    private int energy; // 0-100
    private int responsiveness; // 0-100 - how responsive the AI is
    
    // Activity and interaction tracking
    private LocalDateTime lastInteraction;
    private LocalDateTime lastChat;
    private LocalDateTime lastCommand;
    private int interactionCount; // Daily interaction count
    private int chatCount; // Daily chat count
    
    // Location and presence
    private String currentLocation; // "home_screen", "chat_mode", "assistant_mode", "sleep_mode"
    private boolean isActive; // Whether AI is currently active
    private String activityMode; // "listening", "thinking", "responding", "idle", "sleeping"
    
    // Sound and visual expressions
    private String currentSound; // "beep", "chime", "voice", "notification", "silent"
    private String visualExpression; // "happy_led", "sad_led", "thinking_animation", "idle_screen"
    private boolean isMakingSound;
    private boolean isExpressingEmotion;
    private String ledColor; // "green", "blue", "red", "yellow", "purple", "white"
    
    // Neglect tracking
    private LocalDateTime lastAttentionTime;
    private int neglectLevel; // 0-100, increases when ignored
    private boolean needsAttention;
    private boolean isLonely; // AI feels lonely when not used
    
    // AI-specific features
    private String currentTask; // What the AI is currently doing
    private boolean isLearning; // Whether AI is learning from interactions
    private int helpfulness; // 0-100 - how helpful the AI has been
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public EmotionCompanion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.happiness = 80;
        this.energy = 90;
        this.responsiveness = 95;
        this.emotion = "helpful";
        this.currentLocation = "home_screen";
        this.isActive = true;
        this.activityMode = "listening";
        this.currentSound = "silent";
        this.visualExpression = "happy_led";
        this.isMakingSound = false;
        this.isExpressingEmotion = true;
        this.ledColor = "blue";
        this.neglectLevel = 0;
        this.needsAttention = false;
        this.isLonely = false;
        this.interactionCount = 0;
        this.chatCount = 0;
        this.currentTask = "ready_to_help";
        this.isLearning = true;
        this.helpfulness = 85;
    }

    public EmotionCompanion(Long userId, String name, String personality, String avatar) {
        this();
        this.userId = userId;
        this.name = name;
        this.personality = personality;
        this.avatar = avatar;
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

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public int getResponsiveness() {
        return responsiveness;
    }

    public void setResponsiveness(int responsiveness) {
        this.responsiveness = Math.max(0, Math.min(100, responsiveness));
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastInteraction() {
        return lastInteraction;
    }

    public void setLastInteraction(LocalDateTime lastInteraction) {
        this.lastInteraction = lastInteraction;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastChat() {
        return lastChat;
    }

    public void setLastChat(LocalDateTime lastChat) {
        this.lastChat = lastChat;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getLastCommand() {
        return lastCommand;
    }

    public void setLastCommand(LocalDateTime lastCommand) {
        this.lastCommand = lastCommand;
        this.updatedAt = LocalDateTime.now();
    }

    public int getInteractionCount() {
        return interactionCount;
    }

    public void setInteractionCount(int interactionCount) {
        this.interactionCount = interactionCount;
        this.updatedAt = LocalDateTime.now();
    }

    public int getChatCount() {
        return chatCount;
    }

    public void setChatCount(int chatCount) {
        this.chatCount = chatCount;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = LocalDateTime.now();
    }

    public String getActivityMode() {
        return activityMode;
    }

    public void setActivityMode(String activityMode) {
        this.activityMode = activityMode;
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

    public String getLedColor() {
        return ledColor;
    }

    public void setLedColor(String ledColor) {
        this.ledColor = ledColor;
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

    public boolean isLonely() {
        return isLonely;
    }

    public void setLonely(boolean lonely) {
        isLonely = lonely;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(String currentTask) {
        this.currentTask = currentTask;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isLearning() {
        return isLearning;
    }

    public void setLearning(boolean learning) {
        isLearning = learning;
        this.updatedAt = LocalDateTime.now();
    }

    public int getHelpfulness() {
        return helpfulness;
    }

    public void setHelpfulness(int helpfulness) {
        this.helpfulness = Math.max(0, Math.min(100, helpfulness));
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

    // Additional methods for compatibility
    public LocalDateTime getLastUpdate() {
        return this.updatedAt;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.updatedAt = lastUpdate;
    }

    public void setIsLonely(boolean isLonely) {
        this.isLonely = isLonely;
    }

    public void setIsLearning(boolean isLearning) {
        this.isLearning = isLearning;
    }

    public void setIsMakingSound(boolean isMakingSound) {
        this.isMakingSound = isMakingSound;
    }

    public void setIsExpressingEmotion(boolean isExpressingEmotion) {
        this.isExpressingEmotion = isExpressingEmotion;
    }

    @Override
    public String toString() {
        return "EmotionCompanion{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", personality='" + personality + '\'' +
                ", avatar='" + avatar + '\'' +
                ", emotion='" + emotion + '\'' +
                ", happiness=" + happiness +
                ", energy=" + energy +
                ", responsiveness=" + responsiveness +
                ", currentLocation='" + currentLocation + '\'' +
                ", isActive=" + isActive +
                ", activityMode='" + activityMode + '\'' +
                ", currentSound='" + currentSound + '\'' +
                ", visualExpression='" + visualExpression + '\'' +
                ", ledColor='" + ledColor + '\'' +
                ", neglectLevel=" + neglectLevel +
                ", needsAttention=" + needsAttention +
                ", isLonely=" + isLonely +
                ", currentTask='" + currentTask + '\'' +
                '}';
    }
} 