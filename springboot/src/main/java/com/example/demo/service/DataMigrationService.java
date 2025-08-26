package com.example.demo.service;

import com.example.demo.pojo.PetMood;
import com.example.demo.pojo.User;
import com.example.demo.pojo.HealthRecord;
import com.example.demo.pojo.Schedule;
import com.example.demo.pojo.FamilyContact;
import com.example.demo.pojo.ImportantDate;
import com.example.demo.pojo.Memo;
import com.example.demo.pojo.Podcast;
import com.example.demo.pojo.EmotionCompanion;
import com.example.demo.repository.PetMoodRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.HealthRecordRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.FamilyContactRepository;
import com.example.demo.repository.ImportantDateRepository;
import com.example.demo.repository.MemoRepository;
import com.example.demo.repository.PodcastRepository;
import com.example.demo.repository.EmotionCompanionRepository;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.HealthRecordMapper;
import com.example.demo.mapper.ScheduleMapper;
import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.mapper.ImportantDateMapper;
import com.example.demo.mapper.MemoMapper;
import com.example.demo.mapper.PodcastMapper;
import com.example.demo.mapper.EmotionCompanionMapper;
import com.example.demo.mapper.PetMoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.ArrayList;

/**
 * 完整数据迁移服务
 * 负责从本地SQLite迁移所有数据到云端DynamoDB
 * 实现真正的云端平台数据存储
 * 
 * @author Lepeng Zhou
 * @version 2.0
 */
@Service
@Profile("aws") // 只在AWS环境下使用
public class DataMigrationService {
    
    private static final Logger logger = Logger.getLogger(DataMigrationService.class.getName());
    
    // 本地数据源 (SQLite)
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private HealthRecordMapper healthRecordMapper;
    
    @Autowired
    private ScheduleMapper scheduleMapper;
    
    @Autowired
    private FamilyContactMapper familyContactMapper;
    
    @Autowired
    private ImportantDateMapper importantDateMapper;
    
    @Autowired
    private MemoMapper memoMapper;
    
    @Autowired
    private PodcastMapper podcastMapper;
    
    @Autowired
    private EmotionCompanionMapper emotionCompanionMapper;
    
    @Autowired
    private PetMoodMapper petMoodMapper;
    
    // 云端数据源 (DynamoDB)
    @Autowired
    @Qualifier("dynamoDBPetMoodRepository")
    private PetMoodRepository cloudPetMoodRepository;
    
    @Autowired
    @Qualifier("dynamoDBUserRepository")
    private UserRepository cloudUserRepository;
    
    @Autowired
    @Qualifier("dynamoDBHealthRecordRepository")
    private HealthRecordRepository cloudHealthRecordRepository;
    
    @Autowired
    @Qualifier("dynamoDBScheduleRepository")
    private ScheduleRepository cloudScheduleRepository;
    
    @Autowired
    @Qualifier("dynamoDBFamilyContactRepository")
    private FamilyContactRepository cloudFamilyContactRepository;
    
    @Autowired
    @Qualifier("dynamoDBImportantDateRepository")
    private ImportantDateRepository cloudImportantDateRepository;
    
    @Autowired
    @Qualifier("dynamoDBMemoRepository")
    private MemoRepository cloudMemoRepository;
    
    @Autowired
    @Qualifier("dynamoDBPodcastRepository")
    private PodcastRepository cloudPodcastRepository;
    
    @Autowired
    @Qualifier("dynamoDBEmotionCompanionRepository")
    private EmotionCompanionRepository cloudEmotionCompanionRepository;
    
    /**
     * 执行完整的数据迁移
     */
    public MigrationResult migrateAllData() {
        logger.info("Starting complete data migration from SQLite to DynamoDB...");
        
        MigrationResult result = new MigrationResult();
        
        try {
            // 迁移所有核心业务数据
            result.userResult = migrateUserData();
            result.petMoodResult = migratePetMoodData();
            result.scheduleResult = migrateScheduleData();
            result.healthRecordResult = migrateHealthRecordData();
            result.familyContactResult = migrateFamilyContactData();
            result.importantDateResult = migrateImportantDateData();
            result.memoResult = migrateMemoData();
            result.podcastResult = migratePodcastData();
            result.emotionCompanionResult = migrateEmotionCompanionData();
            
            logger.info("Complete data migration finished successfully!");
            logger.info("Migration Summary: " + result.toString());
            
        } catch (Exception e) {
            logger.severe("Complete data migration failed: " + e.getMessage());
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 迁移用户数据
     */
    private MigrationItemResult migrateUserData() {
        MigrationItemResult result = new MigrationItemResult("Users");
        
        try {
            logger.info("Migrating Users data...");
            
            List<User> sourceData = userMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " User records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No user data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (User user : sourceData) {
                try {
                    cloudUserRepository.save(user);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " User records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate User " + user.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("Users migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("Users migration failed: " + e.getMessage());
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
            
            // 从SQLite获取宠物情绪数据
            List<PetMood> sourceData = petMoodMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " PetMood records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No pet mood data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (PetMood petMood : sourceData) {
                try {
                    cloudPetMoodRepository.save(petMood);
                    successCount++;
                    
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
     * 迁移日程数据
     */
    private MigrationItemResult migrateScheduleData() {
        MigrationItemResult result = new MigrationItemResult("Schedules");
        
        try {
            logger.info("Migrating Schedules data...");
            
            List<Schedule> sourceData = scheduleMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " Schedule records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No schedule data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Schedule schedule : sourceData) {
                try {
                    cloudScheduleRepository.save(schedule);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " Schedule records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate Schedule " + schedule.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("Schedules migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("Schedules migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移健康记录数据
     */
    private MigrationItemResult migrateHealthRecordData() {
        MigrationItemResult result = new MigrationItemResult("HealthRecords");
        
        try {
            logger.info("Migrating HealthRecords data...");
            
            // 获取所有健康记录
            List<HealthRecord> sourceData = healthRecordMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " HealthRecord records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No health record data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (HealthRecord record : sourceData) {
                try {
                    cloudHealthRecordRepository.save(record);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " HealthRecord records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate HealthRecord " + record.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("HealthRecords migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("HealthRecords migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移家庭联系人数据
     */
    private MigrationItemResult migrateFamilyContactData() {
        MigrationItemResult result = new MigrationItemResult("FamilyContacts");
        
        try {
            logger.info("Migrating FamilyContacts data...");
            
            List<FamilyContact> sourceData = familyContactMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " FamilyContact records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No family contact data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (FamilyContact contact : sourceData) {
                try {
                    cloudFamilyContactRepository.save(contact);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " FamilyContact records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate FamilyContact " + contact.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("FamilyContacts migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("FamilyContacts migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移重要日期数据
     */
    private MigrationItemResult migrateImportantDateData() {
        MigrationItemResult result = new MigrationItemResult("ImportantDates");
        
        try {
            logger.info("Migrating ImportantDates data...");
            
            List<ImportantDate> sourceData = importantDateMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " ImportantDate records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No important date data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (ImportantDate date : sourceData) {
                try {
                    cloudImportantDateRepository.save(date);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " ImportantDate records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate ImportantDate " + date.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("ImportantDates migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("ImportantDates migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移备忘录数据
     */
    private MigrationItemResult migrateMemoData() {
        MigrationItemResult result = new MigrationItemResult("Memos");
        
        try {
            logger.info("Migrating Memos data...");
            
            List<Memo> sourceData = memoMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " Memo records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No memo data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Memo memo : sourceData) {
                try {
                    cloudMemoRepository.save(memo);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " Memo records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate Memo " + memo.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("Memos migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("Memos migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移播客数据
     */
    private MigrationItemResult migratePodcastData() {
        MigrationItemResult result = new MigrationItemResult("Podcasts");
        
        try {
            logger.info("Migrating Podcasts data...");
            
            List<Podcast> sourceData = podcastMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " Podcast records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No podcast data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (Podcast podcast : sourceData) {
                try {
                    cloudPodcastRepository.save(podcast);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " Podcast records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate Podcast " + podcast.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("Podcasts migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("Podcasts migration failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 迁移情感陪伴数据
     */
    private MigrationItemResult migrateEmotionCompanionData() {
        MigrationItemResult result = new MigrationItemResult("EmotionCompanions");
        
        try {
            logger.info("Migrating EmotionCompanions data...");
            
            List<EmotionCompanion> sourceData = emotionCompanionMapper.findAll();
            result.totalItems = sourceData.size();
            
            logger.info("Found " + sourceData.size() + " EmotionCompanion records to migrate");
            
            if (sourceData.isEmpty()) {
                result.success = true;
                result.message = "No emotion companion data to migrate";
                return result;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            for (EmotionCompanion companion : sourceData) {
                try {
                    cloudEmotionCompanionRepository.save(companion);
                    successCount++;
                    
                    if (successCount % 10 == 0) {
                        logger.info("Migrated " + successCount + "/" + sourceData.size() + " EmotionCompanion records");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.warning("Failed to migrate EmotionCompanion " + companion.getId() + ": " + e.getMessage());
                }
            }
            
            result.successCount = successCount;
            result.failCount = failCount;
            result.success = failCount == 0;
            result.message = String.format("Migrated %d records, %d failed", successCount, failCount);
            
            logger.info("EmotionCompanions migration completed: " + result.message);
            
        } catch (Exception e) {
            result.success = false;
            result.errorMessage = e.getMessage();
            logger.severe("EmotionCompanions migration failed: " + e.getMessage());
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
            long sourceCount = cloudPetMoodRepository.count();
            long targetCount = cloudPetMoodRepository.count(); // Assuming DynamoDB is the target
            
            result.petMoodSourceCount = sourceCount;
            result.petMoodTargetCount = targetCount;
            result.petMoodValid = sourceCount == targetCount;
            
            logger.info("PetMood validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.petMoodValid);
            
            // 验证用户数据
            sourceCount = cloudUserRepository.count();
            targetCount = userMapper.count(); // Assuming SQLite is the source
            result.userSourceCount = sourceCount;
            result.userTargetCount = targetCount;
            result.userValid = sourceCount == targetCount;
            logger.info("User validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.userValid);

            // 验证日程数据
            sourceCount = cloudScheduleRepository.count();
            targetCount = scheduleMapper.count();
            result.scheduleSourceCount = sourceCount;
            result.scheduleTargetCount = targetCount;
            result.scheduleValid = sourceCount == targetCount;
            logger.info("Schedule validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.scheduleValid);

            // 验证健康记录数据
            sourceCount = cloudHealthRecordRepository.count();
            targetCount = healthRecordMapper.count();
            result.healthRecordSourceCount = sourceCount;
            result.healthRecordTargetCount = targetCount;
            result.healthRecordValid = sourceCount == targetCount;
            logger.info("HealthRecord validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.healthRecordValid);

            // 验证家庭联系人数据
            sourceCount = cloudFamilyContactRepository.count();
            targetCount = familyContactMapper.count();
            result.familyContactSourceCount = sourceCount;
            result.familyContactTargetCount = targetCount;
            result.familyContactValid = sourceCount == targetCount;
            logger.info("FamilyContact validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.familyContactValid);

            // 验证重要日期数据
            sourceCount = cloudImportantDateRepository.count();
            targetCount = importantDateMapper.count();
            result.importantDateSourceCount = sourceCount;
            result.importantDateTargetCount = targetCount;
            result.importantDateValid = sourceCount == targetCount;
            logger.info("ImportantDate validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.importantDateValid);

            // 验证备忘录数据
            sourceCount = cloudMemoRepository.count();
            targetCount = memoMapper.count();
            result.memoSourceCount = sourceCount;
            result.memoTargetCount = targetCount;
            result.memoValid = sourceCount == targetCount;
            logger.info("Memo validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.memoValid);

            // 验证播客数据
            sourceCount = cloudPodcastRepository.count();
            targetCount = podcastMapper.count();
            result.podcastSourceCount = sourceCount;
            result.podcastTargetCount = targetCount;
            result.podcastValid = sourceCount == targetCount;
            logger.info("Podcast validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.podcastValid);

            // 验证情感陪伴数据
            sourceCount = cloudEmotionCompanionRepository.count();
            targetCount = emotionCompanionMapper.count();
            result.emotionCompanionSourceCount = sourceCount;
            result.emotionCompanionTargetCount = targetCount;
            result.emotionCompanionValid = sourceCount == targetCount;
            logger.info("EmotionCompanion validation: Source=" + sourceCount + ", Target=" + targetCount + 
                       ", Valid=" + result.emotionCompanionValid);

            // 综合验证
            result.overallValid = result.petMoodValid && result.userValid && result.scheduleValid && 
                                  result.healthRecordValid && result.familyContactValid && 
                                  result.importantDateValid && result.memoValid && 
                                  result.podcastValid && result.emotionCompanionValid;
            
            if (result.overallValid) {
                result.message = "All data types validated successfully";
            } else {
                result.message = "Data count mismatch detected or validation failed for some types";
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
        public MigrationItemResult userResult;
        public MigrationItemResult petMoodResult;
        public MigrationItemResult scheduleResult;
        public MigrationItemResult healthRecordResult;
        public MigrationItemResult familyContactResult;
        public MigrationItemResult importantDateResult;
        public MigrationItemResult memoResult;
        public MigrationItemResult podcastResult;
        public MigrationItemResult emotionCompanionResult;
        
        @Override
        public String toString() {
            return "MigrationResult{" +
                    "success=" + success +
                    ", user=" + (userResult != null ? userResult.toString() : "null") +
                    ", petMood=" + (petMoodResult != null ? petMoodResult.toString() : "null") +
                    ", schedule=" + (scheduleResult != null ? scheduleResult.toString() : "null") +
                    ", healthRecord=" + (healthRecordResult != null ? healthRecordResult.toString() : "null") +
                    ", familyContact=" + (familyContactResult != null ? familyContactResult.toString() : "null") +
                    ", importantDate=" + (importantDateResult != null ? importantDateResult.toString() : "null") +
                    ", memo=" + (memoResult != null ? memoResult.toString() : "null") +
                    ", podcast=" + (podcastResult != null ? podcastResult.toString() : "null") +
                    ", emotionCompanion=" + (emotionCompanionResult != null ? emotionCompanionResult.toString() : "null") +
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
        
        // User验证结果
        public long userSourceCount;
        public long userTargetCount;
        public boolean userValid;

        // Schedule验证结果
        public long scheduleSourceCount;
        public long scheduleTargetCount;
        public boolean scheduleValid;

        // HealthRecord验证结果
        public long healthRecordSourceCount;
        public long healthRecordTargetCount;
        public boolean healthRecordValid;

        // FamilyContact验证结果
        public long familyContactSourceCount;
        public long familyContactTargetCount;
        public boolean familyContactValid;

        // ImportantDate验证结果
        public long importantDateSourceCount;
        public long importantDateTargetCount;
        public boolean importantDateValid;

        // Memo验证结果
        public long memoSourceCount;
        public long memoTargetCount;
        public boolean memoValid;

        // Podcast验证结果
        public long podcastSourceCount;
        public long podcastTargetCount;
        public boolean podcastValid;

        // EmotionCompanion验证结果
        public long emotionCompanionSourceCount;
        public long emotionCompanionTargetCount;
        public boolean emotionCompanionValid;
        
        @Override
        public String toString() {
            return "ValidationResult{" +
                    "overallValid=" + overallValid +
                    ", message='" + message + '\'' +
                    ", petMood=" + petMoodSourceCount + "->" + petMoodTargetCount +
                    ", user=" + userSourceCount + "->" + userTargetCount +
                    ", schedule=" + scheduleSourceCount + "->" + scheduleTargetCount +
                    ", healthRecord=" + healthRecordSourceCount + "->" + healthRecordTargetCount +
                    ", familyContact=" + familyContactSourceCount + "->" + familyContactTargetCount +
                    ", importantDate=" + importantDateSourceCount + "->" + importantDateTargetCount +
                    ", memo=" + memoSourceCount + "->" + memoTargetCount +
                    ", podcast=" + podcastSourceCount + "->" + podcastTargetCount +
                    ", emotionCompanion=" + emotionCompanionSourceCount + "->" + emotionCompanionTargetCount +
                    '}';
        }
    }
}

