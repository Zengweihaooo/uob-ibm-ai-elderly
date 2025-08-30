package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

/**
 * Database Management Service
 * 数据库管理服务
 * 
 * 提供数据库备份、恢复、状态检查等功能
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
     * 获取数据库状态信息
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
     * 创建数据库备份
     * Create database backup
     */
    public String createBackup() throws IOException {
        File dbFile = new File(databasePath);
        if (!dbFile.exists()) {
            throw new IOException("Database file does not exist: " + databasePath);
        }

        // 确保备份目录存在
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create backup directory: " + backupPath);
            }
        }

        // 生成备份文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFileName = "elderly_companion_backup_" + timestamp + ".db";
        Path backupFilePath = Paths.get(backupPath, backupFileName);

        // 复制数据库文件
        Files.copy(dbFile.toPath(), backupFilePath, StandardCopyOption.REPLACE_EXISTING);

        return backupFilePath.toString();
    }

    /**
     * 从备份恢复数据库
     * Restore database from backup
     */
    public boolean restoreFromBackup(String backupFileName) throws IOException {
        Path backupFilePath = Paths.get(backupPath, backupFileName);
        
        if (!Files.exists(backupFilePath)) {
            throw new IOException("Backup file does not exist: " + backupFilePath);
        }

        Path dbFilePath = Paths.get(databasePath);
        
        // 创建当前数据库的备份(如果存在)
        if (Files.exists(dbFilePath)) {
            String currentBackupName = "before_restore_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
            Path currentBackupPath = Paths.get(backupPath, currentBackupName);
            Files.copy(dbFilePath, currentBackupPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // 从备份恢复
        Files.copy(backupFilePath, dbFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        return true;
    }

    /**
     * 验证数据库完整性
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

            // 基本文件检查通过
            result.put("valid", true);
            result.put("file_size", dbFile.length());
            result.put("last_modified", dbFile.lastModified());
            
            // TODO: 可以添加更多的数据库内容验证
            // 例如：检查关键表是否存在，数据是否一致等
            
        } catch (Exception e) {
            result.put("valid", false);
            result.put("error", "Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 清理旧的备份文件
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

        // 按修改时间排序，保留最新的文件
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
     * 获取数据库文件路径
     * Get database file path
     */
    public String getDatabasePath() {
        return new File(databasePath).getAbsolutePath();
    }

    /**
     * 获取备份目录路径
     * Get backup directory path
     */
    public String getBackupPath() {
        return new File(backupPath).getAbsolutePath();
    }
}
