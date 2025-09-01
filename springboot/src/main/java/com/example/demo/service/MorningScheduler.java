package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.Schedule;
import com.example.demo.pojo.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Morning Scheduler Service
 * Responsible for creating morning greetings and schedules for active users
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
//@Service
public class MorningScheduler {

    private static final Logger logger = Logger.getLogger(MorningScheduler.class.getName());
    
    private final ScheduleService scheduleService;
    private final UserService userService;
    private final PetMoodService petMoodService;

    @Autowired
    public MorningScheduler(ScheduleService scheduleService, 
                           UserService userService,
                           PetMoodService petMoodService) {
        this.scheduleService = scheduleService;
        this.userService = userService;
        this.petMoodService = petMoodService;
    }

    @Value("${pet.wakeupHour:8}")
    private int wakeupHour;
    
    @Value("${pet.defaultPodcastTime:15:00}")
    private String defaultPodcastTime;

    /**
     * Daily morning cron task
     * Use pet.morning.cron for debugging; production example: 0 0 8 * * *
     */
    @Scheduled(cron = "${pet.morning.cron:0 0 8 * * *}")
    public void morningPing() {
        logger.info("🌅 Start executing morning greeting task...");
        
        try {
            List<Long> activeUserIds = scheduleService.activeUserIds();
            logger.info("Found " + activeUserIds.size() + " active users");
            
            for (Long userId : activeUserIds) {
                try {
                    // Check if morning greeting already exists
                    if (!scheduleService.hasMorningGreeting(userId)) {
                        // Create morning greeting
                        Schedule morningGreeting = scheduleService.createTodo(
                            userId, 
                            "Morning Greeting", 
                            "What would you like to do today? I can schedule podcasts / remind you to walk / contact family.", 
                            "08:05"
                        );
                        
                        // Update pet mood (morning interaction)
                        petMoodService.adjustMood(userId, 10);
                        
                        logger.info("Created morning greeting for user " + userId + ": " + morningGreeting.getTitle());
                    }
                    
                    // Create default morning activities
                    createDefaultMorningActivities(userId);
                    
                } catch (Exception e) {
                    logger.warning("Failed to create morning greeting for user " + userId + ": " + e.getMessage());
                }
            }
            
            logger.info("✅ Morning greeting task completed");
            
        } catch (Exception e) {
            logger.severe("Morning greeting task failed: " + e.getMessage());
        }
    }

    /**
     * Handle user intent and create corresponding todos
     * 
     * @param userId User ID
     * @param intent User intent
     * @return created todo
     */
    public Schedule handleIntent(Long userId, String intent) {
        logger.info("Handling intent for user " + userId + ": " + intent);
        
        Schedule createdTodo = null;
        
        try {
            switch (intent) {
                case "SCHEDULE_PODCAST" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "Play Podcast", 
                        "Auto-scheduled podcast time to relax", 
                        defaultPodcastTime
                    );
                    logger.info("Scheduled podcast time for user " + userId);
                }
                
                case "REMIND_WALK" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "Walk for 20 minutes", 
                        "Health reminder: outdoor walk and fresh air", 
                        "16:00"
                    );
                    logger.info("Scheduled walking reminder for user " + userId);
                }
                
                case "MESSAGE_FAMILY" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "Message family", 
                        "Caring communication: contact family and share today's mood", 
                        "10:00"
                    );
                    logger.info("Scheduled family contact reminder for user " + userId);
                }
                
                case "MORNING_EXERCISE" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "Morning exercise", 
                        "Light morning exercise: stretches to wake up the body", 
                        "07:30"
                    );
                    logger.info("Scheduled morning exercise for user " + userId);
                }
                
                case "BREAKFAST_REMINDER" -> {
                    createdTodo = scheduleService.createTodo(
                        userId, 
                        "Breakfast reminder", 
                        "Nutritious breakfast: remember to eat and replenish energy", 
                        "08:00"
                    );
                    logger.info("Scheduled breakfast reminder for user " + userId);
                }
                
                default -> {
                    logger.warning("Unknown user intent: " + intent);
                    return null;
                }
            }
            
            // If todo created successfully, increase pet experience
            if (createdTodo != null) {
                petMoodService.addExperience(userId, 5);
            }
            
        } catch (Exception e) {
            logger.severe("Failed to handle user intent: " + e.getMessage());
        }
        
        return createdTodo;
    }

    /**
     * Create default morning activities
     * 
     * @param userId User ID
     */
    private void createDefaultMorningActivities(Long userId) {
        try {
            LocalDate today = LocalDate.now();
            
            // Check if morning activities already exist
            List<Schedule> todaySchedule = scheduleService.getTodaySchedule(userId);
            boolean hasMorningActivities = todaySchedule.stream()
                .anyMatch(schedule -> schedule.getCategory().equals("morning") && 
                                   schedule.getActivityTime().isBefore(LocalTime.of(12, 0)));
            
            if (!hasMorningActivities) {
                // Create default morning activities
                scheduleService.createTodo(
                    userId, 
                    "Morning stretch", 
                    "Gentle stretches to wake up the body", 
                    "07:30"
                );
                
                scheduleService.createTodo(
                    userId, 
                    "Breakfast time", 
                    "Balanced nutritious breakfast", 
                    "08:00"
                );
                
                scheduleService.createTodo(
                    userId, 
                    "Morning walk", 
                    "15-minute walk in the park for fresh air", 
                    "08:30"
                );
                
                logger.info("Created default morning activities for user " + userId);
            }
            
        } catch (Exception e) {
            logger.warning("Failed to create default morning activities for user " + userId + ": " + e.getMessage());
        }
    }

    /**
     * Manually trigger morning greeting (for testing)
     * 
     * @param userId User ID
     * @return success flag
     */
    public boolean triggerMorningGreeting(Long userId) {
        try {
            if (!scheduleService.hasMorningGreeting(userId)) {
                Schedule morningGreeting = scheduleService.createTodo(
                    userId, 
                    "Morning Greeting", 
                    "What would you like to do today? I can schedule podcasts / remind you to walk / contact family.", 
                    "08:05"
                );
                
                // Update pet mood
                petMoodService.adjustMood(userId, 10);
                
                logger.info("Manually triggered morning greeting for user " + userId);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.severe("Failed to manually trigger morning greeting: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user's morning schedule suggestions
     * 
     * @param userId User ID
     * @return suggestions list
     */
    public List<String> getMorningSuggestions(Long userId) {
        List<String> suggestions = new java.util.ArrayList<>();
        
        try {
            // Provide personalized suggestions based on user status
            var petStatus = petMoodService.getFullPetStatus(userId);
            int moodScore = (Integer) petStatus.get("moodScore");
            
            if (moodScore < 0) {
                suggestions.add("Not feeling great today. Consider listening to some light music.");
                suggestions.add("Schedule an outdoor walk to improve mood.");
            } else if (moodScore > 20) {
                suggestions.add("You're in a good mood today. Try a new activity.");
                suggestions.add("Consider contacting friends to share your good mood.");
            } else {
                suggestions.add("Mood is stable today. Keep a regular routine.");
                suggestions.add("Plan some light activities.");
            }
            
            // Add general suggestions
            suggestions.add("Remember to take medications on time.");
            suggestions.add("Maintain moderate exercise.");
            suggestions.add("Stay in touch with family and friends.");
            
        } catch (Exception e) {
            logger.warning("Failed to get morning suggestions: " + e.getMessage());
            // Return default suggestions
            suggestions.add("Keep a regular routine.");
            suggestions.add("Exercise moderately.");
            suggestions.add("Maintain social connections.");
        }
        
        return suggestions;
    }
}
