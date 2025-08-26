package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.logging.Logger;

/**
 * DynamoDB表管理服务
 * 负责创建、管理和维护所有DynamoDB表
 * 实现完整的云端数据存储方案
 * 
 * @author Lepeng Zhou
 * @version 2.0
 */
@Service
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBTableManager {
    
    private static final Logger logger = Logger.getLogger(DynamoDBTableManager.class.getName());
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    // 表名配置
    @Value("${aws.dynamodb.table.pet-mood:pet_mood}")
    private String petMoodTableName;
    
    @Value("${aws.dynamodb.table.schedules:schedules}")
    private String schedulesTableName;
    
    @Value("${aws.dynamodb.table.users:users}")
    private String usersTableName;
    
    @Value("${aws.dynamodb.table.health-records:health_records}")
    private String healthRecordsTableName;
    
    @Value("${aws.dynamodb.table.family-contacts:family_contacts}")
    private String familyContactsTableName;
    
    @Value("${aws.dynamodb.table.important-dates:important_dates}")
    private String importantDatesTableName;
    
    @Value("${aws.dynamodb.table.memos:memos}")
    private String memosTableName;
    
    @Value("${aws.dynamodb.table.podcasts:podcasts}")
    private String podcastsTableName;
    
    @Value("${aws.dynamodb.table.emotion-companions:emotion_companions}")
    private String emotionCompanionsTableName;
    
    @Value("${aws.dynamodb.table.chat-messages:chat_messages}")
    private String chatMessagesTableName;
    
    /**
     * 初始化所有必需的DynamoDB表
     */
    public void initializeTables() {
        logger.info("Starting complete DynamoDB table initialization...");
        
        try {
            // 核心业务表
            createUsersTable();
            createPetMoodTable();
            createSchedulesTable();
            createHealthRecordsTable();
            createFamilyContactsTable();
            createImportantDatesTable();
            createMemosTable();
            createPodcastsTable();
            createEmotionCompanionsTable();
            createChatMessagesTable();
            
            logger.info("All DynamoDB tables initialized successfully!");
            
        } catch (Exception e) {
            logger.severe("Failed to initialize DynamoDB tables: " + e.getMessage());
            throw new RuntimeException("Table initialization failed", e);
        }
    }
    
    /**
     * 创建用户表
     */
    private void createUsersTable() {
        try {
            if (tableExists(usersTableName)) {
                logger.info("Table " + usersTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + usersTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(usersTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("email")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("email-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("email")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(usersTableName);
            logger.info("Users table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create users table: " + e.getMessage());
            throw new RuntimeException("Users table creation failed", e);
        }
    }
    
    /**
     * 创建宠物情绪表
     */
    private void createPetMoodTable() {
        try {
            if (tableExists(petMoodTableName)) {
                logger.info("Table " + petMoodTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + petMoodTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(petMoodTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("userId")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(petMoodTableName);
            logger.info("PetMood table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create PetMood table: " + e.getMessage());
            throw new RuntimeException("PetMood table creation failed", e);
        }
    }
    
    /**
     * 创建日程表
     */
    private void createSchedulesTable() {
        try {
            if (tableExists(schedulesTableName)) {
                logger.info("Table " + schedulesTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + schedulesTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(schedulesTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("scheduleDate")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-date-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("scheduleDate")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(schedulesTableName);
            logger.info("Schedules table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create schedules table: " + e.getMessage());
            throw new RuntimeException("Schedules table creation failed", e);
        }
    }
    
    /**
     * 创建健康记录表
     */
    private void createHealthRecordsTable() {
        try {
            if (tableExists(healthRecordsTableName)) {
                logger.info("Table " + healthRecordsTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + healthRecordsTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(healthRecordsTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("recordTime")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("type")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-time-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("recordTime")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build(),
                    GlobalSecondaryIndex.builder()
                        .indexName("type-time-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("type")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("recordTime")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(healthRecordsTableName);
            logger.info("HealthRecords table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create health records table: " + e.getMessage());
            throw new RuntimeException("Health records table creation failed", e);
        }
    }
    
    /**
     * 创建家庭联系人表
     */
    private void createFamilyContactsTable() {
        try {
            if (tableExists(familyContactsTableName)) {
                logger.info("Table " + familyContactsTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + familyContactsTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(familyContactsTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("isEmergencyContact")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build(),
                    GlobalSecondaryIndex.builder()
                        .indexName("emergency-contacts-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("isEmergencyContact")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(familyContactsTableName);
            logger.info("FamilyContacts table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create family contacts table: " + e.getMessage());
            throw new RuntimeException("Family contacts table creation failed", e);
        }
    }
    
    /**
     * 创建重要日期表
     */
    private void createImportantDatesTable() {
        try {
            if (tableExists(importantDatesTableName)) {
                logger.info("Table " + importantDatesTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + importantDatesTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(importantDatesTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("date")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-date-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("date")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(importantDatesTableName);
            logger.info("ImportantDates table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create important dates table: " + e.getMessage());
            throw new RuntimeException("Important dates table creation failed", e);
        }
    }
    
    /**
     * 创建备忘录表
     */
    private void createMemosTable() {
        try {
            if (tableExists(memosTableName)) {
                logger.info("Table " + memosTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + memosTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(memosTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("createdAt")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-time-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("createdAt")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(memosTableName);
            logger.info("Memos table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create memos table: " + e.getMessage());
            throw new RuntimeException("Memos table creation failed", e);
        }
    }
    
    /**
     * 创建播客表
     */
    private void createPodcastsTable() {
        try {
            if (tableExists(podcastsTableName)) {
                logger.info("Table " + podcastsTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + podcastsTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(podcastsTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("language")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("language-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("language")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(podcastsTableName);
            logger.info("Podcasts table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create podcasts table: " + e.getMessage());
            throw new RuntimeException("Podcasts table creation failed", e);
        }
    }
    
    /**
     * 创建情感陪伴表
     */
    private void createEmotionCompanionsTable() {
        try {
            if (tableExists(emotionCompanionsTableName)) {
                logger.info("Table " + emotionCompanionsTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + emotionCompanionsTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(emotionCompanionsTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(emotionCompanionsTableName);
            logger.info("EmotionCompanions table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create emotion companions table: " + e.getMessage());
            throw new RuntimeException("Emotion companions table creation failed", e);
        }
    }
    
    /**
     * 创建聊天消息表
     */
    private void createChatMessagesTable() {
        try {
            if (tableExists(chatMessagesTableName)) {
                logger.info("Table " + chatMessagesTableName + " already exists");
                return;
            }
            
            logger.info("Creating table: " + chatMessagesTableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                .tableName(chatMessagesTableName)
                .attributeDefinitions(
                    AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("userId")
                        .attributeType(ScalarAttributeType.S)
                        .build(),
                    AttributeDefinition.builder()
                        .attributeName("timestamp")
                        .attributeType(ScalarAttributeType.S)
                        .build()
                )
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build()
                )
                .globalSecondaryIndexes(
                    GlobalSecondaryIndex.builder()
                        .indexName("userId-time-index")
                        .keySchema(
                            KeySchemaElement.builder()
                                .attributeName("userId")
                                .keyType(KeyType.HASH)
                                .build(),
                            KeySchemaElement.builder()
                                .attributeName("timestamp")
                                .keyType(KeyType.RANGE)
                                .build()
                        )
                        .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                        .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();
            
            dynamoDbClient.createTable(request);
            waitForTableToBecomeActive(chatMessagesTableName);
            logger.info("ChatMessages table created successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to create chat messages table: " + e.getMessage());
            throw new RuntimeException("Chat messages table creation failed", e);
        }
    }
    
    /**
     * 检查表是否存在
     */
    private boolean tableExists(String tableName) {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();
            
            dynamoDbClient.describeTable(request);
            return true;
            
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (Exception e) {
            logger.warning("Error checking table existence for " + tableName + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 等待表变为活跃状态
     */
    private void waitForTableToBecomeActive(String tableName) {
        try {
            logger.info("Waiting for table " + tableName + " to become active...");
            
            // 简单等待，生产环境建议使用waiter
            Thread.sleep(5000);
            logger.info("Table " + tableName + " creation initiated");
            
        } catch (Exception e) {
            logger.warning("Error waiting for table to become active: " + e.getMessage());
        }
    }
    
    /**
     * 删除表（谨慎使用）
     */
    public void deleteTable(String tableName) {
        try {
            logger.warning("Deleting table: " + tableName);
            
            DeleteTableRequest request = DeleteTableRequest.builder()
                .tableName(tableName)
                .build();
            
            dynamoDbClient.deleteTable(request);
            
            logger.info("Table " + tableName + " deleted successfully");
            
        } catch (Exception e) {
            logger.severe("Failed to delete table " + tableName + ": " + e.getMessage());
            throw new RuntimeException("Table deletion failed", e);
        }
    }
    
    /**
     * 获取表信息
     */
    public void describeTable(String tableName) {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(tableName)
                .build();
            
            DescribeTableResponse response = dynamoDbClient.describeTable(request);
            TableDescription table = response.table();
            
            logger.info("Table: " + table.tableName());
            logger.info("Status: " + table.tableStatus());
            logger.info("Item Count: " + table.itemCount());
            logger.info("Table Size: " + table.tableSizeBytes() + " bytes");
            logger.info("ARN: " + table.tableArn());
            
        } catch (Exception e) {
            logger.severe("Failed to describe table " + tableName + ": " + e.getMessage());
        }
    }
}
