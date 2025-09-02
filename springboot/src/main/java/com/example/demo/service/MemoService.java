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
 * Memo service class
 * Provides memo CRUD functionality, PIN code verification and encryption features
 */
@Service
public class MemoService {
    
    @Autowired
    @Qualifier("sqliteMemoRepository")
    private MemoRepository memoRepository;
    
    /**
     * Create new memo
     * @param userId user ID
     * @param title memo title
     * @param content memo content
     * @param type memo type
     * @return creation result
     */
    public Map<String, Object> createMemo(Long userId, String title, String content, String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate input parameters
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
            
            // Smart classification: if title or content contains password-related keywords, automatically classify as important type
            String finalType = type;
            if (isPasswordRelated(title.trim(), content.trim())) {
                finalType = "important";
            }
            
            // Create memo object
            Memo memo = new Memo(userId, title.trim(), content.trim(), finalType);
            memo.setUpdateTime(LocalDateTime.now());
            
            // If it's important type, set default PIN code
            if ("important".equals(finalType)) {
                memo.setPinCode("1234"); // Default PIN code, user can modify
                memo.setImportant(true);
            }
            
            // Save to database
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
     * Get all memos for a user
     * @param userId user ID
     * @return memo list
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
     * Get memo by ID
     * @param userId user ID
     * @param memoId memo ID
     * @return memo information
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
     * Update memo
     * @param userId user ID
     * @param memoId memo ID
     * @param title new title
     * @param content new content
     * @param type new type
     * @return update result
     */
    public Map<String, Object> updateMemo(Long userId, Long memoId, String title, String content, String type) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // First, find the memo
            Optional<Memo> memoOpt = memoRepository.findByUserIdAndId(userId, memoId);
            
            if (memoOpt.isPresent()) {
                Memo memo = memoOpt.get();
                // Update memo information
                if (title != null && !title.trim().isEmpty()) {
                    memo.setTitle(title.trim());
                }
                if (content != null && !content.trim().isEmpty()) {
                    memo.setContent(content.trim());
                }
                if (type != null) {
                    memo.setType(type);
                    // If it's important type, automatically set to encrypted
                    if ("important".equals(type)) {
                        memo.setImportant(true);
                        if (memo.getPinCode() == null) {
                            memo.setPinCode("1234");
                        }
                    }
                }
                
                memo.setUpdateTime(LocalDateTime.now());
                
                // Update to database
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
     * Delete memo (soft delete)
     * @param userId user ID
     * @param memoId memo ID
     * @return delete result
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
     * Verify PIN code
     * @param userId user ID
     * @param pinCode input PIN code
     * @return verification result
     */
    public Map<String, Object> verifyPinCode(Long userId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN code cannot be empty");
                return result;
            }
            
            // Check if user has memos using this PIN code
            boolean isValid = memoRepository.existsByUserIdAndPinCode(userId, pinCode.trim());
            
            if (isValid) {
                result.put("success", true);
                result.put("message", "PIN code verified successfully");
            } else {
                result.put("success", false);
                result.put("message", "PIN code verification failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code verification failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Verify PIN code for a specific memo
     * @param userId user ID
     * @param memoId memo ID
     * @param pinCode input PIN code
     * @return verification result
     */
    public Map<String, Object> verifyMemoPinCode(Long userId, Long memoId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN code cannot be empty");
                return result;
            }
            
            // Get memo
            Optional<Memo> memoOpt = memoRepository.findByUserIdAndId(userId, memoId);
            if (!memoOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "Memo not found");
                return result;
            }
            
            Memo memo = memoOpt.get();
            
            // Check if memo belongs to current user
            if (!memo.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "Unauthorized access to this memo");
                return result;
            }
            
            // Verify PIN code
            if (pinCode.trim().equals(memo.getPinCode())) {
                result.put("success", true);
                result.put("message", "PIN code verified successfully");
            } else {
                result.put("success", false);
                result.put("message", "PIN code verification failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code verification failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Set new PIN code (already verified current PIN, directly set)
     * @param userId user ID
     * @param newPinCode new PIN code
     * @return set result
     */
    public Map<String, Object> setNewPin(Long userId, String newPinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate input parameters
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "New PIN code cannot be empty");
                return result;
            }
            
            // Validate new PIN code format (4 digits)
            if (!newPinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "New PIN code must be 4 digits");
                return result;
            }
            
            System.out.println("DEBUG: Setting new PIN code - User ID: " + userId + ", New PIN: " + newPinCode.trim());
            
            // Check if user has Important type memos
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            
            System.out.println("DEBUG: Found " + importantMemos.size() + " important memos to update");
            
            if (importantMemos.isEmpty()) {
                result.put("success", false);
                result.put("message", "You do not have important type memos, cannot set PIN code");
                return result;
            }
            
            // Directly update PIN code for all Important memos of the user (since current PIN is already verified)
            int updatedCount = 0;
            
            for (Memo memo : importantMemos) {
                System.out.println("DEBUG: Updating memo ID: " + memo.getId() + " PIN from " + memo.getPinCode() + " to " + newPinCode.trim());
                memo.setPinCode(newPinCode.trim());
                memo.setUpdateTime(LocalDateTime.now());
                int updateResult = memoRepository.update(memo);
                if (updateResult > 0) {
                    updatedCount++;
                    System.out.println("DEBUG: memo ID: " + memo.getId() + " updated successfully");
                } else {
                    System.out.println("DEBUG: memo ID: " + memo.getId() + " update failed");
                }
            }
            
            if (updatedCount > 0) {
                result.put("success", true);
                result.put("message", "PIN code updated successfully, " + updatedCount + " important memos updated");
                System.out.println("DEBUG: PIN code updated successfully, total " + updatedCount + " memos updated");
            } else {
                result.put("success", false);
                result.put("message", "PIN code update failed");
                System.out.println("DEBUG: PIN code update failed, no memos were updated");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code setting failed: " + e.getMessage());
            System.err.println("DEBUG: PIN code setting exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Verify current PIN code
     * @param userId user ID
     * @param currentPin current PIN code
     * @return verification result
     */
    public Map<String, Object> verifyCurrentPin(Long userId, String currentPin) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate input parameters
            if (currentPin == null || currentPin.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Current PIN code cannot be empty");
                return result;
            }
            
            System.out.println("DEBUG: Verifying PIN code - User ID: " + userId + ", Input PIN: " + currentPin.trim());
            
            // Check if user has Important type memos
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            
            System.out.println("DEBUG: Found " + importantMemos.size() + " important memos");
            
            if (importantMemos.isEmpty()) {
                result.put("success", false);
                result.put("message", "You do not have important type memos, cannot verify PIN code");
                return result;
            }
            
            // Verify current PIN code
            boolean currentPinValid = false;
            for (Memo memo : importantMemos) {
                System.out.println("DEBUG: Checking memo ID: " + memo.getId() + ", Stored PIN: " + memo.getPinCode());
                if (currentPin.trim().equals(memo.getPinCode())) {
                    currentPinValid = true;
                    System.out.println("DEBUG: PIN code matched successfully!");
                    break;
                }
            }
            
            if (currentPinValid) {
                result.put("success", true);
                result.put("message", "Current PIN code verified successfully");
                System.out.println("DEBUG: PIN code verified successfully");
            } else {
                result.put("success", false);
                result.put("message", "Current PIN code verification failed");
                System.out.println("DEBUG: PIN code verification failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code verification failed: " + e.getMessage());
            System.err.println("DEBUG: PIN code verification exception: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Set user PIN code (requires old PIN verification)
     * @param userId user ID
     * @param oldPinCode old PIN code
     * @param newPinCode new PIN code
     * @return set result
     */
    public Map<String, Object> setPinCode(Long userId, String oldPinCode, String newPinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate input parameters
            if (oldPinCode == null || oldPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Old PIN code cannot be empty");
                return result;
            }
            
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "New PIN code cannot be empty");
                return result;
            }
            
            // Validate new PIN code format (4 digits)
            if (!newPinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "New PIN code must be 4 digits");
                return result;
            }
            
            // Check if user has Important type memos
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            boolean hasImportantMemos = !importantMemos.isEmpty();
            
            // If no Important memos, create a default Important memo to store PIN code
            if (!hasImportantMemos) {
                Memo defaultMemo = new Memo(userId, "PIN Code Setting", "This is a default memo for storing PIN code", "important");
                defaultMemo.setPinCode(newPinCode.trim());
                defaultMemo.setImportant(true);
                defaultMemo.setUpdateTime(LocalDateTime.now());
                
                int insertResult = memoRepository.insert(defaultMemo);
                if (insertResult > 0) {
                    result.put("success", true);
                    result.put("message", "PIN code set successfully, default important memo created");
                    return result;
                } else {
                    result.put("success", false);
                    result.put("message", "PIN code setting failed");
                    return result;
                }
            }
            
            // Verify old PIN code
            boolean oldPinValid = false;
            for (Memo memo : importantMemos) {
                if (oldPinCode.trim().equals(memo.getPinCode())) {
                    oldPinValid = true;
                    break;
                }
            }
            
            if (!oldPinValid) {
                result.put("success", false);
                result.put("message", "Old PIN code verification failed");
                return result;
            }
            
            // Update PIN code for all Important memos of the user
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
                result.put("message", "PIN code set successfully, " + updatedCount + " important memos updated");
            } else {
                result.put("success", false);
                result.put("message", "PIN code update failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code setting failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check if user has already set PIN code
     * @param userId user ID
     * @return check result
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
                result.put("message", "You have not created important type memos yet");
                return result;
            }
            
            // Check if there is already a memo set with a PIN code (not the default 1234)
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
            result.put("message", hasExistingPin ? "User has set PIN code" : "User has not set PIN code");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to check PIN code status: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Set user PIN code (first time setting, no old PIN verification required)
     * @param userId user ID
     * @param pinCode new PIN code
     * @return set result
     */
    public Map<String, Object> setPinCode(Long userId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (pinCode == null || pinCode.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "PIN code cannot be empty");
                return result;
            }
            
            // Validate PIN code format (4 digits)
            if (!pinCode.matches("\\d{4}")) {
                result.put("success", false);
                result.put("message", "PIN code must be 4 digits");
                return result;
            }
            
            // Check if user has Important type memos
            List<Memo> importantMemos = memoRepository.findImportantByUserId(userId);
            boolean hasImportantMemos = !importantMemos.isEmpty();
            
            // If no Important memos, create a default Important memo to store PIN code
            if (!hasImportantMemos) {
                Memo defaultMemo = new Memo(userId, "PIN Code Setting", "This is a default memo for storing PIN code", "important");
                defaultMemo.setPinCode(pinCode.trim());
                defaultMemo.setImportant(true);
                defaultMemo.setUpdateTime(LocalDateTime.now());
                
                int insertResult = memoRepository.insert(defaultMemo);
                if (insertResult > 0) {
                    result.put("success", true);
                    result.put("message", "PIN code set successfully, default important memo created");
                    return result;
                } else {
                    result.put("success", false);
                    result.put("message", "PIN code setting failed");
                    return result;
                }
            }
            
            // Check if there is already a memo set with a PIN code (not the default 1234)
            boolean hasExistingPin = false;
            for (Memo memo : importantMemos) {
                if (memo.getPinCode() != null && !memo.getPinCode().equals("1234")) {
                    hasExistingPin = true;
                    break;
                }
            }
            
            if (hasExistingPin) {
                result.put("success", false);
                result.put("message", "PIN code already set, please use the old PIN verification method to re-set");
                result.put("needOldPin", true);
                return result;
            }
            
            // Update PIN code for all Important memos of the user
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
                result.put("message", "PIN code set successfully, " + updatedCount + " important memos updated");
            } else {
                result.put("success", false);
                result.put("message", "PIN code update failed");
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "PIN code setting failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Search memos
     * @param userId user ID
     * @param keyword search keyword
     * @return search result
     */
    public Map<String, Object> searchMemos(Long userId, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Search keyword cannot be empty");
                return result;
            }
            
            List<Memo> searchResults = memoRepository.searchByKeyword(userId, keyword.trim());
            
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
     * Get memo statistics
     * @param userId user ID
     * @return statistics
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
            result.put("message", "Failed to get statistics: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Get encrypted memo content (requires PIN code verification)
     * @param userId user ID
     * @param memoId memo ID
     * @param pinCode PIN code
     * @return memo content
     */
    public Map<String, Object> getEncryptedMemoContent(Long userId, Long memoId, String pinCode) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get memo
            Map<String, Object> memoResult = getMemoById(userId, memoId);
            if (!(Boolean) memoResult.get("success")) {
                return memoResult;
            }
            
            Memo memo = (Memo) memoResult.get("memo");
            
            // Check if it's an important memo
            if (!memo.isImportant()) {
                result.put("success", true);
                result.put("content", memo.getContent());
                result.put("message", "Memo content retrieved successfully");
            } else {
                // Verify PIN code of the memo
                if (pinCode.equals(memo.getPinCode())) {
                    result.put("success", true);
                    result.put("content", memo.getContent());
                    result.put("message", "Important memo content retrieved successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "PIN code verification failed");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to get encrypted memo content: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Check if memo contains password-related content
     * @param title memo title
     * @param content memo content
     * @return true if password-related content is found, false otherwise
     */
    private boolean isPasswordRelated(String title, String content) {
        if (title == null) title = "";
        if (content == null) content = "";
        
        String combinedText = (title + " " + content).toLowerCase();
        
        // Password-related keywords
        String[] passwordKeywords = {
            "password", "pwd", "pass", "mima", "密碼",
            "account", "username", "user",
            "login", "signin", "sign in",
            "bank card", "credit card",
            "id card", "id number",
            "phone", "phone number",
            "email", "email address",
            "key", "private key", "public key",
            "token", "token", "verification code",
            "pin", "pin code"
        };
        
        for (String keyword : passwordKeywords) {
            if (combinedText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }
} 