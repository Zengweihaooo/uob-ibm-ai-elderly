package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.Memo;
import com.example.demo.repository.MemoRepository;

/**
 * 备忘录服务类
 * 提供备忘录的增删改查功能，以及PIN码验证和加密功能
 */
@Service
public class MemoService {
    
    @Autowired
    @Qualifier("sqliteMemoRepository")
    private MemoRepository memoRepository;
    
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
            
            // 智能分类：如果标题或内容包含密码相关关键词，自动归类为重要类型
            String finalType = type;
            if (isPasswordRelated(title.trim(), content.trim())) {
                finalType = "important";
            }
            
            // 创建备忘录对象
            Memo memo = new Memo(userId, title.trim(), content.trim(), finalType);
            memo.setUpdateTime(LocalDateTime.now());
            
            // 如果是重要类型，设置默认PIN码
            if ("important".equals(finalType)) {
                memo.setPinCode("1234"); // 默认PIN码，用户可以修改
                memo.setImportant(true);
            }
            
            // 保存到数据库
            int insertResult = memoRepository.insert(memo);
            
            if (insertResult > 0) {
                result.put("success", true);
                result.put("message", "Memo created successfully");
                result.put("memo", memo);
            } else {
                result.put("success", false);
                result.put("message", "Failed to create memo");
            }
            
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
            List<Memo> memos = memoRepository.findByUserId(userId);
            
            result.put("success", true);
            result.put("memos", memos);
            result.put("totalCount", memos.size());
            
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
            Optional<Memo> memoOpt = memoRepository.findByUserIdAndId(userId, memoId);
            
            if (memoOpt.isPresent()) {
                Memo memo = memoOpt.get();
                result.put("success", true);
                result.put("memo", memo);
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
            // 先查找备忘录
            Optional<Memo> memoOpt = memoRepository.findByUserIdAndId(userId, memoId);
            
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
                
                // 更新到数据库
                int updateResult = memoRepository.save(memo);
                
                if (updateResult > 0) {
                    result.put("success", true);
                    result.put("message", "Memo updated successfully");
                    result.put("memo", memo);
                } else {
                    result.put("success", false);
                    result.put("message", "Failed to update memo");
                }
                
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
            int deleteResult = memoRepository.softDelete(userId, memoId);
            
            if (deleteResult > 0) {
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
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN码不能为空");
                return result;
            }
            
            // 检查用户是否有使用该PIN码的备忘录
            boolean isValid = memoRepository.existsByUserIdAndPinCode(userId, pinCode.trim());
            
            if (isValid) {
                result.put("success", true);
                result.put("message", "PIN码验证成功");
            } else {
                result.put("success", false);
                result.put("message", "PIN码验证失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证特定备忘录的PIN码
     * @param userId 用户ID
     * @param memoId 备忘录ID
     * @param pinCode 输入的PIN码
     * @return 验证结果
     */
    public Map<String, Object> verifyMemoPinCode(Long userId, Long memoId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN码不能为空");
                return result;
            }
            
            // 获取备忘录
            Optional<Memo> memoOpt = memoRepository.findByUserIdAndId(userId, memoId);
            if (!memoOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "备忘录不存在");
                return result;
            }
            
            Memo memo = memoOpt.get();
            
            // 检查备忘录是否属于当前用户
            if (!memo.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权访问此备忘录");
                return result;
            }
            
            // 验证PIN码
            if (pinCode.trim().equals(memo.getPinCode())) {
                result.put("success", true);
                result.put("message", "PIN码验证成功");
            } else {
                result.put("success", false);
                result.put("message", "PIN码验证失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 设置新PIN码（已验证当前PIN，直接设置）
     * @param userId 用户ID
     * @param newPinCode 新的PIN码
     * @return 设置结果
     */
    public Map<String, Object> setNewPin(Long userId, String newPinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证输入参数
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新PIN码不能为空");
                return result;
            }
            
            // 验证新PIN码格式（4位数字）
            if (!newPinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "新PIN码必须是4位数字");
                return result;
            }
            
            // 检查用户是否有Important类型的备忘录
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            
            if (importantMemos.isEmpty()) {
                result.put("success", false);
                result.put("message", "您还没有重要类型的备忘录，无法设置PIN码");
                return result;
            }
            
            // 直接更新用户所有Important备忘录的PIN码（因为已经验证了当前PIN）
            int updatedCount = 0;
            
            for (Memo memo : importantMemos) {
                memo.setPinCode(newPinCode.trim());
                memo.setUpdateTime(LocalDateTime.now());
                int updateResult = memoRepository.update(memo);
                if (updateResult > 0) {
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                result.put("success", true);
                result.put("message", "PIN码更新成功，已更新 " + updatedCount + " 个重要备忘录");
            } else {
                result.put("success", false);
                result.put("message", "PIN码更新失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码设置失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证当前PIN码
     * @param userId 用户ID
     * @param currentPin 当前PIN码
     * @return 验证结果
     */
    public Map<String, Object> verifyCurrentPin(Long userId, String currentPin) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证输入参数
            if (currentPin == null || currentPin.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "当前PIN码不能为空");
                return result;
            }
            
            // 检查用户是否有Important类型的备忘录
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            
            if (importantMemos.isEmpty()) {
                result.put("success", false);
                result.put("message", "您还没有重要类型的备忘录，无法验证PIN码");
                return result;
            }
            
            // 验证当前PIN码是否正确
            boolean currentPinValid = false;
            for (Memo memo : importantMemos) {
                if (currentPin.trim().equals(memo.getPinCode())) {
                    currentPinValid = true;
                    break;
                }
            }
            
            if (currentPinValid) {
                result.put("success", true);
                result.put("message", "当前PIN码验证成功");
            } else {
                result.put("success", false);
                result.put("message", "当前PIN码验证失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码验证失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 设置用户PIN码（需要验证旧PIN码）
     * @param userId 用户ID
     * @param oldPinCode 旧PIN码
     * @param newPinCode 新的PIN码
     * @return 设置结果
     */
    public Map<String, Object> setPinCode(Long userId, String oldPinCode, String newPinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 验证输入参数
            if (oldPinCode == null || oldPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "旧PIN码不能为空");
                return result;
            }
            
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "新PIN码不能为空");
                return result;
            }
            
            // 验证新PIN码格式（4位数字）
            if (!newPinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "新PIN码必须是4位数字");
                return result;
            }
            
            // 检查用户是否有Important类型的备忘录
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            boolean hasImportantMemos = !importantMemos.isEmpty();
            
            // 如果没有Important备忘录，创建一个默认的Important备忘录来存储PIN码
            if (!hasImportantMemos) {
                Memo defaultMemo = new Memo(userId, "PIN码设置", "这是用于存储PIN码的默认备忘录", "important");
                defaultMemo.setPinCode(newPinCode.trim());
                defaultMemo.setImportant(true);
                defaultMemo.setUpdateTime(LocalDateTime.now());
                
                int insertResult = memoRepository.insert(defaultMemo);
                if (insertResult > 0) {
                    result.put("success", true);
                    result.put("message", "PIN码设置成功，已创建默认重要备忘录");
                    return result;
                } else {
                    result.put("success", false);
                    result.put("message", "PIN码设置失败");
                    return result;
                }
            }
            
            // 验证旧PIN码是否正确
            boolean oldPinValid = false;
            for (Memo memo : importantMemos) {
                if (oldPinCode.trim().equals(memo.getPinCode())) {
                    oldPinValid = true;
                    break;
                }
            }
            
            if (!oldPinValid) {
                result.put("success", false);
                result.put("message", "旧PIN码验证失败");
                return result;
            }
            
            // 更新用户所有Important备忘录的PIN码
            int updatedCount = 0;
            
            for (Memo memo : importantMemos) {
                memo.setPinCode(newPinCode.trim());
                memo.setUpdateTime(LocalDateTime.now());
                int updateResult = memoRepository.update(memo);
                if (updateResult > 0) {
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                result.put("success", true);
                result.put("message", "PIN码设置成功，已更新 " + updatedCount + " 个重要备忘录");
            } else {
                result.put("success", false);
                result.put("message", "PIN码更新失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码设置失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查用户是否已经设置了PIN码
     * @param userId 用户ID
     * @return 检查结果
     */
    public Map<String, Object> checkPinCodeStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            boolean hasImportantMemos = !importantMemos.isEmpty();
            
            if (!hasImportantMemos) {
                result.put("success", true);
                result.put("hasPin", false);
                result.put("hasImportantMemos", false);
                result.put("message", "您还没有创建重要类型的备忘录");
                return result;
            }
            
            // 检查是否已经有备忘录设置了PIN码（不是默认的1234）
            boolean hasExistingPin = false;
            for (Memo memo : importantMemos) {
                if (memo.getPinCode() != null && !memo.getPinCode().equals("1234")) {
                    hasExistingPin = true;
                    break;
                }
            }
            
            result.put("success", true);
            result.put("hasPin", hasExistingPin);
            result.put("hasImportantMemos", true);
            result.put("message", hasExistingPin ? "用户已设置PIN码" : "用户未设置PIN码");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查PIN码状态失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 设置用户PIN码（首次设置，不需要验证旧PIN码）
     * @param userId 用户ID
     * @param pinCode 新的PIN码
     * @return 设置结果
     */
    public Map<String, Object> setPinCode(Long userId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN码不能为空");
                return result;
            }
            
            // 验证PIN码格式（4位数字）
            if (!pinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "PIN码必须是4位数字");
                return result;
            }
            
            // 检查用户是否有Important类型的备忘录
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            boolean hasImportantMemos = !importantMemos.isEmpty();
            
            // 如果没有Important备忘录，创建一个默认的Important备忘录来存储PIN码
            if (!hasImportantMemos) {
                Memo defaultMemo = new Memo(userId, "PIN码设置", "这是用于存储PIN码的默认备忘录", "important");
                defaultMemo.setPinCode(pinCode.trim());
                defaultMemo.setImportant(true);
                defaultMemo.setUpdateTime(LocalDateTime.now());
                
                int insertResult = memoRepository.insert(defaultMemo);
                if (insertResult > 0) {
                    result.put("success", true);
                    result.put("message", "PIN码设置成功，已创建默认重要备忘录");
                    return result;
                } else {
                    result.put("success", false);
                    result.put("message", "PIN码设置失败");
                    return result;
                }
            }
            
            // 检查是否已经有备忘录设置了PIN码（不是默认的1234）
            boolean hasExistingPin = false;
            for (Memo memo : importantMemos) {
                if (memo.getPinCode() != null && !memo.getPinCode().equals("1234")) {
                    hasExistingPin = true;
                    break;
                }
            }
            
            if (hasExistingPin) {
                result.put("success", false);
                result.put("message", "PIN码已设置，请使用验证旧PIN码的方式重新设置");
                result.put("needOldPin", true);
                return result;
            }
            
            // 更新用户所有Important备忘录的PIN码
            int updatedCount = 0;
            
            for (Memo memo : importantMemos) {
                memo.setPinCode(pinCode.trim());
                memo.setUpdateTime(LocalDateTime.now());
                int updateResult = memoRepository.update(memo);
                if (updateResult > 0) {
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                result.put("success", true);
                result.put("message", "PIN码设置成功，已更新 " + updatedCount + " 个重要备忘录");
            } else {
                result.put("success", false);
                result.put("message", "PIN码更新失败");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN码设置失败: " + e.getMessage());
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
                result.put("message", "搜索关键词不能为空");
                return result;
            }
            
            List<Memo> searchResults = memoRepository.searchByKeyword(userId, keyword.trim());
            
            result.put("success", true);
            result.put("memos", searchResults);
            result.put("keyword", keyword);
            result.put("totalCount", searchResults.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搜索备忘录失败: " + e.getMessage());
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
            long totalCount = memoRepository.countByUserId(userId);
            long importantCount = memoRepository.countImportantByUserId(userId);
            long generalCount = memoRepository.countByUserIdAndType(userId, "general");
            long todoCount = memoRepository.countByUserIdAndType(userId, "todo");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", totalCount);
            statistics.put("important", importantCount);
            statistics.put("general", generalCount);
            statistics.put("todo", todoCount);
            
            result.put("success", true);
            result.put("statistics", statistics);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
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
                    result.put("message", "PIN码验证失败");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get encrypted memo content: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 判断备忘录是否包含密码相关内容
     * @param title 备忘录标题
     * @param content 备忘录内容
     * @return 是否包含密码相关内容
     */
    private boolean isPasswordRelated(String title, String content) {
        if (title == null) title = "";
        if (content == null) content = "";
        
        String combinedText = (title + " " + content).toLowerCase();
        
        // 密码相关关键词
        String[] passwordKeywords = {
            "密码", "password", "pwd", "pass", "mima", "密碼",
            "账号", "account", "用户名", "username", "user",
            "登录", "login", "signin", "sign in",
            "银行卡", "bank card", "信用卡", "credit card",
            "身份证", "id card", "身份证号", "id number",
            "手机号", "phone", "电话号码", "phone number",
            "邮箱", "email", "邮箱地址", "email address",
            "密钥", "key", "私钥", "private key", "公钥", "public key",
            "token", "令牌", "验证码", "verification code",
            "pin", "pin码", "pin code"
        };
        
        for (String keyword : passwordKeywords) {
            if (combinedText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
} 