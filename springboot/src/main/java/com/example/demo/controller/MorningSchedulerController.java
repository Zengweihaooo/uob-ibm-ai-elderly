package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.pojo.Schedule;
import com.example.demo.service.MorningScheduler;
import com.example.demo.service.ScheduleService;

/**
 * Morning Scheduler Controller
 * Provides REST API endpoints related to morning scheduling and routines.
 *
 * Author: Lepeng Zhou
 * Version: 1.0
 */
//@RestController
//@RequestMapping("/api/morning")
@CrossOrigin(origins = "*")
public class MorningSchedulerController {

    private final MorningScheduler morningScheduler;
    private final ScheduleService scheduleService;

    @Autowired
    public MorningSchedulerController(MorningScheduler morningScheduler, 
                                    ScheduleService scheduleService) {
        this.morningScheduler = morningScheduler;
        this.scheduleService = scheduleService;
    }

    /**
     * Manually trigger a morning greeting (for testing).
     *
     * @param userId user ID
     * @return operation result
     */
    @PostMapping("/greeting/trigger")
    public ResponseEntity<Map<String, Object>> triggerMorningGreeting(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = morningScheduler.triggerMorningGreeting(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Morning greeting triggered");
                response.put("userId", userId);
            } else {
                response.put("success", false);
                response.put("message", "Morning greeting already sent today");
                response.put("userId", userId);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to trigger morning greeting: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process a user intent and create the corresponding TODO item.
     *
     * @param userId user ID
     * @param requestBody request body containing the intent
     * @return created TODO item
     */
    @PostMapping("/intent")
    public ResponseEntity<Map<String, Object>> handleIntent(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String intent = (String) requestBody.get("intent");
            
            if (intent == null || intent.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Intent must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Schedule createdTodo = morningScheduler.handleIntent(userId, intent);
            
            if (createdTodo != null) {
                response.put("success", true);
                response.put("message", "Successfully processed user intent");
                response.put("todo", createdTodo);
                response.put("intent", intent);
            } else {
                response.put("success", false);
                response.put("message", "Failed to process user intent or intent unknown");
                response.put("intent", intent);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to process user intent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get morning schedule suggestions for the user.
     *
     * @param userId user ID
     * @return morning schedule suggestions
     */
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getMorningSuggestions(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> suggestions = morningScheduler.getMorningSuggestions(userId);
            
            response.put("success", true);
            response.put("suggestions", suggestions);
            response.put("userId", userId);
            response.put("count", suggestions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get morning suggestions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get the user's morning schedule for today.
     *
     * @param userId user ID
     * @return today's morning schedule
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getTodayMorningSchedule(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Schedule> todaySchedule = scheduleService.getTodaySchedule(userId);
            
            // Filter morning activities (before 12:00)
            List<Schedule> morningSchedule = todaySchedule.stream()
                .filter(schedule -> schedule.getActivityTime().getHour() < 12)
                .sorted((a, b) -> a.getActivityTime().compareTo(b.getActivityTime()))
                .toList();
            
            response.put("success", true);
            response.put("morningSchedule", morningSchedule);
            response.put("userId", userId);
            response.put("count", morningSchedule.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get morning schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check whether the user has a morning greeting.
     *
     * @param userId user ID
     * @return check result
     */
    @GetMapping("/greeting/check")
    public ResponseEntity<Map<String, Object>> checkMorningGreeting(
            @RequestParam Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean hasGreeting = scheduleService.hasMorningGreeting(userId);
            
            response.put("success", true);
            response.put("hasMorningGreeting", hasGreeting);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to check morning greeting: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get list of available user intents.
     *
     * @return list of available intents
     */
    @GetMapping("/intents")
    public ResponseEntity<Map<String, Object>> getAvailableIntents() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, String> intents = new HashMap<>();
            intents.put("SCHEDULE_PODCAST", "Schedule podcast time");
            intents.put("REMIND_WALK", "Remind to walk");
            intents.put("MESSAGE_FAMILY", "Contact family");
            intents.put("MORNING_EXERCISE", "Morning exercise");
            intents.put("BREAKFAST_REMINDER", "Breakfast reminder");
            
            response.put("success", true);
            response.put("intents", intents);
            response.put("count", intents.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get intent list: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Batch process multiple intents.
     *
     * @param userId user ID
     * @param requestBody request body containing an intents array
     * @return processing result
     */
    @PostMapping("/intents/batch")
    public ResponseEntity<Map<String, Object>> handleMultipleIntents(
            @RequestParam Long userId,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<String> intents = (List<String>) requestBody.get("intents");
            
            if (intents == null || intents.isEmpty()) {
                response.put("success", false);
                response.put("message", "Intent list must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Schedule> createdTodos = new java.util.ArrayList<>();
            List<String> failedIntents = new java.util.ArrayList<>();
            
            for (String intent : intents) {
                try {
                    Schedule todo = morningScheduler.handleIntent(userId, intent);
                    if (todo != null) {
                        createdTodos.add(todo);
                    } else {
                        failedIntents.add(intent);
                    }
                } catch (Exception e) {
                    failedIntents.add(intent + " (error: " + e.getMessage() + ")");
                }
            }
            
            response.put("success", true);
            response.put("createdTodos", createdTodos);
            response.put("failedIntents", failedIntents);
            response.put("userId", userId);
            response.put("totalIntents", intents.size());
            response.put("successCount", createdTodos.size());
            response.put("failedCount", failedIntents.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to batch process intents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
