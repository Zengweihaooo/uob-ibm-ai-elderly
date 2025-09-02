package com.example.demo.repository;

import com.example.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * DynamoDB版本的用户数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBUserRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBUserRepository implements UserRepository {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "users";
    private static final String ID_ATTR = "id";
    private static final String EMAIL_ATTR = "email";
    private static final String USERNAME_ATTR = "username";
    private static final String PASSWORD_HASH_ATTR = "passwordHash";
    private static final String NAME_ATTR = "name";
    private static final String VERIFICATION_CODE_ATTR = "verificationCode";
    private static final String STATUS_ATTR = "status";
    private static final String ROLE_ATTR = "role";
    private static final String PHONE_NUMBER_ATTR = "phoneNumber";
    private static final String IS_VERIFIED_ATTR = "isVerified";
    private static final String CREATED_AT_ATTR = "createdAt";
    private static final String VERIFIED_AT_ATTR = "verifiedAt";
    private static final String CODE_EXPIRES_AT_ATTR = "codeExpiresAt";
    private static final String UPDATED_AT_ATTR = "updatedAt";
    
    @Override
    public Optional<User> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToUser(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by id: " + id, e);
        }
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        try {
            // 使用GSI查询邮箱
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#email", EMAIL_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":email", AttributeValue.builder().s(email).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("email-index") // 需要创建GSI
                .keyConditionExpression("#email = :email")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            if (response.items().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToUser(response.items().get(0)));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by email: " + email, e);
        }
    }
    
    @Override
    public User save(User user) {
        try {
            // 如果是新用户，生成ID
            if (user.getId() == null) {
                user.setId(System.currentTimeMillis()); // 简单的ID生成策略
            }
            
            // 设置更新时间
            user.setUpdatedAt(LocalDateTime.now());
            
            Map<String, AttributeValue> item = mapToDynamoDBItem(user);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return user;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user: " + user, e);
        }
    }
    
    @Override
    public User update(User user) {
        return save(user); // DynamoDB的put操作可以覆盖现有记录
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
            throw new RuntimeException("Failed to delete user by id: " + id, e);
        }
    }
    
    @Override
    public List<User> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<User> users = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                users.add(mapToUser(item));
            }
            
            return users;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all users", e);
        }
    }
    
    @Override
    public List<User> findByStatus(String status) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#status", STATUS_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":status", AttributeValue.builder().s(status).build());
            
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("#status = :status")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<User> users = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                users.add(mapToUser(item));
            }
            
            return users;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find users by status: " + status, e);
        }
    }
    
    @Override
    public List<User> findByRole(String role) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#role", ROLE_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":role", AttributeValue.builder().s(role).build());
            
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("#role = :role")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<User> users = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                users.add(mapToUser(item));
            }
            
            return users;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find users by role: " + role, e);
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
            throw new RuntimeException("Failed to count users", e);
        }
    }
    
    /**
     * 将User对象转换为DynamoDB属性映射
     */
    private Map<String, AttributeValue> mapToDynamoDBItem(User user) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(user.getId().toString()).build());
        item.put(EMAIL_ATTR, AttributeValue.builder().s(user.getEmail()).build());
        item.put(USERNAME_ATTR, AttributeValue.builder().s(user.getUsername()).build());
        item.put(NAME_ATTR, AttributeValue.builder().s(user.getName()).build());
        item.put(STATUS_ATTR, AttributeValue.builder().s(user.getStatus().toString()).build());
        item.put(ROLE_ATTR, AttributeValue.builder().s(user.getRole().toString()).build());
        item.put(IS_VERIFIED_ATTR, AttributeValue.builder().bool(user.getIsVerified()).build());
        
        if (user.getPasswordHash() != null) {
            item.put(PASSWORD_HASH_ATTR, AttributeValue.builder().s(user.getPasswordHash()).build());
        }
        
        if (user.getVerificationCode() != null) {
            item.put(VERIFICATION_CODE_ATTR, AttributeValue.builder().s(user.getVerificationCode()).build());
        }
        
        if (user.getPhoneNumber() != null) {
            item.put(PHONE_NUMBER_ATTR, AttributeValue.builder().s(user.getPhoneNumber()).build());
        }
        
        if (user.getCreatedAt() != null) {
            item.put(CREATED_AT_ATTR, AttributeValue.builder().s(user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        if (user.getVerifiedAt() != null) {
            item.put(VERIFIED_AT_ATTR, AttributeValue.builder().s(user.getVerifiedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        if (user.getCodeExpiresAt() != null) {
            item.put(CODE_EXPIRES_AT_ATTR, AttributeValue.builder().s(user.getCodeExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        if (user.getUpdatedAt() != null) {
            item.put(UPDATED_AT_ATTR, AttributeValue.builder().s(user.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        return item;
    }
    
    /**
     * 将DynamoDB属性映射转换为User对象
     */
    private User mapToUser(Map<String, AttributeValue> item) {
        User user = new User();
        
        if (item.containsKey(ID_ATTR)) {
            user.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        if (item.containsKey(EMAIL_ATTR)) {
            user.setEmail(item.get(EMAIL_ATTR).s());
        }
        
        if (item.containsKey(USERNAME_ATTR)) {
            user.setUsername(item.get(USERNAME_ATTR).s());
        }
        
        if (item.containsKey(PASSWORD_HASH_ATTR)) {
            user.setPasswordHash(item.get(PASSWORD_HASH_ATTR).s());
        }
        
        if (item.containsKey(NAME_ATTR)) {
            user.setName(item.get(NAME_ATTR).s());
        }
        
        if (item.containsKey(VERIFICATION_CODE_ATTR)) {
            user.setVerificationCode(item.get(VERIFICATION_CODE_ATTR).s());
        }
        
        if (item.containsKey(STATUS_ATTR)) {
            user.setStatus(User.UserStatus.valueOf(item.get(STATUS_ATTR).s()));
        }
        
        if (item.containsKey(ROLE_ATTR)) {
            user.setRole(User.UserRole.valueOf(item.get(ROLE_ATTR).s()));
        }
        
        if (item.containsKey(PHONE_NUMBER_ATTR)) {
            user.setPhoneNumber(item.get(PHONE_NUMBER_ATTR).s());
        }
        
        if (item.containsKey(IS_VERIFIED_ATTR)) {
            user.setIsVerified(item.get(IS_VERIFIED_ATTR).bool());
        }
        
        if (item.containsKey(CREATED_AT_ATTR)) {
            user.setCreatedAt(LocalDateTime.parse(item.get(CREATED_AT_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (item.containsKey(VERIFIED_AT_ATTR)) {
            user.setVerifiedAt(LocalDateTime.parse(item.get(VERIFIED_AT_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (item.containsKey(CODE_EXPIRES_AT_ATTR)) {
            user.setCodeExpiresAt(LocalDateTime.parse(item.get(CODE_EXPIRES_AT_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (item.containsKey(UPDATED_AT_ATTR)) {
            user.setUpdatedAt(LocalDateTime.parse(item.get(UPDATED_AT_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return user;
    }
}
