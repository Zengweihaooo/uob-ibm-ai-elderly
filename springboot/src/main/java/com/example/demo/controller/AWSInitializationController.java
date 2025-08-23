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
 * AWS服务初始化控制器
 * 提供API端点来管理AWS服务的初始化、数据迁移等
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
@RequestMapping("/api/aws")
@Profile("aws") // 只在AWS环境下使用
public class AWSInitializationController {
    
    private static final Logger logger = Logger.getLogger(AWSInitializationController.class.getName());
    
    @Autowired
    private DynamoDBTableManager tableManager;
    
    @Autowired
    private DataMigrationService migrationService;
    
    /**
     * 初始化AWS服务
     * 创建必要的DynamoDB表
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeAWS() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Initializing AWS services...");
            
            // 初始化DynamoDB表
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
     * 执行数据迁移
     * 从SQLite迁移数据到DynamoDB
     */
    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrateData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Starting data migration...");
            
            // 执行数据迁移
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
     * 异步执行数据迁移
     * 不阻塞请求线程
     */
    @PostMapping("/migrate/async")
    public ResponseEntity<Map<String, Object>> migrateDataAsync() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Starting asynchronous data migration...");
            
            // 异步执行数据迁移
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
     * 验证迁移结果
     */
    @GetMapping("/migrate/validate")
    public ResponseEntity<Map<String, Object>> validateMigration() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Validating migration results...");
            
            // 验证迁移结果
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
     * 获取AWS服务状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAWSStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Getting AWS service status...");
            
            // 检查DynamoDB表状态
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
     * 健康检查端点
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

