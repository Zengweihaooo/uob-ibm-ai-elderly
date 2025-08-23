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
 * 负责创建、管理和维护DynamoDB表
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBTableManager {
    
    private static final Logger logger = Logger.getLogger(DynamoDBTableManager.class.getName());
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    @Value("${aws.dynamodb.table.pet-mood:pet_mood}")
    private String petMoodTableName;
    
    @Value("${aws.dynamodb.table.schedules:schedules}")
    private String schedulesTableName;
    
    /**
     * 初始化所有必需的DynamoDB表
     */
    public void initializeTables() {
        logger.info("Starting DynamoDB table initialization...");
        
        try {
            // 创建宠物情绪表
            createPetMoodTable();
            
            // 创建日程表
            createSchedulesTable();
            
            logger.info("All DynamoDB tables initialized successfully!");
            
        } catch (Exception e) {
            logger.severe("Failed to initialize DynamoDB tables: " + e.getMessage());
            throw new RuntimeException("Table initialization failed", e);
        }
    }
    
    /**
     * 创建宠物情绪表
     */
    private void createPetMoodTable() {
        try {
            // 检查表是否已存在
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
            
            CreateTableResponse response = dynamoDbClient.createTable(request);
            
            // 等待表创建完成
            waitForTableToBecomeActive(petMoodTableName);
            
            logger.info("Table " + petMoodTableName + " created successfully. ARN: " + response.tableDescription().tableArn());
            
        } catch (Exception e) {
            logger.severe("Failed to create table " + petMoodTableName + ": " + e.getMessage());
            throw new RuntimeException("Table creation failed", e);
        }
    }
    
    /**
     * 创建日程表
     */
    private void createSchedulesTable() {
        try {
            // 检查表是否已存在
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
            
            CreateTableResponse response = dynamoDbClient.createTable(request);
            
            // 等待表创建完成
            waitForTableToBecomeActive(schedulesTableName);
            
            logger.info("Table " + schedulesTableName + " created successfully. ARN: " + response.tableDescription().tableArn());
            
        } catch (Exception e) {
            logger.severe("Failed to create table " + schedulesTableName + ": " + e.getMessage());
            throw new RuntimeException("Table creation failed", e);
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
