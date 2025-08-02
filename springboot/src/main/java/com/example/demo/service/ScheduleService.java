package com.example.demo.service;

import com.example.demo.pojo.Schedule;
import com.example.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserService userService;

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
        
        // Morning activities with enhanced features
        List<Map<String, Object>> morning = Arrays.asList(
            createGuestActivity("07:00", "Wake Up & Stretch", "Gentle morning stretches to start the day", "low", null, null, "daily", "5min", false, null),
            createGuestActivity("07:30", "Breakfast", "Healthy breakfast with fruits and oatmeal", "medium", null, null, "daily", "15min", false, null),
            createGuestActivity("08:30", "Morning Walk", "20-minute walk in the park", "high", null, null, "daily", "15min", true, "City Park"),
            createGuestActivity("09:30", "Read Newspaper", "Catch up on daily news", "low", null, null, "daily", "30min", false, null)
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

        // Medication reminders with emergency contacts
        List<Map<String, Object>> medication = Arrays.asList(
            createGuestActivity("08:00", "Morning Medication", "Blood pressure medication", "high", "+1234567890", "Dr. Smith", "daily", "15min", false, null),
            createGuestActivity("14:00", "Afternoon Vitamins", "Daily vitamins and supplements", "medium", null, null, "daily", "30min", false, null),
            createGuestActivity("20:00", "Evening Medication", "Evening prescribed medications", "high", "+1234567890", "Dr. Smith", "daily", "15min", false, null)
        );

        guestSchedule.put("morning", morning);
        guestSchedule.put("afternoon", afternoon);
        guestSchedule.put("evening", evening);
        guestSchedule.put("medication", medication);

        return guestSchedule;
    }

    /**
     * Helper method to create guest activity with enhanced features
     */
    private Map<String, Object> createGuestActivity(String time, String title, String description) {
        return createGuestActivity(time, title, description, "medium", null, null, "none", "15min", false, null);
    }

    /**
     * Helper method to create guest activity with full parameters
     */
    private Map<String, Object> createGuestActivity(String time, String title, String description, 
                                                   String priority, String emergencyContact, String emergencyContactName,
                                                   String repeatCycle, String notificationTime, 
                                                   boolean locationReminder, String locationName) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("time", time);
        activity.put("title", title);
        activity.put("description", description);
        activity.put("completed", false);
        activity.put("priority", priority);
        activity.put("emergencyContact", emergencyContact);
        activity.put("emergencyContactName", emergencyContactName);
        activity.put("repeatCycle", repeatCycle);
        activity.put("notificationTime", notificationTime);
        activity.put("locationReminder", locationReminder);
        activity.put("locationName", locationName);
        activity.put("notes", "");
        activity.put("isAllDay", false);
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

    /**
     * Get users who do not have a schedule for today
     * 
     * @return List of users without today's schedule
     */
    public List<User> getUsersWithoutTodaySchedule() {
        return getUsersWithoutTodaySchedule(LocalDate.now());
    }

    /**
     * Get users who do not have a schedule for a specific date
     * 
     * @param date Target date
     * @return List of users without schedule for the specified date
     */
    public List<User> getUsersWithoutTodaySchedule(LocalDate date) {
        List<User> allUsers = userService.getAllUsers();
        List<User> usersWithoutTodaySchedule = new ArrayList<>();

        for (User user : allUsers) {
            if (!hasScheduleForDate(user.getEmail(), date)) {
                usersWithoutTodaySchedule.add(user);
            }
        }
        return usersWithoutTodaySchedule;
    }

    /**
     * Helper method to check if a user has a schedule for a specific date
     */
    private boolean hasScheduleForDate(String userEmail, LocalDate date) {
        return schedules.values().stream()
                .anyMatch(schedule -> schedule.getUserId().equals(userEmail) && schedule.getScheduleDate().equals(date));
    }
} 