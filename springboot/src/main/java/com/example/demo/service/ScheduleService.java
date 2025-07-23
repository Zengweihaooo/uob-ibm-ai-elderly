package com.example.demo.service;

import com.example.demo.pojo.Schedule;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service class for managing user schedules
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class ScheduleService {
    
    // In-memory storage for schedules (replace with database in production)
    private final Map<Long, Schedule> schedules = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Get all schedules for a specific user and date
     * 
     * @param userId User ID
     * @param date Target date
     * @return Map of categorized schedules
     */
    public Map<String, List<Schedule>> getSchedulesByUserAndDate(Long userId, LocalDate date) {
        List<Schedule> userSchedules = schedules.values().stream()
                .filter(schedule -> schedule.getUserId().equals(userId))
                .filter(schedule -> schedule.getScheduleDate().equals(date))
                .sorted(Comparator.comparing(Schedule::getActivityTime))
                .collect(Collectors.toList());

        // Group by category
        Map<String, List<Schedule>> categorizedSchedules = new HashMap<>();
        categorizedSchedules.put("morning", new ArrayList<>());
        categorizedSchedules.put("afternoon", new ArrayList<>());
        categorizedSchedules.put("evening", new ArrayList<>());
        categorizedSchedules.put("medication", new ArrayList<>());

        for (Schedule schedule : userSchedules) {
            String category = schedule.getCategory();
            if (categorizedSchedules.containsKey(category)) {
                categorizedSchedules.get(category).add(schedule);
            }
        }

        return categorizedSchedules;
    }

    /**
     * Add a new schedule item
     * 
     * @param schedule Schedule to add
     * @return Created schedule with ID
     */
    public Schedule addSchedule(Schedule schedule) {
        Long id = idGenerator.getAndIncrement();
        schedule.setId(id);
        schedule.setCreatedAt(java.time.LocalDateTime.now());
        schedule.setUpdatedAt(java.time.LocalDateTime.now());
        schedules.put(id, schedule);
        return schedule;
    }

    /**
     * Update an existing schedule
     * 
     * @param id Schedule ID
     * @param updatedSchedule Updated schedule data
     * @return Updated schedule or null if not found
     */
    public Schedule updateSchedule(Long id, Schedule updatedSchedule) {
        Schedule existingSchedule = schedules.get(id);
        if (existingSchedule == null) {
            return null;
        }

        existingSchedule.setTitle(updatedSchedule.getTitle());
        existingSchedule.setDescription(updatedSchedule.getDescription());
        existingSchedule.setActivityTime(updatedSchedule.getActivityTime());
        existingSchedule.setCategory(updatedSchedule.getCategory());
        existingSchedule.setCompleted(updatedSchedule.isCompleted());
        existingSchedule.setUpdatedAt(java.time.LocalDateTime.now());

        return existingSchedule;
    }

    /**
     * Toggle completion status of a schedule
     * 
     * @param id Schedule ID
     * @param userId User ID (for security)
     * @return Updated schedule or null if not found/unauthorized
     */
    public Schedule toggleCompletion(Long id, Long userId) {
        Schedule schedule = schedules.get(id);
        if (schedule == null || !schedule.getUserId().equals(userId)) {
            return null;
        }

        schedule.setCompleted(!schedule.isCompleted());
        schedule.setUpdatedAt(java.time.LocalDateTime.now());
        return schedule;
    }

    /**
     * Delete a schedule
     * 
     * @param id Schedule ID
     * @param userId User ID (for security)
     * @return true if deleted, false if not found/unauthorized
     */
    public boolean deleteSchedule(Long id, Long userId) {
        Schedule schedule = schedules.get(id);
        if (schedule == null || !schedule.getUserId().equals(userId)) {
            return false;
        }

        schedules.remove(id);
        return true;
    }

    /**
     * Get guest mode sample schedule
     * 
     * @param date Target date
     * @return Sample schedule for elderly users
     */
    public Map<String, List<Map<String, Object>>> getGuestSchedule(LocalDate date) {
        Map<String, List<Map<String, Object>>> guestSchedule = new HashMap<>();
        
        // Morning activities
        List<Map<String, Object>> morning = Arrays.asList(
            createGuestActivity("07:00", "Wake Up & Stretch", "Gentle morning stretches to start the day"),
            createGuestActivity("07:30", "Breakfast", "Healthy breakfast with fruits and oatmeal"),
            createGuestActivity("08:30", "Morning Walk", "20-minute walk in the park"),
            createGuestActivity("09:30", "Read Newspaper", "Catch up on daily news")
        );

        // Afternoon activities
        List<Map<String, Object>> afternoon = Arrays.asList(
            createGuestActivity("12:00", "Lunch", "Nutritious lunch with vegetables"),
            createGuestActivity("13:30", "Afternoon Rest", "Short nap or quiet time"),
            createGuestActivity("15:00", "Social Activity", "Call family or friends"),
            createGuestActivity("16:00", "Hobby Time", "Gardening or reading")
        );

        // Evening activities
        List<Map<String, Object>> evening = Arrays.asList(
            createGuestActivity("18:00", "Dinner", "Light dinner with family"),
            createGuestActivity("19:30", "TV Time", "Watch favorite programs"),
            createGuestActivity("21:00", "Evening Routine", "Prepare for bed"),
            createGuestActivity("22:00", "Bedtime", "Good night sleep")
        );

        // Medication reminders
        List<Map<String, Object>> medication = Arrays.asList(
            createGuestActivity("08:00", "Morning Medication", "Blood pressure medication"),
            createGuestActivity("14:00", "Afternoon Vitamins", "Daily vitamins and supplements"),
            createGuestActivity("20:00", "Evening Medication", "Evening prescribed medications")
        );

        guestSchedule.put("morning", morning);
        guestSchedule.put("afternoon", afternoon);
        guestSchedule.put("evening", evening);
        guestSchedule.put("medication", medication);

        return guestSchedule;
    }

    /**
     * Helper method to create guest activity
     */
    private Map<String, Object> createGuestActivity(String time, String title, String description) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("time", time);
        activity.put("title", title);
        activity.put("description", description);
        activity.put("completed", false);
        return activity;
    }

    /**
     * Create default schedule for new users
     * 
     * @param userId User ID
     * @param date Target date
     */
    public void createDefaultScheduleForUser(Long userId, LocalDate date) {
        // Create some default activities for new users
        addSchedule(new Schedule(userId, date, LocalTime.of(8, 0), 
            "Morning Medication", "Take prescribed morning medications", "medication"));
        addSchedule(new Schedule(userId, date, LocalTime.of(9, 0), 
            "Morning Walk", "Light exercise and fresh air", "morning"));
        addSchedule(new Schedule(userId, date, LocalTime.of(12, 0), 
            "Lunch", "Healthy midday meal", "afternoon"));
        addSchedule(new Schedule(userId, date, LocalTime.of(20, 0), 
            "Evening Medication", "Take prescribed evening medications", "medication"));
    }
} 