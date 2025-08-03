package com.example.demo.controller;

import com.example.demo.pojo.ImportantDate;
import com.example.demo.service.ImportantDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for important dates management
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@RestController
@RequestMapping("/api/important-dates")
@CrossOrigin(origins = "*")
public class ImportantDateController {

    @Autowired
    private ImportantDateService importantDateService;

    /**
     * Add a new important date
     * 
     * @param dateData Important date data from request body
     * @param authHeader Authorization header
     * @return Created important date or error message
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addImportantDate(
            @RequestBody Map<String, Object> dateData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            // Extract date data
            String title = (String) dateData.get("title");
            String dateStr = (String) dateData.get("date");
            String type = (String) dateData.get("type");
            String description = (String) dateData.getOrDefault("description", "");

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (dateStr == null || dateStr.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Date is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (type == null || type.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Type is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate date format
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                response.put("success", false);
                response.put("message", "Invalid date format. Please use YYYY-MM-DD");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate type
            if (!isValidType(type)) {
                response.put("success", false);
                response.put("message", "Invalid type. Must be: birthday, anniversary, holiday, custom");
                return ResponseEntity.badRequest().body(response);
            }

            // Create important date
            ImportantDate importantDate = importantDateService.addImportantDate(
                userId, title, date, type, description
            );

            response.put("success", true);
            response.put("message", "Important date added successfully");
            response.put("importantDate", importantDate);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add important date: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all important dates for the user
     * 
     * @param authHeader Authorization header
     * @return List of important dates
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllImportantDates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<ImportantDate> importantDates = importantDateService.getImportantDatesByUser(userId);

            response.put("success", true);
            response.put("importantDates", importantDates);
            response.put("count", importantDates.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching important dates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get important dates by type
     * 
     * @param type Date type
     * @param authHeader Authorization header
     * @return List of important dates of specified type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getImportantDatesByType(
            @PathVariable String type,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            if (!isValidType(type)) {
                response.put("success", false);
                response.put("message", "Invalid type. Must be: birthday, anniversary, holiday, custom");
                return ResponseEntity.badRequest().body(response);
            }

            List<ImportantDate> importantDates = importantDateService.getImportantDatesByType(userId, type);

            response.put("success", true);
            response.put("importantDates", importantDates);
            response.put("type", type);
            response.put("count", importantDates.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching important dates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get upcoming important dates
     * 
     * @param authHeader Authorization header
     * @return List of upcoming important dates
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingImportantDates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<ImportantDate> upcomingDates = importantDateService.getUpcomingImportantDates(userId);

            response.put("success", true);
            response.put("upcomingDates", upcomingDates);
            response.put("count", upcomingDates.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching upcoming dates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get today's important dates
     * 
     * @param authHeader Authorization header
     * @return List of today's important dates
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayImportantDates(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<ImportantDate> todayDates = importantDateService.getTodayImportantDates(userId);

            response.put("success", true);
            response.put("todayDates", todayDates);
            response.put("count", todayDates.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching today's dates: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update an important date
     * 
     * @param id Important date ID
     * @param dateData Updated date data
     * @param authHeader Authorization header
     * @return Updated important date
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateImportantDate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dateData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            // Extract updated data
            String title = (String) dateData.get("title");
            String dateStr = (String) dateData.get("date");
            String type = (String) dateData.get("type");
            String description = (String) dateData.getOrDefault("description", "");

            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (dateStr == null || dateStr.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Date is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (type == null || type.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Type is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate date format
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (DateTimeParseException e) {
                response.put("success", false);
                response.put("message", "Invalid date format. Please use YYYY-MM-DD");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate type
            if (!isValidType(type)) {
                response.put("success", false);
                response.put("message", "Invalid type. Must be: birthday, anniversary, holiday, custom");
                return ResponseEntity.badRequest().body(response);
            }

            // Create updated important date
            ImportantDate updatedDate = new ImportantDate(userId, title, date, type);
            updatedDate.setDescription(description);

            // Update important date
            ImportantDate importantDate = importantDateService.updateImportantDate(id, updatedDate);

            response.put("success", true);
            response.put("message", "Important date updated successfully");
            response.put("importantDate", importantDate);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update important date: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete an important date
     * 
     * @param id Important date ID
     * @param authHeader Authorization header
     * @return Deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteImportantDate(
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
            boolean deleted = importantDateService.deleteImportantDate(id);

            if (deleted) {
                response.put("success", true);
                response.put("message", "Important date deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Important date not found");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete important date: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Toggle important date enabled status
     * 
     * @param id Important date ID
     * @param authHeader Authorization header
     * @return Updated important date
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleImportantDate(
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
            ImportantDate importantDate = importantDateService.toggleImportantDate(id);

            response.put("success", true);
            response.put("message", "Important date toggled successfully");
            response.put("importantDate", importantDate);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to toggle important date: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get important date statistics
     * 
     * @param authHeader Authorization header
     * @return Statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getImportantDateStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            Map<String, Object> stats = importantDateService.getImportantDateStats(userId);

            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Add default holidays for a user
     * 
     * @param authHeader Authorization header
     * @return List of added holidays
     */
    @PostMapping("/add-default-holidays")
    public ResponseEntity<Map<String, Object>> addDefaultHolidays(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            List<ImportantDate> holidays = importantDateService.getDefaultHolidays(userId);

            response.put("success", true);
            response.put("message", "Default holidays added successfully");
            response.put("holidays", holidays);
            response.put("count", holidays.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add default holidays: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Validate important date type
     * 
     * @param type Date type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidType(String type) {
        return type != null && (type.equals("birthday") || type.equals("anniversary") || 
                               type.equals("holiday") || type.equals("custom"));
    }
} 