package com.example.demo.repository;

import com.example.demo.pojo.EmotionCompanion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的情感伴侣数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBEmotionCompanionRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBEmotionCompanionRepository implements EmotionCompanionRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "emotion_companions";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<EmotionCompanion> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToEmotionCompanion(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find emotion companion by id: " + id, e);
        }
    }
    
    @Override
    public EmotionCompanion save(EmotionCompanion emotionCompanion) {
        try {
            if (emotionCompanion.getId() == null) {
                emotionCompanion.setId(System.currentTimeMillis());
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(emotionCompanion);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return emotionCompanion;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save emotion companion: " + emotionCompanion, e);
        }
    }
    
    @Override
    public EmotionCompanion update(EmotionCompanion emotionCompanion) {
        return save(emotionCompanion);
    }
    
    @Override
    public void deleteById(Long id) {
        try {
            DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            dynamoDbClient.deleteItem(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete emotion companion by id: " + id, e);
        }
    }
    
    @Override
    public List<EmotionCompanion> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<EmotionCompanion> companions = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                companions.add(mapToEmotionCompanion(item));
            }
            
            return companions;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all emotion companions", e);
        }
    }
    
    @Override
    public long count() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .select(Select.COUNT)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            return response.count();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to count emotion companions", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(EmotionCompanion companion) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(companion.getId().toString()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private EmotionCompanion mapToEmotionCompanion(Map<String, AttributeValue> item) {
        EmotionCompanion companion = new EmotionCompanion();
        
        if (item.containsKey(ID_ATTR)) {
            companion.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        // 添加其他字段映射...
        
        return companion;
    }
}
