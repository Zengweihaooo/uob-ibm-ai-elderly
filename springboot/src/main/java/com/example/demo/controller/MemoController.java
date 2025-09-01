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
 * 备忘录控制器
 * 提供备忘录相关的REST API接口
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
     * 创建新备忘录
     * @param memoData 备忘录数据
     * @param authHeader 授权头
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createMemo(
            @RequestBody Map<String, Object> memoData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "创建备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取用户的所有备忘录
     * @param authHeader 授权头
     * @return 备忘录列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserMemos(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "获取备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取备忘录
     * @param memoId 备忘录ID
     * @param authHeader 授权头
     * @return 备忘录信息
     */
    @GetMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> getMemoById(
            @PathVariable Long memoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "获取备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新备忘录
     * @param memoId 备忘录ID
     * @param memoData 更新的备忘录数据
     * @param authHeader 授权头
     * @return 更新结果
     */
    @PutMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> updateMemo(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> memoData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "更新备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除备忘录
     * @param memoId 备忘录ID
     * @param authHeader 授权头
     * @return 删除结果
     */
    @DeleteMapping("/{memoId}")
    public ResponseEntity<Map<String, Object>> deleteMemo(
            @PathVariable Long memoId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "删除备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证PIN码
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 验证结果
     */
    @PostMapping("/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyPinCode(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
                response.put("message", "PIN码不能为空");
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
            response.put("message", "PIN码验证失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证特定备忘录的PIN码
     * @param memoId 备忘录ID
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 验证结果
     */
    @PostMapping("/{memoId}/verify-pin")
    public ResponseEntity<Map<String, Object>> verifyMemoPinCode(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
                response.put("message", "PIN码不能为空");
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
            response.put("message", "PIN码验证失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 设置用户PIN码（首次设置或重新设置）
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 设置结果
     */
    @PostMapping("/set-pin")
    public ResponseEntity<Map<String, Object>> setPinCode(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
        Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
        if (userId == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        try {
            String oldPinCode = (String) pinData.get("oldPinCode");
            String newPinCode = (String) pinData.get("newPinCode");
            String pinCode = (String) pinData.get("pinCode"); // 兼容旧版本
            
            // 如果提供了oldPinCode和newPinCode，使用新的验证方式
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
            
            // 兼容旧版本：只提供pinCode的情况
            if (pinCode != null && !pinCode.trim().isEmpty()) {
                Map<String, Object> result = memoService.setPinCode(userId, pinCode.trim());
                
                if ((Boolean) result.get("success")) {
                    response.put("success", true);
                    response.put("message", result.get("message"));
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", result.get("message"));
                    // 如果需要验证旧PIN，返回特殊状态码
                    if (result.containsKey("needOldPin") && (Boolean) result.get("needOldPin")) {
                        response.put("needOldPin", true);
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                    }
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            response.put("success", false);
            response.put("message", "PIN码参数不完整");
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "PIN码设置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 设置新PIN码（已验证当前PIN）
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 设置结果
     */
    @PostMapping("/set-new-pin")
    public ResponseEntity<Map<String, Object>> setNewPin(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
                response.put("message", "新PIN码不能为空");
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
            response.put("message", "PIN码设置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 验证当前PIN码
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 验证结果
     */
    @PostMapping("/verify-current-pin")
    public ResponseEntity<Map<String, Object>> verifyCurrentPin(
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
                response.put("message", "当前PIN码不能为空");
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
            response.put("message", "PIN码验证失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查PIN码状态
     * @param authHeader 授权头
     * @return PIN码状态
     */
    @GetMapping("/pin-status")
    public ResponseEntity<Map<String, Object>> checkPinStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "检查PIN码状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取加密备忘录内容
     * @param memoId 备忘录ID
     * @param pinData PIN码数据
     * @param authHeader 授权头
     * @return 备忘录内容
     */
    @PostMapping("/{memoId}/content")
    public ResponseEntity<Map<String, Object>> getEncryptedMemoContent(
            @PathVariable Long memoId,
            @RequestBody Map<String, Object> pinData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
                response.put("message", "PIN码不能为空");
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
            response.put("message", "获取备忘录内容失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 搜索备忘录
     * @param keyword 搜索关键词
     * @param authHeader 授权头
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMemos(
            @RequestParam String keyword,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "搜索备忘录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取备忘录统计信息
     * @param authHeader 授权头
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMemoStatistics(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        // JWT验证
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
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 