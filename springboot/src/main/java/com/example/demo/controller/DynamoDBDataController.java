package com.example.demo.controller;

import com.example.demo.service.DynamoDBHealthRecordService;
import com.example.demo.service.DynamoDBUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * DynamoDB Data Controller
 * Provides API endpoints for DynamoDB data operations
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
@RequestMapping("/api/dynamodb")
@Profile("aws")
@CrossOrigin(origins = "*")
public class DynamoDBDataController {
    
    private static final Logger logger = Logger.getLogger(DynamoDBDataController.class.getName());
    
    @Autowired
    private DynamoDBHealthRecordService healthRecordService;
    
    @Autowired
    private DynamoDBUserService userService;
    
    /**
     * Save health record to DynamoDB
     */
    @PostMapping("/health-record")
    public ResponseEntity<Map<String, Object>> saveHealthRecord(
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam(required = false) String notes,
            @RequestParam(defaultValue = "false") boolean isAbnormal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            healthRecordService.saveHealthRecord(type, value, notes, isAbnormal);
            
            response.put("success", true);
            response.put("message", "Health record saved to DynamoDB successfully");
            response.put("type", type);
            response.put("value", value);
            response.put("isAbnormal", isAbnormal);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to save health record: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to save health record: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Save user to DynamoDB
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> saveUser(
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            userService.saveUser(email, name, role);
            
            response.put("success", true);
            response.put("message", "User saved to DynamoDB successfully");
            response.put("email", email);
            response.put("name", name);
            response.put("role", role);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to save user: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to save user: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get health record count
     */
    @GetMapping("/health-record/count")
    public ResponseEntity<Map<String, Object>> getHealthRecordCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int count = healthRecordService.getHealthRecordCount();
            
            response.put("success", true);
            response.put("count", count);
            response.put("message", "Health record count retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to get health record count: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to get health record count: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get user count
     */
    @GetMapping("/user/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int count = userService.getUserCount();
            
            response.put("success", true);
            response.put("count", count);
            response.put("message", "User count retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to get user count: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to get user count: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get all health records
     */
    @GetMapping("/health-record/all")
    public ResponseEntity<Map<String, Object>> getAllHealthRecords() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var records = healthRecordService.getAllHealthRecords();
            
            response.put("success", true);
            response.put("records", records);
            response.put("count", records.size());
            response.put("message", "Health records retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to get health records: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to get health records: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get all users
     */
    @GetMapping("/user/all")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var users = userService.getAllUsers();
            
            response.put("success", true);
            response.put("users", users);
            response.put("count", users.size());
            response.put("message", "Users retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to get users: " + e.getMessage());
            
            response.put("success", false);
            response.put("message", "Failed to get users: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
