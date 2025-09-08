package com.example.demo.service;

import com.example.demo.pojo.HealthRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * DynamoDB Health Record Service
 * Handles health record data storage in DynamoDB
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws")
public class DynamoDBHealthRecordService {
    
    private static final Logger logger = Logger.getLogger(DynamoDBHealthRecordService.class.getName());
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "health_records";
    
    /**
     * Save health record to DynamoDB
     */
    public void saveHealthRecord(String type, String value, String notes, boolean isAbnormal) {
        try {
            String id = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(id).build());
            item.put("type", AttributeValue.builder().s(type).build());
            item.put("value", AttributeValue.builder().s(value).build());
            item.put("notes", AttributeValue.builder().s(notes != null ? notes : "").build());
            item.put("isAbnormal", AttributeValue.builder().bool(isAbnormal).build());
            item.put("timestamp", AttributeValue.builder().s(timestamp).build());
            item.put("createdAt", AttributeValue.builder().s(timestamp).build());
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            logger.info("Successfully saved health record to DynamoDB: " + type);
            
        } catch (Exception e) {
            logger.severe("Failed to save health record to DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to save health record", e);
        }
    }
    
    /**
     * Get all health records from DynamoDB
     */
    public List<Map<String, AttributeValue>> getAllHealthRecords() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            logger.info("Retrieved " + response.items().size() + " health records from DynamoDB");
            
            return response.items();
            
        } catch (Exception e) {
            logger.severe("Failed to get health records from DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to get health records", e);
        }
    }
    
    /**
     * Get health record count
     */
    public int getHealthRecordCount() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .select("COUNT")
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            return response.count();
            
        } catch (Exception e) {
            logger.severe("Failed to get health record count: " + e.getMessage());
            return 0;
        }
    }
}
