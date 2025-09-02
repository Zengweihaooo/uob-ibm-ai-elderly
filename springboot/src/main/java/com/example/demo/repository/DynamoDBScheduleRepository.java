package com.example.demo.repository;

import com.example.demo.pojo.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的日程数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBScheduleRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBScheduleRepository implements ScheduleRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "schedules";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<Schedule> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToSchedule(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find schedule by id: " + id, e);
        }
    }
    
    @Override
    public Schedule save(Schedule schedule) {
        try {
            if (schedule.getId() == null) {
                schedule.setId(System.currentTimeMillis());
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(schedule);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return schedule;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save schedule: " + schedule, e);
        }
    }
    
    @Override
    public Schedule update(Schedule schedule) {
        return save(schedule);
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
            throw new RuntimeException("Failed to delete schedule by id: " + id, e);
        }
    }
    
    @Override
    public List<Schedule> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Schedule> schedules = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                schedules.add(mapToSchedule(item));
            }
            
            return schedules;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all schedules", e);
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
            throw new RuntimeException("Failed to count schedules", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(Schedule schedule) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(schedule.getId().toString()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private Schedule mapToSchedule(Map<String, AttributeValue> item) {
        Schedule schedule = new Schedule();
        
        if (item.containsKey(ID_ATTR)) {
            schedule.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        // 添加其他字段映射...
        
        return schedule;
    }
}
