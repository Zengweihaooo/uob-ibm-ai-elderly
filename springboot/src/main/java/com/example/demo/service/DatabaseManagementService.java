package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Database Management Service
 * Provides database backup, restore, status check and other functions
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Service
public class DatabaseManagementService {

    @Value("${app.database.path:data/elderly_companion.db}")
    private String databasePath;

    @Value("${app.database.backup.path:data/backups/}")
    private String backupPath;

    /**
     * Get database status information
     */
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        File dbFile = new File(databasePath);
        
        status.put("database_exists", dbFile.exists());
        status.put("database_path", dbFile.getAbsolutePath());
        
        if (dbFile.exists()) {
            status.put("database_size", dbFile.length());
            status.put("last_modified", LocalDateTime.ofEpochSecond(
                dbFile.lastModified() / 1000, 0, 
                java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now())
            ));
            status.put("readable", dbFile.canRead());
            status.put("writable", dbFile.canWrite());
        }
        
        File backupDir = new File(backupPath);
        status.put("backup_directory_exists", backupDir.exists());
        status.put("backup_path", backupDir.getAbsolutePath());
        
        if (backupDir.exists()) {
            File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".db"));
            status.put("backup_count", backupFiles != null ? backupFiles.length : 0);
        }
        
        return status;
    }

    /**
     * Create database backup
     */
    public String createBackup() throws IOException {
        File dbFile = new File(databasePath);
        if (!dbFile.exists()) {
            throw new IOException("Database file does not exist: " + databasePath);
        }

        // Ensure the backup directory exists
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create backup directory: " + backupPath);
            }
        }

        // Generate backup file name
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "elderly_companion_backup_" + timestamp + ".db";
        Path backupFilePath = Paths.get(backupPath, backupFileName);

        // Copy database file to backup location
        Files.copy(dbFile.toPath(), backupFilePath, StandardCopyOption.REPLACE_EXISTING);

        return backupFilePath.toString();
    }

    /**
     * Restore database from backup
     */
    public boolean restoreFromBackup(String backupFileName) throws IOException {
        Path backupFilePath = Paths.get(backupPath, backupFileName);
        
        if (!Files.exists(backupFilePath)) {
            throw new IOException("Backup file does not exist: " + backupFilePath);
        }

        Path dbFilePath = Paths.get(databasePath);
        
        // Create a backup of the current database (if it exists)
        if (Files.exists(dbFilePath)) {
            String currentBackupName = "before_restore_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
            Path currentBackupPath = Paths.get(backupPath, currentBackupName);
            Files.copy(dbFilePath, currentBackupPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // Restore from the specified backup
        Files.copy(backupFilePath, dbFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        return true;
    }

    /**
     * Validate database integrity
     */
    public Map<String, Object> validateDatabaseIntegrity() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File dbFile = new File(databasePath);
            
            if (!dbFile.exists()) {
                result.put("valid", false);
                result.put("error", "Database file does not exist");
                return result;
            }

            if (dbFile.length() == 0) {
                result.put("valid", false);
                result.put("error", "Database file is empty");
                return result;
            }

            // Basic file checks passed
            result.put("valid", true);
            result.put("file_size", dbFile.length());
            result.put("last_modified", dbFile.lastModified());
            
            // TODO: Add more database content validations if needed
            // For example: check whether key tables exist and data consistency
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Clean up old backup files
     */
    public int cleanupOldBackups(int keepCount) {
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            return 0;
        }

        File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".db"));
        if (backupFiles == null || backupFiles.length <= keepCount) {
            return 0;
        }

        // Sort by modification time and keep the newest files
        java.util.Arrays.sort(backupFiles, (a, b) -> 
            Long.compare(b.lastModified(), a.lastModified()));

        int deletedCount = 0;
        for (int i = keepCount; i < backupFiles.length; i++) {
            if (backupFiles[i].delete()) {
                deletedCount++;
            }
        }

        return deletedCount;
    }

    /**
     * Get database file path
     */
    public String getDatabasePath() {
        return new File(databasePath).getAbsolutePath();
    }

    /**
     * Get backup directory path
     */
    public String getBackupPath() {
        return new File(backupPath).getAbsolutePath();
    }
}
