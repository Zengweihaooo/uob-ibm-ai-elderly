package com.example.demo.repository;

import com.example.demo.pojo.PetMood;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的宠物情绪数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBPetMoodRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBPetMoodRepository implements PetMoodRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "pet_mood";
    private static final String USER_ID_ATTR = "userId";
    
    @Override
    public Optional<PetMood> findByUserId(Long userId) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(USER_ID_ATTR, AttributeValue.builder().s(userId.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToPetMood(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find pet mood by userId: " + userId, e);
        }
    }
    
    @Override
    public PetMood save(PetMood petMood) {
        try {
            Map<String, AttributeValue> item = mapToDynamoDBItem(petMood);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return petMood;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save pet mood: " + petMood, e);
        }
    }
    
    @Override
    public PetMood update(PetMood petMood) {
        return save(petMood); // DynamoDB的put操作可以覆盖现有记录
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        try {
            DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(USER_ID_ATTR, AttributeValue.builder().s(userId.toString()).build()))
                .build();
            
            dynamoDbClient.deleteItem(request);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete pet mood by userId: " + userId, e);
        }
    }
    
    @Override
    public boolean existsByUserId(Long userId) {
        return findByUserId(userId).isPresent();
    }
    
    @Override
    public List<PetMood> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            return response.items().stream()
                .map(this::mapToPetMood)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all pet moods", e);
        }
    }
    
    @Override
    public List<PetMood> findByMoodScoreBetween(int minScore, int maxScore) {
        // 简化实现，使用scan + 过滤
        return findAll().stream()
            .filter(pm -> pm.getMoodScore() >= minScore && pm.getMoodScore() <= maxScore)
            .collect(java.util.stream.Collectors.toList());
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
            throw new RuntimeException("Failed to count pet moods", e);
        }
    }
    
    /**
     * 将PetMood对象转换为DynamoDB属性映射
     */
    private Map<String, AttributeValue> mapToDynamoDBItem(PetMood petMood) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(USER_ID_ATTR, AttributeValue.builder().s(petMood.getUserId().toString()).build());
        item.put("moodScore", AttributeValue.builder().n(petMood.getMoodScore().toString()).build());
        item.put("happiness", AttributeValue.builder().n(petMood.getHappiness().toString()).build());
        item.put("health", AttributeValue.builder().n(petMood.getHealth().toString()).build());
        item.put("energy", AttributeValue.builder().n(petMood.getEnergy().toString()).build());
        item.put("moodEmoji", AttributeValue.builder().s(petMood.getMoodEmoji()).build());
        item.put("status", AttributeValue.builder().s(petMood.getStatus()).build());
        item.put("level", AttributeValue.builder().n(petMood.getLevel().toString()).build());
        item.put("experience", AttributeValue.builder().n(petMood.getExperience().toString()).build());
        
        return item;
    }
    
    /**
     * 将DynamoDB属性映射转换为PetMood对象
     */
    private PetMood mapToPetMood(Map<String, AttributeValue> item) {
        PetMood petMood = new PetMood();
        
        petMood.setUserId(Long.parseLong(item.get(USER_ID_ATTR).s()));
        petMood.setMoodScore(Integer.parseInt(item.get("moodScore").n()));
        petMood.setHappiness(Integer.parseInt(item.get("happiness").n()));
        petMood.setHealth(Integer.parseInt(item.get("health").n()));
        petMood.setEnergy(Integer.parseInt(item.get("energy").n()));
        petMood.setMoodEmoji(item.get("moodEmoji").s());
        petMood.setStatus(item.get("status").s());
        petMood.setLevel(Integer.parseInt(item.get("level").n()));
        petMood.setExperience(Integer.parseInt(item.get("experience").n()));
        
        return petMood;
    }
}

