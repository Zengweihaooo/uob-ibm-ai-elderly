package com.example.demo.controller;

import com.example.demo.pojo.ImportantDate;
import com.example.demo.service.ImportantDateService;
// import com.example.demo.service.UserService; // Unused
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.util.JwtUtil;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for managing important dates and email reminders
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
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // @Autowired
    // private UserService userService; // Not used currently

    /**
     * Add a new important date
     * 
     * @param requestBody Request body containing important date details
     * @return Response with created important date
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addImportantDate(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = requireUserId(authHeader);
            String title = (String) requestBody.get("title");
            String dateStr = (String) requestBody.get("date");
            String type = (String) requestBody.get("type");
            String description = (String) requestBody.get("description");
            Boolean enabled = (Boolean) requestBody.getOrDefault("enabled", Boolean.TRUE);
            
            LocalDate date = LocalDate.parse(dateStr);
            
            ImportantDate importantDate = importantDateService.addImportantDate(
                userId, title, date, type, description
            );
            if (enabled != null) {
                importantDate.setEnabled(enabled);
            }
            
            response.put("success", true);
            response.put("message", "Important date added successfully");
            response.put("importantDate", importantDate);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add important date: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get all important dates for the authenticated user (frontend compatibility)
     * 
     * Frontend expects: GET /api/important-dates/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllForCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = requireUserId(authHeader);
            List<ImportantDate> importantDates = importantDateService.getImportantDatesByUser(userId);
            response.put("success", true);
            response.put("importantDates", importantDates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get important dates: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get stats for the authenticated user (frontend compatibility)
     * 
     * Frontend expects: GET /api/important-dates/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatsForCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = requireUserId(authHeader);
            Map<String, Object> stats = importantDateService.getImportantDateStats(userId);
            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get important date stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all important dates for a user
     * 
     * @param userId User ID
     * @return List of important dates
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getImportantDatesByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ImportantDate> importantDates = importantDateService.getImportantDatesByUser(userId);
            
            response.put("success", true);
            response.put("importantDates", importantDates);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get important dates: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get upcoming important dates for a user
     * 
     * @param userId User ID
     * @return List of upcoming important dates
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingImportantDates(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ImportantDate> upcomingDates = importantDateService.getUpcomingImportantDates(userId);
            
            response.put("success", true);
            response.put("upcomingDates", upcomingDates);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get upcoming important dates: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get important date statistics for a user
     * 
     * @param userId User ID
     * @return Statistics map
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getImportantDateStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = importantDateService.getImportantDateStats(userId);
            
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get important date stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update an important date
     * 
     * @param id Important date ID
     * @param requestBody Updated important date data
     * @return Response with updated important date
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateImportantDate(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ImportantDate updatedDate = new ImportantDate();
            updatedDate.setId(id);
            updatedDate.setTitle((String) requestBody.get("title"));
            updatedDate.setDescription((String) requestBody.get("description"));
            updatedDate.setDate(LocalDate.parse((String) requestBody.get("date")));
            updatedDate.setType((String) requestBody.get("type"));
            updatedDate.setRepeatCycle((String) requestBody.get("repeatCycle"));
            updatedDate.setEnabled((Boolean) requestBody.get("enabled"));
            
            ImportantDate result = importantDateService.updateImportantDate(id, updatedDate);
            
            response.put("success", true);
            response.put("message", "Important date updated successfully");
            response.put("importantDate", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update important date: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete an important date
     * 
     * @param id Important date ID
     * @return Response with deletion result
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteImportantDate(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = importantDateService.deleteImportantDate(id);
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Important date deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Important date not found");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete important date: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Toggle important date enabled status
     * 
     * @param id Important date ID
     * @return Response with updated important date
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleImportantDate(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ImportantDate result = importantDateService.toggleImportantDate(id);
            
            response.put("success", true);
            response.put("message", "Important date status toggled successfully");
            response.put("importantDate", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to toggle important date: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Compatibility for frontend calling PUT /{id}/toggle
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleImportantDatePut(@PathVariable Long id) {
        return toggleImportantDate(id);
    }

    /**
     * Explicitly enable/disable an important date (no deletion)
     */
    @PutMapping("/{id}/enabled")
    public ResponseEntity<Map<String, Object>> setEnabled(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object enabledObj = body.get("enabled");
            if (enabledObj == null) {
                response.put("success", false);
                response.put("message", "Missing 'enabled' field");
                return ResponseEntity.badRequest().body(response);
            }
            boolean enabled = Boolean.TRUE.equals(enabledObj) || (enabledObj instanceof Boolean b && b);
            ImportantDate result = importantDateService.setImportantDateEnabled(id, enabled);
            response.put("success", true);
            response.put("message", "Important date status updated");
            response.put("importantDate", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Manually trigger email reminders for all pending important dates
     * 
     * @return Response with reminder sending result
     */
    @PostMapping("/send-reminders")
    public ResponseEntity<Map<String, Object>> sendReminders() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            importantDateService.sendAllPendingReminders();
            
            response.put("success", true);
            response.put("message", "Reminders sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send reminders: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get default holidays for a user
     * 
     * @param userId User ID
     * @return Response with default holidays
     */
    @PostMapping("/user/{userId}/default-holidays")
    public ResponseEntity<Map<String, Object>> getDefaultHolidays(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ImportantDate> holidays = importantDateService.getDefaultHolidays(userId);
            
            response.put("success", true);
            response.put("message", "Default holidays added successfully");
            response.put("holidays", holidays);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add default holidays: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== Helper ====================
    private Long requireUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Authorization header");
        }
        String header = authHeader.trim();
        String prefix = "Bearer ";
        if (!header.regionMatches(true, 0, prefix, 0, prefix.length())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization must be Bearer token");
        }
        String token = header.substring(prefix.length()).trim();
        if (!jwtUtil.isValidToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired JWT");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT missing userId");
        }
        return userId;
    }
} 