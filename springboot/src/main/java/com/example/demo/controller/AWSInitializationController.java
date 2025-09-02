package com.example.demo.controller;

import com.example.demo.service.DynamoDBTableManager;
import com.example.demo.service.DataMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AWS services initialization controller.
 * Provides API endpoints to manage AWS service initialization and data migration.
 *
 * Author: Lepeng Zhou
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/aws")
@Profile("aws") // Only active in AWS profile
public class AWSInitializationController {
    
    private static final Logger logger = Logger.getLogger(AWSInitializationController.class.getName());
    
    @Autowired
    private DynamoDBTableManager tableManager;
    
    @Autowired
    private DataMigrationService migrationService;
    
    /**
     * Initialize AWS services.
     * Creates the required DynamoDB tables.
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeAWS() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Initializing AWS services...");
            
            // Initialize DynamoDB tables
            tableManager.initializeTables();
            
            response.put("success", true);
            response.put("message", "AWS services initialized successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("AWS services initialization completed successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("AWS services initialization failed: " + e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Execute data migration.
     * Migrates data from SQLite to DynamoDB.
     */
    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrateData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Starting data migration...");
            
            // Perform data migration
            DataMigrationService.MigrationResult result = migrationService.migrateAllData();
            
            response.put("success", result.success);
            response.put("message", "Data migration completed");
            response.put("result", result);
            response.put("timestamp", System.currentTimeMillis());
            
            if (result.success) {
                logger.info("Data migration completed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warning("Data migration completed with errors: " + result.errorMessage);
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            logger.severe("Data migration failed: " + e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Execute data migration asynchronously.
     * Does not block the request thread.
     */
    @PostMapping("/migrate/async")
    public ResponseEntity<Map<String, Object>> migrateDataAsync() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Starting asynchronous data migration...");
            
            // Run migration asynchronously
            migrationService.migrateAllDataAsync()
                .thenAccept(result -> {
                    if (result.success) {
                        logger.info("Asynchronous data migration completed successfully");
                    } else {
                        logger.warning("Asynchronous data migration completed with errors: " + result.errorMessage);
                    }
                })
                .exceptionally(throwable -> {
                    logger.severe("Asynchronous data migration failed: " + throwable.getMessage());
                    return null;
                });
            
            response.put("success", true);
            response.put("message", "Asynchronous data migration started");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Failed to start asynchronous data migration: " + e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Validate migration results.
     */
    @GetMapping("/migrate/validate")
    public ResponseEntity<Map<String, Object>> validateMigration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Validating migration results...");
            
            // Validate migration outcome
            DataMigrationService.ValidationResult result = migrationService.validateMigration();
            
            response.put("success", true);
            response.put("validation", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Migration validation failed: " + e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get AWS service status.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAWSStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Getting AWS service status...");
            
            // Check DynamoDB table status
            tableManager.describeTable("pet_mood");
            tableManager.describeTable("schedules");
            
            response.put("success", true);
            response.put("status", "AWS services are running");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.warning("AWS service status check failed: " + e.getMessage());
            
            response.put("success", false);
            response.put("status", "AWS services check failed");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "UP");
        response.put("service", "AWS Initialization Controller");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}

