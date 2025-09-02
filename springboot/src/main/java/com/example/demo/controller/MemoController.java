package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MemoService;
import com.example.demo.util.UserContextUtil;

/**
 * Memo Controller.
 * Provides REST API endpoints for memo operations.
 */
@RestController
@RequestMapping("/api/memo")
@CrossOrigin(origins = "*")
public class MemoController {

    @Autowired
    private MemoService memoService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    /**
     * Create a new memo.
     * @param memoData Memo data
     * @param authHeader Authorization header
     * @return Creation result
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMemo(
            @RequestBody Map<String, Object> memoData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String title = (String) memoData.get("title");
            String content = (String) memoData.get("content");
            String type = (String) memoData.getOrDefault("type", "general");
            
            Map<String, Object> result = memoService.createMemo(userId, title, content, type);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                response.put("memo", result.get("memo"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create memo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all memos for the authenticated user.
     * @param authHeader Authorization header
     * @return Memo list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserMemos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.getUserMemos(userId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("memos", result.get("memos"));
                response.put("totalCount", result.get("totalCount"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve memos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get a memo by its ID.
     * @param memoId Memo ID
     * @param authHeader Authorization header
     * @return Memo information
     */
    @GetMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> getMemoById(
            @PathVariable Long memoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.getMemoById(userId, memoId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("memo", result.get("memo"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve memo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update a memo.
     * @param memoId Memo ID
     * @param memoData Updated memo data
     * @param authHeader Authorization header
     * @return Update result
     */
    @PutMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> updateMemo(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> memoData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String title = (String) memoData.get("title");
            String content = (String) memoData.get("content");
            String type = (String) memoData.get("type");
            
            Map<String, Object> result = memoService.updateMemo(userId, memoId, title, content, type);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                response.put("memo", result.get("memo"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update memo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a memo.
     * @param memoId Memo ID
     * @param authHeader Authorization header
     * @return Deletion result
     */
    @DeleteMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> deleteMemo(
            @PathVariable Long memoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.deleteMemo(userId, memoId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete memo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify PIN code (general, not memo-specific).
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Verification result
     */
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPinCode(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String pinCode = (String) pinData.get("pinCode");
            
            if (pinCode == null || pinCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "PIN code must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = memoService.verifyPinCode(userId, pinCode.trim());
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "PIN code verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify the PIN code for a specific memo.
     * @param memoId Memo ID
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Verification result
     */
    @PostMapping("/{memoId}/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyMemoPinCode(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String pinCode = (String) pinData.get("pinCode");
            
            if (pinCode == null || pinCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "PIN code must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = memoService.verifyMemoPinCode(userId, memoId, pinCode.trim());
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "PIN code verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Set user PIN code (initial setup or reset).
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Result
     */
    @PostMapping("/set-pin")
    public ResponseEntity<Map<String, Object>> setPinCode(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String oldPinCode = (String) pinData.get("oldPinCode");
            String newPinCode = (String) pinData.get("newPinCode");
            String pinCode = (String) pinData.get("pinCode"); // Backward compatibility
            
            // If oldPinCode and newPinCode provided, use new verification flow
            if (oldPinCode != null && newPinCode != null) {
                Map<String, Object> result = memoService.setPinCode(userId, oldPinCode.trim(), newPinCode.trim());
                
                if ((Boolean) result.get("success")) {
                    response.put("success", true);
                    response.put("message", result.get("message"));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", result.get("message"));
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            // Backward compatibility: only pinCode provided
            if (pinCode != null && !pinCode.trim().isEmpty()) {
                Map<String, Object> result = memoService.setPinCode(userId, pinCode.trim());
                
                if ((Boolean) result.get("success")) {
                    response.put("success", true);
                    response.put("message", result.get("message"));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", result.get("message"));
            // If old PIN verification is required, return a conflict status
                    if (result.containsKey("needOldPin") && (Boolean) result.get("needOldPin")) {
                        response.put("needOldPin", true);
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                    }
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            response.put("success", false);
        response.put("message", "Incomplete PIN code parameters");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to set PIN code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Set a new PIN code (after current PIN has been verified).
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Result
     */
    @PostMapping("/set-new-pin")
    public ResponseEntity<Map<String, Object>> setNewPin(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String newPinCode = (String) pinData.get("newPinCode");
            
            if (newPinCode == null || newPinCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "New PIN code must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = memoService.setNewPin(userId, newPinCode.trim());
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to set PIN code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verify the current PIN code.
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Verification result
     */
    @PostMapping("/verify-current-pin")
    public ResponseEntity<Map<String, Object>> verifyCurrentPin(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String currentPin = (String) pinData.get("currentPin");
            
            if (currentPin == null || currentPin.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Current PIN code must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = memoService.verifyCurrentPin(userId, currentPin.trim());
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "PIN code verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check PIN code status.
     * @param authHeader Authorization header
     * @return PIN code status
     */
    @GetMapping("/pin-status")
    public ResponseEntity<Map<String, Object>> checkPinStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.checkPinCodeStatus(userId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("hasPin", result.get("hasPin"));
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to check PIN code status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get encrypted memo content.
     * @param memoId Memo ID
     * @param pinData PIN code payload
     * @param authHeader Authorization header
     * @return Memo content
     */
    @PostMapping("/{memoId}/content")
    public ResponseEntity<Map<String, Object>> getEncryptedMemoContent(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String pinCode = (String) pinData.get("pinCode");
            
            if (pinCode == null || pinCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "PIN code must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = memoService.getEncryptedMemoContent(userId, memoId, pinCode.trim());
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("content", result.get("content"));
                response.put("message", result.get("message"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get memo content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Search memos.
     * @param keyword Search keyword
     * @param authHeader Authorization header
     * @return Search result
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMemos(
            @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.searchMemos(userId, keyword);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("memos", result.get("memos"));
                response.put("keyword", result.get("keyword"));
                response.put("totalCount", result.get("totalCount"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to search memos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get memo statistics.
     * @param authHeader Authorization header
     * @return Statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMemoStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
    // JWT validation
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            Map<String, Object> result = memoService.getMemoStatistics(userId);
            
            if ((Boolean) result.get("success")) {
                response.put("success", true);
                response.put("statistics", result.get("statistics"));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.get("message"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 