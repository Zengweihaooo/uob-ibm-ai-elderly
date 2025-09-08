package com.example.demo.service;

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
 * DynamoDB User Service
 * Handles user data storage in DynamoDB
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws")
public class DynamoDBUserService {
    
    private static final Logger logger = Logger.getLogger(DynamoDBUserService.class.getName());
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "users";
    
    /**
     * Save user to DynamoDB
     */
    public void saveUser(String email, String name, String role) {
        try {
            String id = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(id).build());
            item.put("email", AttributeValue.builder().s(email).build());
            item.put("name", AttributeValue.builder().s(name != null ? name : "").build());
            item.put("role", AttributeValue.builder().s(role != null ? role : "USER").build());
            item.put("status", AttributeValue.builder().s("ACTIVE").build());
            item.put("createdAt", AttributeValue.builder().s(timestamp).build());
            item.put("updatedAt", AttributeValue.builder().s(timestamp).build());
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            logger.info("Successfully saved user to DynamoDB: " + email);
            
        } catch (Exception e) {
            logger.severe("Failed to save user to DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }
    
    /**
     * Get all users from DynamoDB
     */
    public List<Map<String, AttributeValue>> getAllUsers() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            logger.info("Retrieved " + response.items().size() + " users from DynamoDB");
            
            return response.items();
            
        } catch (Exception e) {
            logger.severe("Failed to get users from DynamoDB: " + e.getMessage());
            throw new RuntimeException("Failed to get users", e);
        }
    }
    
    /**
     * Get user count
     */
    public int getUserCount() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .select("COUNT")
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            return response.count();
            
        } catch (Exception e) {
            logger.severe("Failed to get user count: " + e.getMessage());
            return 0;
        }
    }
}
