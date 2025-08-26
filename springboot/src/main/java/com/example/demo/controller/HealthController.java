package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.HealthRecord;
import com.example.demo.service.HealthService;
import com.example.demo.util.UserContextUtil;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private HealthService healthService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> addHealthRecord(
            @RequestBody Map<String, String> recordData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // 从JWT token中提取用户ID
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            String type = recordData.get("type"); // bloodPressure, bloodSugar, steps
            String value = recordData.get("value");

            if (type == null || value == null) {
                response.put("success", false);
                response.put("message", "Type and value are required");
                return ResponseEntity.badRequest().body(response);
            }

            HealthRecord record = healthService.addHealthRecord(userId, type, value);

            // Check for abnormal values and send email notification
            boolean isAbnormal = healthService.checkAbnormalAndNotify(type, value, userId);
            
            if (isAbnormal) {
                response.put("alert", "Abnormal value detected! Email notification has been sent to emergency contact.");
                response.put("abnormal", true);
            } else {
                response.put("abnormal", false);
            }

            response.put("success", true);
            response.put("record", record);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to record health data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayHealthRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<HealthRecord> records = healthService.getTodayRecords(userId);
            response.put("success", true);
            response.put("records", records);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching today's health data");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取所有健康记录
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAllHealthRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<HealthRecord> records = healthService.getAll();
            response.put("success", true);
            response.put("records", records);
            response.put("count", records.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching all health records: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get health data statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHealthStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }
            List<HealthRecord> todayRecords = healthService.getTodayRecords(userId);
            
            // Count abnormal records for today
            long abnormalCount = todayRecords.stream()
                .filter(record -> healthService.isAbnormal(record.getType(), record.getValue()))
                .count();
            
            response.put("success", true);
            response.put("totalRecords", todayRecords.size());
            response.put("abnormalCount", abnormalCount);
            response.put("normalCount", todayRecords.size() - abnormalCount);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching health statistics");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== 新增共享功能相关接口 ==========
    
    /**
     * 共享健康记录给家庭成员或医生
     */
    @PostMapping("/share")
    public ResponseEntity<Map<String, Object>> shareHealthRecord(
            @RequestBody Map<String, Object> shareData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            Long recordId = Long.valueOf(shareData.get("recordId").toString());
            Long sharedWithUserId = Long.valueOf(shareData.get("sharedWithUserId").toString());
            String sharedWithRole = (String) shareData.get("sharedWithRole");

            if (recordId == null || sharedWithUserId == null || sharedWithRole == null) {
                response.put("success", false);
                response.put("message", "recordId, sharedWithUserId, and sharedWithRole are required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = healthService.shareHealthRecord(recordId, userId, sharedWithUserId, sharedWithRole);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Health record shared successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to share health record");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to share health record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 取消共享健康记录
     */
    @DeleteMapping("/share/{recordId}")
    public ResponseEntity<Map<String, Object>> unshareHealthRecord(
            @PathVariable Long recordId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            boolean success = healthService.unshareHealthRecord(recordId, userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Health record unshared successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to unshare health record");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to unshare health record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取共享给当前用户的健康记录（供家庭成员/医生查看）
     */
    @GetMapping("/shared-with-me")
    public ResponseEntity<Map<String, Object>> getSharedRecordsForMe(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<HealthRecord> sharedRecords = healthService.getSharedRecordsForUser(userId);
            response.put("success", true);
            response.put("records", sharedRecords);
            response.put("count", sharedRecords.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching shared health records");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取用户共享的所有健康记录
     */
    @GetMapping("/my-shared")
    public ResponseEntity<Map<String, Object>> getMySharedRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<HealthRecord> sharedRecords = healthService.getUserSharedRecords(userId);
            response.put("success", true);
            response.put("records", sharedRecords);
            response.put("count", sharedRecords.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching my shared health records");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取可共享的用户列表（家庭成员和医生）
     */
    @GetMapping("/shareable-users")
    public ResponseEntity<Map<String, Object>> getShareableUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<Map<String, Object>> shareableUsers = healthService.getShareableUsers(userId);
            response.put("success", true);
            response.put("users", shareableUsers);
            response.put("count", shareableUsers.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching shareable users");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== 新增共享功能接口结束 ==========
    
    // ========== 数据库操作相关接口 ==========
    
    /**
     * 添加健康记录到数据库
     */
    @PostMapping("/record-db")
    public ResponseEntity<Map<String, Object>> addRecord(@RequestBody HealthRecord record) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long id = healthService.addRecord(record);
            response.put("id", id);
            response.put("status", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to add record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新健康记录的分享信息
     */
    @PatchMapping("/share/{id}")
    public ResponseEntity<Map<String, Object>> updateShareInfo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> shareData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Boolean shared = (Boolean) shareData.get("shared");
            Long sharedWithUserId = shareData.get("sharedWithUserId") != null ? 
                Long.valueOf(shareData.get("sharedWithUserId").toString()) : null;
            String sharedWithRole = (String) shareData.get("sharedWithRole");
            
            healthService.setShareInfo(id, shared, sharedWithUserId, sharedWithRole);
            response.put("status", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to update share info: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取用户指定时间范围内的健康记录历史
     */
    @GetMapping("/history")
    public ResponseEntity<List<HealthRecord>> getHistory(
            @RequestParam Long userId,
            @RequestParam String start,
            @RequestParam String end) {
        
        try {
            List<HealthRecord> records = healthService.history(userId, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户指定类型和时间范围内的健康记录历史
     */
    @GetMapping("/history/type")
    public ResponseEntity<List<HealthRecord>> getHistoryByType(
            @RequestParam Long userId,
            @RequestParam String type,
            @RequestParam String start,
            @RequestParam String end) {
        
        try {
            List<HealthRecord> records = healthService.historyByType(userId, type, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取用户最新的健康记录
     */
    @GetMapping("/latest")
    public ResponseEntity<HealthRecord> getLatest(@RequestParam Long userId) {
        
        try {
            HealthRecord record = healthService.latest(userId);
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除健康记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRecord(@PathVariable Long id) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            healthService.delete(id);
            response.put("status", "deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete record: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== 数据库操作相关接口结束 ==========
}
