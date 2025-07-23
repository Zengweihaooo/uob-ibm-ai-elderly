package com.example.demo.pojo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Enhanced Schedule entity representing a user's daily activity with advanced features
 * 
 * @author Weihao Zeng
 * @version 2.0
 */
public class Schedule {
    private Long id;
    private Long userId;
    private LocalDate scheduleDate;
    private LocalTime activityTime;
    private String title;
    private String description;
    private String category; // morning, afternoon, evening, medication
    private boolean completed;
    
    // Enhanced features
    private String priority; // high, medium, low
    private String emergencyContact; // Phone number or email
    private String emergencyContactName; // Name of emergency contact
    private String repeatCycle; // none, daily, weekly, monthly, yearly
    private String notificationTime; // 5min, 15min, 30min, 1hour, 1day
    private boolean locationReminder; // Enable/disable location-based reminder
    private String locationName; // Name of the location
    private Double latitude; // Latitude for geofencing
    private Double longitude; // Longitude for geofencing
    private Integer locationRadius; // Radius in meters for geofencing
    private String notes; // Additional notes
    private boolean isAllDay; // All day event flag
    private LocalDateTime reminderSent; // Track when reminder was sent
    private LocalDateTime emergencyNotificationSent; // Track emergency notifications
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Schedule() {}

    public Schedule(Long userId, LocalDate scheduleDate, LocalTime activityTime, 
                   String title, String description, String category) {
        this.userId = userId;
        this.scheduleDate = scheduleDate;
        this.activityTime = activityTime;
        this.title = title;
        this.description = description;
        this.category = category;
        this.completed = false;
        this.priority = "medium"; // Default priority
        this.repeatCycle = "none"; // Default no repeat
        this.notificationTime = "15min"; // Default 15 minutes before
        this.locationReminder = false; // Default no location reminder
        this.isAllDay = false; // Default not all day
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

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(LocalDate scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public LocalTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalTime activityTime) {
        this.activityTime = activityTime;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getRepeatCycle() {
        return repeatCycle;
    }

    public void setRepeatCycle(String repeatCycle) {
        this.repeatCycle = repeatCycle;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }

    public boolean isLocationReminder() {
        return locationReminder;
    }

    public void setLocationReminder(boolean locationReminder) {
        this.locationReminder = locationReminder;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getLocationRadius() {
        return locationRadius;
    }

    public void setLocationRadius(Integer locationRadius) {
        this.locationRadius = locationRadius;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public void setAllDay(boolean allDay) {
        isAllDay = allDay;
    }

    public LocalDateTime getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(LocalDateTime reminderSent) {
        this.reminderSent = reminderSent;
    }

    public LocalDateTime getEmergencyNotificationSent() {
        return emergencyNotificationSent;
    }

    public void setEmergencyNotificationSent(LocalDateTime emergencyNotificationSent) {
        this.emergencyNotificationSent = emergencyNotificationSent;
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
     * Get priority display color
     */
    public String getPriorityColor() {
        switch (priority) {
            case "high": return "#dc3545";
            case "medium": return "#ffc107";
            case "low": return "#28a745";
            default: return "#6c757d";
        }
    }

    /**
     * Check if this is an overdue incomplete task
     */
    public boolean isOverdue() {
        if (completed) return false;
        LocalDateTime taskDateTime = LocalDateTime.of(scheduleDate, activityTime != null ? activityTime : LocalTime.of(23, 59));
        return taskDateTime.isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", userId=" + userId +
                ", scheduleDate=" + scheduleDate +
                ", activityTime=" + activityTime +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", completed=" + completed +
                ", repeatCycle='" + repeatCycle + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                ", locationReminder=" + locationReminder +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 