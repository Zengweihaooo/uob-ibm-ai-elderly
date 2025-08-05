package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.pojo.Memo;

/**
 * 备忘录服务类
 * 提供备忘录的增删改查功能，以及PIN码验证和加密功能
 */
@Service
public class MemoService {
    
    // 使用内存存储备忘录数据（实际项目中应该使用数据库）
    private final Map<Long, List<Memo>> userMemos = new ConcurrentHashMap<>();
    private final Map<Long, String> userPinCodes = new ConcurrentHashMap<>();
    private long memoIdCounter = 1;
    
    /**
     * 创建新备忘录
     * @param userId 用户ID
     * @param title 备忘录标题
     * @param content 备忘录内容
     * @param type 备忘录类型
     * @return 创建结果
     */
    public Map<String, Object> createMemo(Long userId, String title, String content, String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证输入参数
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Memo title cannot be empty");
                return result;
            }
            
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Memo content cannot be empty");
                return result;
            }
            
            // 创建备忘录对象
            Memo memo = new Memo(userId, title.trim(), content.trim(), type);
            memo.setId(memoIdCounter++);
            memo.setUpdateTime(LocalDateTime.now());
            
            // 如果是重要类型，设置默认PIN码
            if ("important".equals(type)) {
                memo.setPinCode("1234"); // 默认PIN码，用户可以修改
            }
            
            // 存储备忘录
            List<Memo> userMemoList = userMemos.computeIfAbsent(userId, k -> new ArrayList<>());
            userMemoList.add(memo);
            
            result.put("success", true);
            result.put("message", "Memo created successfully");
            result.put("memo", memo);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to create memo: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取用户的所有备忘录
     * @param userId 用户ID
     * @return 备忘录列表
     */
    public Map<String, Object> getUserMemos(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            
            // 过滤掉已删除的备忘录，并按更新时间排序
            List<Memo> activeMemos = memos.stream()
                    .filter(memo -> !memo.isDeleted())
                    .sorted(Comparator.comparing(Memo::getUpdateTime).reversed())
                    .collect(Collectors.toList());
            
            result.put("success", true);
            result.put("memos", activeMemos);
            result.put("totalCount", activeMemos.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get memos: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据ID获取备忘录
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @return 备忘录信息
     */
    public Map<String, Object> getMemoById(Long userId, Long memoId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            Optional<Memo> memoOpt = memos.stream()
                    .filter(memo -> memo.getId().equals(memoId) && !memo.isDeleted())
                    .findFirst();
            
            if (memoOpt.isPresent()) {
                result.put("success", true);
                result.put("memo", memoOpt.get());
            } else {
                result.put("success", false);
                result.put("message", "Memo not found");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get memo: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 更新备忘录
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @param title 新标题
     * @param content 新内容
     * @param type 新类型
     * @return 更新结果
     */
    public Map<String, Object> updateMemo(Long userId, Long memoId, String title, String content, String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            Optional<Memo> memoOpt = memos.stream()
                    .filter(memo -> memo.getId().equals(memoId) && !memo.isDeleted())
                    .findFirst();
            
            if (memoOpt.isPresent()) {
                Memo memo = memoOpt.get();
                
                // 更新备忘录信息
                if (title != null && !title.trim().isEmpty()) {
                    memo.setTitle(title.trim());
                }
                if (content != null && !content.trim().isEmpty()) {
                    memo.setContent(content.trim());
                }
                if (type != null) {
                    memo.setType(type);
                    // 如果是重要类型，自动设置为加密
                    if ("important".equals(type)) {
                        memo.setImportant(true);
                        if (memo.getPinCode() == null) {
                            memo.setPinCode("1234");
                        }
                    }
                }
                
                memo.setUpdateTime(LocalDateTime.now());
                
                result.put("success", true);
                result.put("message", "Memo updated successfully");
                result.put("memo", memo);
                
            } else {
                result.put("success", false);
                result.put("message", "Memo not found");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to update memo: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除备忘录（软删除）
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @return 删除结果
     */
    public Map<String, Object> deleteMemo(Long userId, Long memoId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            Optional<Memo> memoOpt = memos.stream()
                    .filter(memo -> memo.getId().equals(memoId) && !memo.isDeleted())
                    .findFirst();
            
            if (memoOpt.isPresent()) {
                Memo memo = memoOpt.get();
                memo.setDeleted(true);
                memo.setUpdateTime(LocalDateTime.now());
                
                result.put("success", true);
                result.put("message", "Memo deleted successfully");
                
            } else {
                result.put("success", false);
                result.put("message", "Memo not found");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to delete memo: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证PIN码
     * @param userId 用户ID
     * @param pinCode 输入的PIN码
     * @return 验证结果
     */
    public Map<String, Object> verifyPinCode(Long userId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String storedPinCode = userPinCodes.get(userId);
            
            if (storedPinCode == null) {
                // 如果没有设置PIN码，使用默认PIN码
                storedPinCode = "1234";
                userPinCodes.put(userId, storedPinCode);
            }
            
            if (storedPinCode.equals(pinCode)) {
                result.put("success", true);
                result.put("message", "PIN code verified successfully");
            } else {
                result.put("success", false);
                result.put("message", "Incorrect PIN code");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to verify PIN code: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 设置用户PIN码
     * @param userId 用户ID
     * @param newPinCode 新PIN码
     * @return 设置结果
     */
    public Map<String, Object> setPinCode(Long userId, String newPinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN code cannot be empty");
                return result;
            }
            
            if (newPinCode.length() < 4) {
                result.put("success", false);
                result.put("message", "PIN code must be at least 4 digits");
                return result;
            }
            
            userPinCodes.put(userId, newPinCode.trim());
            
            result.put("success", true);
            result.put("message", "PIN code set successfully");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to set PIN code: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取加密备忘录内容（需要PIN码验证）
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @param pinCode PIN码
     * @return 备忘录内容
     */
    public Map<String, Object> getEncryptedMemoContent(Long userId, Long memoId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 首先验证PIN码
            Map<String, Object> pinResult = verifyPinCode(userId, pinCode);
            if (!(Boolean) pinResult.get("success")) {
                return pinResult;
            }
            
            // 获取备忘录
            Map<String, Object> memoResult = getMemoById(userId, memoId);
            if (!(Boolean) memoResult.get("success")) {
                return memoResult;
            }
            
            Memo memo = (Memo) memoResult.get("memo");
            
            // 检查是否是重要备忘录
            if (!memo.isImportant()) {
                result.put("success", true);
                result.put("content", memo.getContent());
                result.put("message", "Memo content retrieved successfully");
            } else {
                // 验证备忘录的PIN码
                if (pinCode.equals(memo.getPinCode())) {
                    result.put("success", true);
                    result.put("content", memo.getContent());
                    result.put("message", "Important memo content retrieved successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "Incorrect memo PIN code");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get encrypted memo content: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 搜索备忘录
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    public Map<String, Object> searchMemos(Long userId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Search keyword cannot be empty");
                return result;
            }
            
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            String searchKeyword = keyword.toLowerCase().trim();
            
            // 搜索标题和内容包含关键词的备忘录
            List<Memo> searchResults = memos.stream()
                    .filter(memo -> !memo.isDeleted())
                    .filter(memo -> 
                        memo.getTitle().toLowerCase().contains(searchKeyword) ||
                        memo.getContent().toLowerCase().contains(searchKeyword)
                    )
                    .sorted(Comparator.comparing(Memo::getUpdateTime).reversed())
                    .collect(Collectors.toList());
            
            result.put("success", true);
            result.put("memos", searchResults);
            result.put("keyword", keyword);
            result.put("totalCount", searchResults.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to search memos: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取备忘录统计信息
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getMemoStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> memos = userMemos.getOrDefault(userId, new ArrayList<>());
            List<Memo> activeMemos = memos.stream()
                    .filter(memo -> !memo.isDeleted())
                    .collect(Collectors.toList());
            
            // 按类型统计
            long generalCount = activeMemos.stream().filter(m -> "general".equals(m.getType())).count();
            long importantCount = activeMemos.stream().filter(m -> "important".equals(m.getType())).count();
            long todoCount = activeMemos.stream().filter(m -> "todo".equals(m.getType())).count();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", activeMemos.size());
            statistics.put("generalCount", generalCount);
            statistics.put("importantCount", importantCount);
            statistics.put("todoCount", todoCount);
            
            result.put("success", true);
            result.put("statistics", statistics);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get statistics: " + e.getMessage());
        }
        
        return result;
    }
} 