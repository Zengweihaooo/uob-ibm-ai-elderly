package com.example.demo.repository;

import com.example.demo.pojo.FamilyContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * DynamoDB版本的家庭联系人数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBFamilyContactRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBFamilyContactRepository implements FamilyContactRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "family_contacts";
    private static final String ID_ATTR = "id";
    
    @Override
    public Optional<FamilyContact> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToFamilyContact(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find family contact by id: " + id, e);
        }
    }
    
    @Override
    public FamilyContact save(FamilyContact familyContact) {
        try {
            if (familyContact.getId() == null) {
                familyContact.setId(System.currentTimeMillis());
            }
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(familyContact);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return familyContact;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save family contact: " + familyContact, e);
        }
    }
    
    @Override
    public FamilyContact update(FamilyContact familyContact) {
        return save(familyContact);
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
            throw new RuntimeException("Failed to delete family contact by id: " + id, e);
        }
    }
    
    @Override
    public List<FamilyContact> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<FamilyContact> contacts = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                contacts.add(mapToFamilyContact(item));
            }
            
            return contacts;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all family contacts", e);
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
            throw new RuntimeException("Failed to count family contacts", e);
        }
    }
    
    private Map<String, AttributeValue> mapToDynamoDBItem(FamilyContact contact) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(contact.getId().toString()).build());
        // 添加其他字段映射...
        
        return item;
    }
    
    private FamilyContact mapToFamilyContact(Map<String, AttributeValue> item) {
        FamilyContact contact = new FamilyContact();
        
        if (item.containsKey(ID_ATTR)) {
            contact.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        // 添加其他字段映射...
        
        return contact;
    }
}
