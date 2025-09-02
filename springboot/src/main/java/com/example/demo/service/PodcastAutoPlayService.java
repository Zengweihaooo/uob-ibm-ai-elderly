package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Podcast auto-play service
 * Responsible for managing scheduled podcast playback and playback status tracking
 */
@Service
public class PodcastAutoPlayService {

    // Inject podcast service for getting podcast information
    @Autowired
    private PodcastService podcastService;

    // Store user's podcast playback schedules (user ID -> schedule list)
    private final Map<Long, List<Map<String, Object>>> userSchedules = new ConcurrentHashMap<>();
    
    // Store current playback status (user ID -> playback status)
    private final Map<Long, Map<String, Object>> playbackStatuses = new ConcurrentHashMap<>();

    /**
     * Add podcast playback schedule for user
     * @param userId user ID
     * @param scheduleData playback schedule data
     * @return schedule ID
     */
    public String addSchedule(Long userId, Map<String, Object> scheduleData) {
        // Generate unique schedule ID
        String scheduleId = "schedule_" + System.currentTimeMillis();
        
        // Set basic information for the schedule
        scheduleData.put("id", scheduleId);
        scheduleData.put("userId", userId);
        scheduleData.put("isActive", true);  // Default active status
        scheduleData.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Add the schedule to user's schedule list
        userSchedules.computeIfAbsent(userId, k -> new ArrayList<>()).add(scheduleData);
        
        return scheduleId;
    }

    /**
     * Get all podcast playback schedules for a user
     * @param userId user ID
     * @return schedule list
     */
    public List<Map<String, Object>> getUserSchedules(Long userId) {
        return userSchedules.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Get all podcast playback schedules for a user (return Map format)
     * @param userId user ID
     * @return schedule Map
     */
    public Map<String, Object> getUserSchedulesMap(Long userId) {
        List<Map<String, Object>> schedules = userSchedules.getOrDefault(userId, new ArrayList<>());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("schedules", schedules);
        result.put("totalSchedules", schedules.size());
        return result;
    }

    /**
     * Schedule podcast playback for user
     * @param userId user ID
     * @param podcastTitle podcast title
     * @param playTime play time
     * @param playDate play date
     * @return schedule result
     */
    public Map<String, Object> schedulePodcast(Long userId, String podcastTitle, String playTime, String playDate) {
        try {
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("podcastTitle", podcastTitle);
            scheduleData.put("playTime", playTime);
            scheduleData.put("playDate", playDate);
            
            String scheduleId = addSchedule(userId, scheduleData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("schedule", scheduleData);
            result.put("scheduleId", scheduleId);
            result.put("message", "Podcast scheduled successfully");
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Error scheduling podcast: " + e.getMessage());
            return result;
        }
    }

    /**
     * Remove a specific playback schedule
     * @param userId user ID
     * @param scheduleId playback schedule ID
     * @return whether deletion succeeded
     */
    public boolean removeSchedule(Long userId, String scheduleId) {
        List<Map<String, Object>> schedules = userSchedules.get(userId);
        if (schedules != null) {
            // Remove the plan by schedule ID
            return schedules.removeIf(schedule -> scheduleId.equals(schedule.get("id")));
        }
        return false;
    }

    /**
     * Get the user's current playback status
     * @param userId user ID
     * @return playback status info
     */
    public Map<String, Object> getPlaybackStatus(Long userId) {
        return playbackStatuses.get(userId);
    }

    /**
     * Start playing the specified podcast
     * @param userId user ID
     * @param scheduleId schedule ID
     * @return whether playback started successfully
     */
    public boolean startPlayback(Long userId, String scheduleId) {
        // Get all playback schedules for the user
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        
        // Find the corresponding schedule
        for (Map<String, Object> schedule : schedules) {
            if (scheduleId.equals(schedule.get("id"))) {
                // Create playback status object
                Map<String, Object> playbackStatus = new HashMap<>();
                playbackStatus.put("userId", userId);
                playbackStatus.put("scheduleId", scheduleId);
                playbackStatus.put("podcastId", schedule.get("podcastId"));
                playbackStatus.put("podcastTitle", schedule.get("podcastTitle"));
                playbackStatus.put("isPlaying", true);  // Set to playing status
                playbackStatus.put("startedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                playbackStatus.put("currentPosition", 0);  // Playback position starts from 0
                
                // Save playback status
                playbackStatuses.put(userId, playbackStatus);
                return true;
            }
        }
        
        return false;  // Schedule not found
    }

    /**
     * Stop user's podcast playback
     * @param userId user ID
     * @return whether playback stopped successfully
     */
    public boolean stopPlayback(Long userId) {
        // Remove user's playback status
        Map<String, Object> status = playbackStatuses.remove(userId);
        return status != null;  // Return whether there was previous playback status
    }

    /**
     * Update playback position
     * @param userId user ID
     * @param position playback position (seconds)
     * @return whether updated successfully
     */
    public boolean updatePlaybackPosition(Long userId, int position) {
        Map<String, Object> status = playbackStatuses.get(userId);
        if (status != null) {
            // Update playback position and last update time
            status.put("currentPosition", position);
            status.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return true;
        }
        return false;
    }

    /**
     * Scheduled task: check and execute scheduled podcast playback
     * Executes every minute to check if there are podcasts that need to be played
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndExecuteScheduledPodcasts() {
        // Get current time
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // Iterate through all users' schedules
        for (Map.Entry<Long, List<Map<String, Object>>> entry : userSchedules.entrySet()) {
            Long userId = entry.getKey();
            List<Map<String, Object>> schedules = entry.getValue();

            // Check each schedule for the user
            for (Map<String, Object> schedule : schedules) {
                // Only process active schedules
                if (Boolean.TRUE.equals(schedule.get("isActive"))) {
                    String playDateStr = (String) schedule.get("playDate");
                    String playTimeStr = (String) schedule.get("playTime");

                    if (playDateStr != null && playTimeStr != null) {
                        try {
                            // Parse play date and time
                            LocalDate playDate = LocalDate.parse(playDateStr);
                            LocalTime playTime = LocalTime.parse(playTimeStr);

                            // Check if it's time to play
                            if (playDate.equals(today) && playTime.equals(currentTime)) {
                                // Start playing podcast
                                String scheduleId = (String) schedule.get("id");
                                startPlayback(userId, scheduleId);
                                
                                // Output playback log
                                System.out.println("🎵 Auto playback started: " + schedule.get("podcastTitle") + 
                                                 " (user: " + userId + ") time: " + currentTime);
                            }
                        } catch (Exception e) {
                            // Handle date time parsing error
                            System.err.println("Failed to parse schedule time: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Get user's upcoming podcast list
     * @param userId user ID
     * @return upcoming podcast list
     */
    public List<Map<String, Object>> getUpcomingPodcasts(Long userId) {
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        List<Map<String, Object>> upcoming = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Iterate through all schedules
        for (Map<String, Object> schedule : schedules) {
            // Only process active schedules
            if (Boolean.TRUE.equals(schedule.get("isActive"))) {
                String playDateStr = (String) schedule.get("playDate");
                String playTimeStr = (String) schedule.get("playTime");

                if (playDateStr != null && playTimeStr != null) {
                    try {
                        // Parse playback date and time
                        LocalDate playDate = LocalDate.parse(playDateStr);
                        LocalTime playTime = LocalTime.parse(playTimeStr);
                        LocalDateTime scheduledTime = LocalDateTime.of(playDate, playTime);

                        // Only add future schedules
                        if (scheduledTime.isAfter(now)) {
                            Map<String, Object> upcomingPodcast = new HashMap<>(schedule);
                            upcomingPodcast.put("scheduledTime", scheduledTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                            upcoming.add(upcomingPodcast);
                        }
                    } catch (Exception e) {
                        // Handle schedule time parse error
                        System.err.println("Failed to parse upcoming schedule time: " + e.getMessage());
                    }
                }
            }
        }

        // Sort by scheduled time
        upcoming.sort((a, b) -> {
            String timeA = (String) a.get("scheduledTime");
            String timeB = (String) b.get("scheduledTime");
            return timeA.compareTo(timeB);
        });

        return upcoming;
    }

    /**
     * Get user's recently played podcast list
     * @param userId user ID
     * @return recently played podcast list
     */
    public List<Map<String, Object>> getRecentlyPlayedPodcasts(Long userId) {
        List<Map<String, Object>> schedules = getUserSchedules(userId);
        List<Map<String, Object>> recent = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);  // Time one week ago

        // Iterate through all schedules
        for (Map<String, Object> schedule : schedules) {
            String createdAtStr = (String) schedule.get("createdAt");
            if (createdAtStr != null) {
                try {
                    // Parse creation time
                    LocalDateTime createdAt = LocalDateTime.parse(createdAtStr);
                    // Only add schedules created within one week
                    if (createdAt.isAfter(oneWeekAgo)) {
                        recent.add(schedule);
                    }
                } catch (Exception e) {
                    // Handle time parse error
                    System.err.println("Failed to parse creation time: " + e.getMessage());
                }
            }
        }

        return recent;
    }
} 