package com.example.demo.repository;

import com.example.demo.pojo.Podcast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的播客数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBPodcastRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBPodcastRepository implements PodcastRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "podcasts";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<Podcast> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToPodcast(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find podcast by id: " + id, e);
        }
    }
    
    @Override
    public Podcast save(Podcast podcast) {
        try {
            if (podcast.getId() == null) {
                podcast.setId(String.valueOf(System.currentTimeMillis()));
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(podcast);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return podcast;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save podcast: " + podcast, e);
        }
    }
    
    @Override
    public Podcast update(Podcast podcast) {
        return save(podcast);
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
            throw new RuntimeException("Failed to delete podcast by id: " + id, e);
        }
    }
    
    @Override
    public List<Podcast> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Podcast> podcasts = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                podcasts.add(mapToPodcast(item));
            }
            
            return podcasts;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all podcasts", e);
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
            throw new RuntimeException("Failed to count podcasts", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(Podcast podcast) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(podcast.getId()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private Podcast mapToPodcast(Map<String, AttributeValue> item) {
        Podcast podcast = new Podcast();
        
        if (item.containsKey(ID_ATTR)) {
            podcast.setId(item.get(ID_ATTR).s());
        }
        
        // 添加其他字段映射...
        
        return podcast;
    }
}
