package com.example.demo.service;

import com.example.demo.pojo.PetMood;
import com.example.demo.repository.PetMoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 数据迁移服务
 * 负责从本地SQLite迁移数据到云端DynamoDB
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws") // 只在AWS环境下使用
public class DataMigrationService {
    
    private static final Logger logger = Logger.getLogger(DataMigrationService.class.getName());
    
    @Autowired
    @Qualifier("sqlitePetMoodRepository")
    private PetMoodRepository sourceRepository;
    
    @Autowired
    @Qualifier("dynamoDBPetMoodRepository")
    private PetMoodRepository targetRepository;
    
    /**
     * 执行完整的数据迁移
     */
    public MigrationResult migrateAllData() {
        logger.info("Starting complete data migration from SQLite to DynamoDB...");
        
        MigrationResult result = new MigrationResult();
        
        try {
            // 迁移宠物情绪数据
            result.petMoodResult = migratePetMoodData();
            
            // 迁移其他数据（如日程、用户等）
            // result.scheduleResult = migrateScheduleData();
            // result.userResult = migrateUserData();
            
            logger.info("Data migration completed successfully!");
            logger.info("PetMood: " + result.petMoodResult.toString());
            
        } catch (Exception e) {
            logger.severe("Data migration failed: " + e.getMessage());
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 迁移宠物情绪数据
     */
    private MigrationItemResult migratePetMoodData() {
        MigrationItemResult result = new MigrationItemResult("PetMood");
        
        try {
            logger.info("Migrating PetMood data...");
            
            // 从源数据库获取所有数据
            List<PetMood> sourceData = sourceRepository.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " PetMood records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No data to migrate";
                return result;
            }
            
            // 批量迁移数据
            int successCount = 0;
            int failCount = 0;
            
            for (PetMood petMood : sourceData) {
                try {
                    // 迁移到目标数据库
                    targetRepository.save(petMood);
                    successCount++;
                    
                    // 记录进度
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " PetMood records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate PetMood for user " + petMood.getUserId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("PetMood migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("PetMood migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 异步迁移数据（不阻塞主线程）
     */
    public CompletableFuture<MigrationResult> migrateAllDataAsync() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Starting asynchronous data migration...");
            return migrateAllData();
        });
    }
    
    /**
     * 验证迁移结果
     */
    public ValidationResult validateMigration() {
        logger.info("Validating migration results...");
        
        ValidationResult result = new ValidationResult();
        
        try {
            // 验证宠物情绪数据
            long sourceCount = sourceRepository.count();
            long targetCount = targetRepository.count();
            
            result.petMoodSourceCount = sourceCount;
            result.petMoodTargetCount = targetCount;
            result.petMoodValid = sourceCount == targetCount;
            
            logger.info("PetMood validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.petMoodValid);
            
            // 验证数据完整性
            if (result.petMoodValid) {
                result.overallValid = true;
                result.message = "Migration validation successful";
            } else {
                result.overallValid = false;
                result.message = "Data count mismatch detected";
            }
            
        } catch (Exception e) {
            result.overallValid = false;
            result.errorMessage = e.getMessage();
            logger.severe("Migration validation failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移结果类
     */
    public static class MigrationResult {
        public boolean success = true;
        public String errorMessage;
        public MigrationItemResult petMoodResult;
        public MigrationItemResult scheduleResult;
        public MigrationItemResult userResult;
        
        @Override
        public String toString() {
            return "MigrationResult{" +
                    "success=" + success +
                    ", petMood=" + (petMoodResult != null ? petMoodResult.toString() : "null") +
                    ", error=" + errorMessage +
                    '}';
        }
    }
    
    /**
     * 单项迁移结果类
     */
    public static class MigrationItemResult {
        public String dataType;
        public boolean success;
        public int totalItems;
        public int successCount;
        public int failCount;
        public String message;
        public String errorMessage;
        
        public MigrationItemResult(String dataType) {
            this.dataType = dataType;
        }
        
        @Override
        public String toString() {
            return dataType + "{" +
                    "success=" + success +
                    ", total=" + totalItems +
                    ", success=" + successCount +
                    ", failed=" + failCount +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
    
    /**
     * 验证结果类
     */
    public static class ValidationResult {
        public boolean overallValid;
        public String message;
        public String errorMessage;
        
        // PetMood验证结果
        public long petMoodSourceCount;
        public long petMoodTargetCount;
        public boolean petMoodValid;
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "overallValid=" + overallValid +
                    ", message='" + message + '\'' +
                    ", petMood=" + petMoodSourceCount + "->" + petMoodTargetCount +
                    '}';
        }
    }
}

