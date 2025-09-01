package com.example.demo.repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.example.demo.pojo.Memo;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

/**
 * DynamoDB版本的备忘录数据访问实现
 * 使用AWS DynamoDB作为云端数据存储
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("dynamoDBMemoRepository")
@Profile("aws") // 只在AWS环境下使用
public class DynamoDBMemoRepository implements MemoRepositoryInterface {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    private static final String TABLE_NAME = "memos";
    private static final String ID_ATTR = "id";
    private static final String USER_ID_ATTR = "userId";
    private static final String TITLE_ATTR = "title";
    private static final String CONTENT_ATTR = "content";
    private static final String TYPE_ATTR = "type";
    private static final String IS_IMPORTANT_ATTR = "isImportant";
    private static final String PIN_CODE_ATTR = "pinCode";
    private static final String CREATE_TIME_ATTR = "createTime";
    private static final String UPDATE_TIME_ATTR = "updateTime";
    private static final String IS_DELETED_ATTR = "isDeleted";
    
    @Override
    public Memo save(Memo memo) {
        try {
            Map<String, AttributeValue> item = mapToDynamoDBItem(memo);
            
            PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
            
            dynamoDbClient.putItem(request);
            
            return memo;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to save memo: " + memo, e);
        }
    }
    
    @Override
    public Optional<Memo> findById(Long id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(Collections.singletonMap(ID_ATTR, AttributeValue.builder().s(id.toString()).build()))
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapToMemo(response.item()));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find memo by id: " + id, e);
        }
    }
    
    @Override
    public Optional<Memo> findByUserIdAndId(Long userId, Long memoId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(ID_ATTR, AttributeValue.builder().s(memoId.toString()).build());
            key.put(USER_ID_ATTR, AttributeValue.builder().s(userId.toString()).build());
            
            GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.item() == null || response.item().isEmpty()) {
                return Optional.empty();
            }
            
            Memo memo = mapToMemo(response.item());
            if (memo.isDeleted()) {
                return Optional.empty();
            }
            
            return Optional.of(memo);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find memo by userId and id: " + userId + ", " + memoId, e);
        }
    }
    
    @Override
    public List<Memo> findByUserId(Long userId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-index") // 需要创建GSI
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find memos by userId: " + userId, e);
        }
    }
    
    @Override
    public List<Memo> findByUserIdAndType(Long userId, String type) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#type", TYPE_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":type", AttributeValue.builder().s(type).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-type-index") // 需要创建GSI
                .keyConditionExpression("#userId = :userId AND #type = :type")
                .filterExpression("#isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find memos by userId and type: " + userId + ", " + type, e);
        }
    }
    
    @Override
    public List<Memo> findImportantByUserId(Long userId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#isImportant", IS_IMPORTANT_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":isImportant", AttributeValue.builder().bool(true).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-important-index") // 需要创建GSI
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#isImportant = :isImportant AND #isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find important memos by userId: " + userId, e);
        }
    }
    
    @Override
    public List<Memo> findByUserIdAndPinCode(Long userId, String pinCode) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#pinCode", PIN_CODE_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":pinCode", AttributeValue.builder().s(pinCode).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-pinCode-index") // 需要创建GSI
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#pinCode = :pinCode AND #isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find memos by userId and pinCode: " + userId + ", " + pinCode, e);
        }
    }
    
    @Override
    public List<Memo> searchByKeyword(Long userId, String keyword) {
        try {
            // 使用Scan操作进行全文搜索（在生产环境中应该使用Elasticsearch等搜索引擎）
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#title", TITLE_ATTR);
            expressionAttributeNames.put("#content", CONTENT_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":keyword", AttributeValue.builder().s(keyword.toLowerCase()).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("#userId = :userId AND #isDeleted = :isDeleted AND (contains(lower(#title), :keyword) OR contains(lower(#content), :keyword))")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to search memos by keyword: " + keyword, e);
        }
    }
    
    @Override
    public int softDelete(Long userId, Long memoId) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(ID_ATTR, AttributeValue.builder().s(memoId.toString()).build());
            key.put(USER_ID_ATTR, AttributeValue.builder().s(userId.toString()).build());
            
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            expressionAttributeNames.put("#updateTime", UPDATE_TIME_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(true).build());
            expressionAttributeValues.put(":updateTime", AttributeValue.builder().s(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
            
            UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET #isDeleted = :isDeleted, #updateTime = :updateTime")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();
            
            UpdateItemResponse response = dynamoDbClient.updateItem(request);
            
            return response.attributes() != null && !response.attributes().isEmpty() ? 1 : 0;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to soft delete memo: " + userId + ", " + memoId, e);
        }
    }
    
    @Override
    public long countByUserId(Long userId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-index")
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .select(Select.COUNT)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            return response.count();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to count memos by userId: " + userId, e);
        }
    }
    
    @Override
    public long countImportantByUserId(Long userId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#isImportant", IS_IMPORTANT_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":isImportant", AttributeValue.builder().bool(true).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-important-index")
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#isImportant = :isImportant AND #isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .select(Select.COUNT)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            return response.count();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to count important memos by userId: " + userId, e);
        }
    }
    
    @Override
    public long countByUserIdAndType(Long userId, String type) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#type", TYPE_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":type", AttributeValue.builder().s(type).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-type-index")
                .keyConditionExpression("#userId = :userId AND #type = :type")
                .filterExpression("#isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .select(Select.COUNT)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            return response.count();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to count memos by userId and type: " + userId + ", " + type, e);
        }
    }
    
    @Override
    public boolean existsByUserIdAndPinCode(Long userId, String pinCode) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#userId", USER_ID_ATTR);
            expressionAttributeNames.put("#pinCode", PIN_CODE_ATTR);
            expressionAttributeNames.put("#isDeleted", IS_DELETED_ATTR);
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userId", AttributeValue.builder().s(userId.toString()).build());
            expressionAttributeValues.put(":pinCode", AttributeValue.builder().s(pinCode).build());
            expressionAttributeValues.put(":isDeleted", AttributeValue.builder().bool(false).build());
            
            QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName("userId-pinCode-index")
                .keyConditionExpression("#userId = :userId")
                .filterExpression("#pinCode = :pinCode AND #isDeleted = :isDeleted")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .select(Select.COUNT)
                .build();
            
            QueryResponse response = dynamoDbClient.query(request);
            
            return response.count() > 0;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if memo exists by userId and pinCode: " + userId + ", " + pinCode, e);
        }
    }
    
    @Override
    public List<Memo> findAll() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Memo> memos = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                memos.add(mapToMemo(item));
            }
            
            return memos;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to find all memos", e);
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
            throw new RuntimeException("Failed to count all memos", e);
        }
    }
    
    // 辅助方法：将Memo对象转换为DynamoDB Item
    private Map<String, AttributeValue> mapToDynamoDBItem(Memo memo) {
        Map<String, AttributeValue> item = new HashMap<>();
        
        item.put(ID_ATTR, AttributeValue.builder().s(memo.getId().toString()).build());
        item.put(USER_ID_ATTR, AttributeValue.builder().s(memo.getUserId().toString()).build());
        item.put(TITLE_ATTR, AttributeValue.builder().s(memo.getTitle()).build());
        item.put(CONTENT_ATTR, AttributeValue.builder().s(memo.getContent()).build());
        item.put(TYPE_ATTR, AttributeValue.builder().s(memo.getType()).build());
        item.put(IS_IMPORTANT_ATTR, AttributeValue.builder().bool(memo.isImportant()).build());
        
        if (memo.getPinCode() != null) {
            item.put(PIN_CODE_ATTR, AttributeValue.builder().s(memo.getPinCode()).build());
        }
        
        if (memo.getCreateTime() != null) {
            item.put(CREATE_TIME_ATTR, AttributeValue.builder().s(memo.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        if (memo.getUpdateTime() != null) {
            item.put(UPDATE_TIME_ATTR, AttributeValue.builder().s(memo.getUpdateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        }
        
        item.put(IS_DELETED_ATTR, AttributeValue.builder().bool(memo.isDeleted()).build());
        
        return item;
    }
    
    // 辅助方法：将DynamoDB Item转换为Memo对象
    private Memo mapToMemo(Map<String, AttributeValue> item) {
        Memo memo = new Memo();
        
        if (item.containsKey(ID_ATTR)) {
            memo.setId(Long.parseLong(item.get(ID_ATTR).s()));
        }
        
        if (item.containsKey(USER_ID_ATTR)) {
            memo.setUserId(Long.parseLong(item.get(USER_ID_ATTR).s()));
        }
        
        if (item.containsKey(TITLE_ATTR)) {
            memo.setTitle(item.get(TITLE_ATTR).s());
        }
        
        if (item.containsKey(CONTENT_ATTR)) {
            memo.setContent(item.get(CONTENT_ATTR).s());
        }
        
        if (item.containsKey(TYPE_ATTR)) {
            memo.setType(item.get(TYPE_ATTR).s());
        }
        
        if (item.containsKey(IS_IMPORTANT_ATTR)) {
            memo.setImportant(item.get(IS_IMPORTANT_ATTR).bool());
        }
        
        if (item.containsKey(PIN_CODE_ATTR)) {
            memo.setPinCode(item.get(PIN_CODE_ATTR).s());
        }
        
        if (item.containsKey(CREATE_TIME_ATTR)) {
            memo.setCreateTime(LocalDateTime.parse(item.get(CREATE_TIME_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (item.containsKey(UPDATE_TIME_ATTR)) {
            memo.setUpdateTime(LocalDateTime.parse(item.get(UPDATE_TIME_ATTR).s(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        if (item.containsKey(IS_DELETED_ATTR)) {
            memo.setDeleted(item.get(IS_DELETED_ATTR).bool());
        }
        
        return memo;
    }
} 
 
 