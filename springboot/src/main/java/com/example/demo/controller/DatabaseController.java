package com.example.demo.controller;

import com.example.demo.service.DatabaseManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Database Management Controller
 * Provides API endpoints for database status check, backup, restore, etc.
 *
 * Author: Weihao Zeng
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private DatabaseManagementService databaseManagementService;

    /**
     * Get database status.
     * @return Database status information
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        try {
            Map<String, Object> status = databaseManagementService.getDatabaseStatus();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", status);
            response.put("message", "Database status retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get database status: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Create database backup.
     * @return Backup creation result
     */
    @PostMapping("/backup")
    public ResponseEntity<Map<String, Object>> createBackup() {
        try {
            String backupPath = databaseManagementService.createBackup();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("backup_path", backupPath);
            response.put("message", "Database backup created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create backup: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Restore database from backup.
     * @param requestBody Request containing backup filename
     * @return Restore operation result
     */
    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreFromBackup(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String backupFileName = (String) requestBody.get("backup_filename");
            
            if (backupFileName == null || backupFileName.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Backup filename is required");
                
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean restored = databaseManagementService.restoreFromBackup(backupFileName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", restored);
            response.put("message", restored ? 
                "Database restored successfully from backup" : 
                "Failed to restore database from backup");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to restore from backup: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Validate database integrity.
     * @return Database integrity validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateDatabase() {
        try {
            Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("validation", validation);
            response.put("message", "Database validation completed");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to validate database: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Clean up old backup files.
     * @param requestBody Request containing keep count
     * @return Cleanup operation result
     */
    @PostMapping("/cleanup-backups")
    public ResponseEntity<Map<String, Object>> cleanupBackups(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            Integer keepCount = (Integer) requestBody.getOrDefault("keep_count", 5);
            
            int deletedCount = databaseManagementService.cleanupOldBackups(keepCount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("deleted_count", deletedCount);
            response.put("message", String.format("Cleaned up %d old backup files", deletedCount));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to cleanup backups: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get basic database information.
     * @return Database basic information
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("database_path", databaseManagementService.getDatabasePath());
            info.put("backup_path", databaseManagementService.getBackupPath());
            info.put("database_type", "SQLite");
            info.put("version", "1.0.0");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("info", info);
            response.put("message", "Database information retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to get database info: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
