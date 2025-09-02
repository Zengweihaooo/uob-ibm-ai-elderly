package com.example.demo.repository;

import com.example.demo.pojo.ImportantDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的重要日期数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBImportantDateRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBImportantDateRepository implements ImportantDateRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "important_dates";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<ImportantDate> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToImportantDate(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find important date by id: " + id, e);
        }
    }
    
    @Override
    public ImportantDate save(ImportantDate importantDate) {
        try {
            if (importantDate.getId() == null) {
                importantDate.setId(System.currentTimeMillis());
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(importantDate);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return importantDate;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save important date: " + importantDate, e);
        }
    }
    
    @Override
    public ImportantDate update(ImportantDate importantDate) {
        return save(importantDate);
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
            throw new RuntimeException("Failed to delete important date by id: " + id, e);
        }
    }
    
    @Override
    public List<ImportantDate> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<ImportantDate> dates = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                dates.add(mapToImportantDate(item));
            }
            
            return dates;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all important dates", e);
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
            throw new RuntimeException("Failed to count important dates", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(ImportantDate date) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(date.getId().toString()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private ImportantDate mapToImportantDate(Map<String, AttributeValue> item) {
        ImportantDate date = new ImportantDate();
        
        if (item.containsKey(ID_ATTR)) {
            date.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        // 添加其他字段映射...
        
        return date;
    }
}
