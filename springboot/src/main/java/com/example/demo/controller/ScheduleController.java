package com.example.demo.controller;

import com.example.demo.pojo.Schedule;
import com.example.demo.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for schedule management
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@RestController
@RequestMapping("/api/schedule")
@CrossOrigin(origins = "*") // Allow CORS for frontend access
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Get schedule for a specific date
     * Guest mode returns sample data, authenticated users get their personal schedule
     * 
     * @param date Date in YYYY-MM-DD format
     * @param request HTTP request to check for authentication
     * @return Schedule data organized by categories
     */
    @GetMapping("/{date}")
    public ResponseEntity<Map<String, Object>> getSchedule(
            @PathVariable String date,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        try {
            LocalDate targetDate = LocalDate.parse(date);
            Map<String, Object> response = new HashMap<>();
            
            // Check if user is authenticated
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extract user ID from token
                // Token format: "user-token-{userId}-{timestamp}"
                String token = authHeader.substring(7); // Remove "Bearer " prefix
                Long userId = 1L; // Default user ID
                
                if (token.startsWith("user-token-")) {
                    String[] parts = token.split("-");
                    if (parts.length >= 3) {
                        try {
                            userId = Long.parseLong(parts[2]);
                        } catch (NumberFormatException e) {
                            // If parsing fails, use default user ID
                            userId = 1L;
                        }
                    }
                }
                
                Map<String, List<Schedule>> userSchedule = scheduleService.getSchedulesByUserAndDate(userId, targetDate);
                response.put("success", true);
                response.put("schedule", userSchedule);
                response.put("isGuest", false);
            } else {
                // Guest mode - return sample data
                Map<String, List<Map<String, Object>>> guestSchedule = scheduleService.getGuestSchedule(targetDate);
                response.put("success", true);
                response.put("schedule", guestSchedule);
                response.put("isGuest", true);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (DateTimeParseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid date format. Please use YYYY-MM-DD");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Add a new activity to the schedule
     * Requires authentication
     * 
     * @param activityData Activity data from request body
     * @param authHeader Authorization header
     * @return Created activity or error message
     */
    @PostMapping("/activity")
    public ResponseEntity<Map<String, Object>> addActivity(
            @RequestBody Map<String, Object> activityData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // Extract user ID from token
            // Token format: "user-token-{userId}-{timestamp}"
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            Long userId = 1L; // Default user ID
            
            if (token.startsWith("user-token-")) {
                String[] parts = token.split("-");
                if (parts.length >= 3) {
                    try {
                        userId = Long.parseLong(parts[2]);
                    } catch (NumberFormatException e) {
                        // If parsing fails, use default user ID
                        userId = 1L;
                    }
                }
            }
            
            // Parse and validate input data
            LocalDate scheduleDate = LocalDate.parse((String) activityData.get("date"));
            LocalTime activityTime = LocalTime.parse((String) activityData.get("time"));
            String title = (String) activityData.get("title");
            String description = (String) activityData.get("description");
            String category = (String) activityData.get("category");
            
            // Parse enhanced fields
            String priority = (String) activityData.getOrDefault("priority", "medium");
            String emergencyContact = (String) activityData.get("emergencyContact");
            String emergencyContactName = (String) activityData.get("emergencyContactName");
            String repeatCycle = (String) activityData.getOrDefault("repeatCycle", "none");
            String notificationTime = (String) activityData.getOrDefault("notificationTime", "15min");
            Boolean locationReminder = (Boolean) activityData.getOrDefault("locationReminder", false);
            String locationName = (String) activityData.get("locationName");
            String notes = (String) activityData.get("notes");
            Boolean isAllDay = (Boolean) activityData.getOrDefault("isAllDay", false);
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Activity title is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (category == null || !isValidCategory(category)) {
                response.put("success", false);
                response.put("message", "Valid category is required (morning, afternoon, evening, medication)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create and save the schedule
            Schedule newSchedule = new Schedule(userId, scheduleDate, activityTime, title, description, category);
            
            // Set enhanced fields
            newSchedule.setPriority(priority);
            newSchedule.setEmergencyContact(emergencyContact);
            newSchedule.setEmergencyContactName(emergencyContactName);
            newSchedule.setRepeatCycle(repeatCycle);
            newSchedule.setNotificationTime(notificationTime);
            newSchedule.setLocationReminder(locationReminder);
            newSchedule.setLocationName(locationName);
            newSchedule.setNotes(notes);
            newSchedule.setAllDay(isAllDay);
            
            Schedule savedSchedule = scheduleService.addSchedule(newSchedule);
            
            response.put("success", true);
            response.put("message", "Activity added successfully");
            response.put("activity", savedSchedule);
            return ResponseEntity.ok(response);
            
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Invalid date or time format");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Toggle completion status of an activity
     * 
     * @param id Activity ID
     * @param authHeader Authorization header
     * @return Updated activity or error message
     */
    @PutMapping("/activity/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleActivity(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // TODO: Extract user ID from JWT token
            Long userId = 1L;
            
            Schedule updatedSchedule = scheduleService.toggleCompletion(id, userId);
            if (updatedSchedule == null) {
                response.put("success", false);
                response.put("message", "Activity not found or access denied");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("message", "Activity status updated");
            response.put("activity", updatedSchedule);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete an activity
     * 
     * @param id Activity ID
     * @param authHeader Authorization header
     * @return Success message or error
     */
    @DeleteMapping("/activity/{id}")
    public ResponseEntity<Map<String, Object>> deleteActivity(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // TODO: Extract user ID from JWT token
            Long userId = 1L;
            
            boolean deleted = scheduleService.deleteSchedule(id, userId);
            if (!deleted) {
                response.put("success", false);
                response.put("message", "Activity not found or access denied");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("message", "Activity deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update an existing activity
     * 
     * @param id Activity ID
     * @param activityData Updated activity data
     * @param authHeader Authorization header
     * @return Updated activity or error message
     */
    @PutMapping("/activity/{id}")
    public ResponseEntity<Map<String, Object>> updateActivity(
            @PathVariable Long id,
            @RequestBody Map<String, String> activityData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // Parse input data
            LocalTime activityTime = LocalTime.parse(activityData.get("time"));
            String title = activityData.get("title");
            String description = activityData.get("description");
            String category = activityData.get("category");
            
            // Create updated schedule object
            Schedule updatedSchedule = new Schedule();
            updatedSchedule.setActivityTime(activityTime);
            updatedSchedule.setTitle(title);
            updatedSchedule.setDescription(description);
            updatedSchedule.setCategory(category);
            
            Schedule result = scheduleService.updateSchedule(id, updatedSchedule);
            if (result == null) {
                response.put("success", false);
                response.put("message", "Activity not found");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("message", "Activity updated successfully");
            response.put("activity", result);
            return ResponseEntity.ok(response);
            
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Invalid time format");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating activity: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Validate if category is valid
     * 
     * @param category Category to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidCategory(String category) {
        return category != null && (
            category.equals("morning") || 
            category.equals("afternoon") || 
            category.equals("evening") || 
            category.equals("medication"));
    }

    /**
     * Confirm completion of a reminder/activity
     * 
     * @param id Activity ID
     * @param authHeader Authorization header
     * @return Confirmation result
     */
    @PostMapping("/reminder/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmReminder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // TODO: Extract user ID from JWT token
            Long userId = 1L;
            
            boolean confirmed = scheduleService.confirmReminder(id, userId);
            
            if (confirmed) {
                response.put("success", true);
                response.put("message", "Reminder confirmed successfully");
                response.put("reminderId", id);
            } else {
                response.put("success", false);
                response.put("message", "Reminder not found or already confirmed");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to confirm reminder: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Repeat reminder until confirmed
     * 
     * @param id Activity ID
     * @param authHeader Authorization header
     * @return Repeat reminder result
     */
    @PostMapping("/reminder/{id}/repeat")
    public ResponseEntity<Map<String, Object>> repeatReminder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            // TODO: Extract user ID from JWT token
            Long userId = 1L;
            
            scheduleService.repeatReminder(id, userId);
            
            response.put("success", true);
            response.put("message", "Reminder repeated successfully");
            response.put("reminderId", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to repeat reminder: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 