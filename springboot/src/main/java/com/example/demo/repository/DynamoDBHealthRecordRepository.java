package com.example.demo.repository;

import com.example.demo.pojo.HealthRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的健康记录数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBHealthRecordRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBHealthRecordRepository implements HealthRecordRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "health_records";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<HealthRecord> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToHealthRecord(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find health record by id: " + id, e);
        }
    }
    
    @Override
    public HealthRecord save(HealthRecord healthRecord) {
        try {
            if (healthRecord.getId() == null) {
                healthRecord.setId(System.currentTimeMillis());
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(healthRecord);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return healthRecord;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save health record: " + healthRecord, e);
        }
    }
    
    @Override
    public HealthRecord update(HealthRecord healthRecord) {
        return save(healthRecord);
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
            throw new RuntimeException("Failed to delete health record by id: " + id, e);
        }
    }
    
    @Override
    public List<HealthRecord> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<HealthRecord> records = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                records.add(mapToHealthRecord(item));
            }
            
            return records;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all health records", e);
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
            throw new RuntimeException("Failed to count health records", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(HealthRecord record) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(record.getId().toString()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private HealthRecord mapToHealthRecord(Map<String, AttributeValue> item) {
        HealthRecord record = new HealthRecord();
        
        if (item.containsKey(ID_ATTR)) {
            record.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        // 添加其他字段映射...
        
        return record;
    }
}
