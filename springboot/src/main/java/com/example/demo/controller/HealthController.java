package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.example.demo.util.JwtUtil;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private HealthService healthService;
    
    @Autowired
    private UserContextUtil userContextUtil;
    
    @Autowired
    private JwtUtil jwtUtil;

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
            // 验证token有效性
            Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }
            
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
    
    // ========== JWT测试相关接口 ==========
    
    /**
     * JWT测试端点 - 验证JWT token
     */
    @GetMapping("/jwt-test")
    public ResponseEntity<Map<String, Object>> testJwt(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authorization header required");
            response.put("debug", "No Authorization header or invalid format");
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            String token = authHeader.substring(7);
            
            // 调试信息
            String debugInfo = userContextUtil.debugTokenExtraction(authHeader);
            
            // 验证token
            if (userContextUtil.isValidAuthHeader(authHeader)) {
                Long userId = userContextUtil.getUserIdFromAuthHeader(authHeader);
                String email = userContextUtil.getEmailFromAuthHeader(authHeader);
                
                response.put("success", true);
                response.put("message", "JWT token is valid");
                response.put("userId", userId);
                response.put("email", email);
                response.put("debug", debugInfo);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid JWT token");
                response.put("debug", debugInfo);
                return ResponseEntity.status(401).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing JWT token");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
               /**
            * 生成真实的JWT token
            */
           @PostMapping("/generate-test-token")
           public ResponseEntity<Map<String, Object>> generateTestToken(
                   @RequestBody Map<String, Object> request) {
               
               Map<String, Object> response = new HashMap<>();
               
               try {
                   Long userId = Long.valueOf(request.get("userId").toString());
                   String email = (String) request.get("email");
                   
                   if (userId == null || email == null) {
                       response.put("success", false);
                       response.put("message", "userId and email are required");
                       return ResponseEntity.badRequest().body(response);
                   }
                   
                   // 生成真实的JWT token
                   String jwtToken = jwtUtil.generateToken(userId, email);
                   
                   response.put("success", true);
                   response.put("token", jwtToken);
                   response.put("userId", userId);
                   response.put("email", email);
                   response.put("message", "Real JWT token generated successfully");
                   
                   return ResponseEntity.ok(response);
                   
               } catch (Exception e) {
                   response.put("success", false);
                   response.put("message", "Error generating JWT token: " + e.getMessage());
                   return ResponseEntity.internalServerError().body(response);
               }
           }
    
    // ========== JWT测试相关接口结束 ==========
    
    // ========== 健康功能与Email集成API ==========
    
    /**
     * 发送每日健康检查提醒
     */
    @PostMapping("/reminder/daily")
    public ResponseEntity<Map<String, Object>> sendDailyHealthReminder(
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

            boolean success = healthService.sendDailyHealthCheckReminder(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Daily health check reminder sent successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send daily health check reminder");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending daily health check reminder: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 发送健康数据报告
     */
    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> sendHealthReport(
            @RequestBody Map<String, String> reportRequest,
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

            String reportType = reportRequest.get("reportType"); // daily, weekly, monthly
            if (reportType == null) {
                response.put("success", false);
                response.put("message", "Report type is required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = healthService.sendHealthReport(userId, reportType);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Health report sent successfully");
                response.put("reportType", reportType);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send health report");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending health report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 发送健康趋势分析
     */
    @PostMapping("/trend-analysis")
    public ResponseEntity<Map<String, Object>> sendHealthTrendAnalysis(
            @RequestBody Map<String, Object> analysisRequest,
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

            Integer days = (Integer) analysisRequest.get("days");
            if (days == null || days <= 0) {
                days = 7; // 默认7天
            }

            boolean success = healthService.sendHealthTrendAnalysis(userId, days);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Health trend analysis sent successfully");
                response.put("analysisDays", days);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send health trend analysis");
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error sending health trend analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取健康数据统计信息（增强版）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getHealthStatistics(
            @RequestParam(required = false) String period, // today, week, month
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

            // 根据时间段获取数据
            List<HealthRecord> records;
            if ("week".equals(period)) {
                LocalDateTime startDate = LocalDateTime.now().minusWeeks(1);
                String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String endIso = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                records = healthService.history(userId, startIso, endIso);
            } else if ("month".equals(period)) {
                LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
                String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String endIso = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                records = healthService.history(userId, startIso, endIso);
            } else {
                // 默认今天 - 修复时间范围计算
                LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
                LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(23, 59, 59, 999999999);
                String startIso = startOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                String endIso = endOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                records = healthService.history(userId, startIso, endIso);
            }
            
            // 计算统计信息
            long totalRecords = records.size();
            long abnormalRecords = records.stream()
                .filter(record -> healthService.isAbnormal(record.getType(), record.getValue()))
                .count();
            
            // 按类型分组统计
            Map<String, Long> typeCount = new HashMap<>();
            Map<String, Long> abnormalTypeCount = new HashMap<>();
            
            for (HealthRecord record : records) {
                typeCount.merge(record.getType(), 1L, Long::sum);
                if (healthService.isAbnormal(record.getType(), record.getValue())) {
                    abnormalTypeCount.merge(record.getType(), 1L, Long::sum);
                }
            }
            
            response.put("success", true);
            response.put("period", period != null ? period : "today");
            response.put("totalRecords", totalRecords);
            response.put("abnormalRecords", abnormalRecords);
            response.put("normalRecords", totalRecords - abnormalRecords);
            response.put("typeCount", typeCount);
            response.put("abnormalTypeCount", abnormalTypeCount);
            response.put("abnormalRate", totalRecords > 0 ? (double) abnormalRecords / totalRecords : 0.0);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching health statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取健康趋势数据
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getHealthTrends(
            @RequestParam(defaultValue = "7") int days,
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

            // 获取指定天数的数据
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endIso = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            List<HealthRecord> records = healthService.history(userId, startIso, endIso);
            
            // 按日期分组
            Map<String, List<HealthRecord>> recordsByDate = new HashMap<>();
            for (HealthRecord record : records) {
                String dateKey = record.getRecordTime().toLocalDate().toString();
                recordsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(record);
            }
            
            // 生成趋势数据
            List<Map<String, Object>> trendData = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDateTime date = endDate.minusDays(i);
                String dateKey = date.toLocalDate().toString();
                List<HealthRecord> dayRecords = recordsByDate.getOrDefault(dateKey, new ArrayList<>());
                
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateKey);
                dayData.put("totalRecords", dayRecords.size());
                dayData.put("abnormalRecords", dayRecords.stream()
                    .filter(record -> healthService.isAbnormal(record.getType(), record.getValue()))
                    .count());
                
                // 按类型统计
                Map<String, Long> typeCount = new HashMap<>();
                for (HealthRecord record : dayRecords) {
                    typeCount.merge(record.getType(), 1L, Long::sum);
                }
                dayData.put("typeCount", typeCount);
                
                trendData.add(dayData);
            }
            
            response.put("success", true);
            response.put("days", days);
            response.put("trendData", trendData);
            response.put("totalRecords", records.size());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching health trends: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== 健康功能与Email集成API结束 ==========
}
