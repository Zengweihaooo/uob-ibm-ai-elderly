package com.voicecommand.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.HashMap;

/**
 * 日程管理服务
 * 用于调用主后端的日程管理API
 */
@Service
@Slf4j
public class ScheduleManagementService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    // 主后端服务的日程管理端点
    private static final String SCHEDULE_ADD_URL = "http://localhost:8080/api/schedule/activity";
    
    /**
     * 添加日程安排
     * 
     * @param scheduleData 日程数据
     * @param userId 用户ID
     * @return 添加结果
     */
    public ScheduleResponse addSchedule(Map<String, Object> scheduleData, String userId) {
        if (scheduleData == null || scheduleData.isEmpty()) {
            log.warn("日程数据为空，无法添加");
            return ScheduleResponse.builder()
                .success(false)
                .errorMessage("日程数据为空")
                .build();
        }
        
        log.info("开始添加日程: scheduleData={}, userId={}", scheduleData, userId);
        
        try {
            // 构建请求头（模拟认证）
            String authToken = "user-token-" + userId + "-" + System.currentTimeMillis();
            
            // 创建请求头
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            
            // 创建请求实体
            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(scheduleData, headers);
            
            // 调用主后端服务添加日程
            ResponseEntity<Map> response = restTemplate.postForEntity(
                SCHEDULE_ADD_URL, 
                requestEntity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    log.info("日程添加成功: {}", responseBody.get("message"));
                    
                    return ScheduleResponse.builder()
                        .success(true)
                        .message((String) responseBody.get("message"))
                        .scheduleId(responseBody.get("activity") != null ? 
                            String.valueOf(((Map) responseBody.get("activity")).get("id")) : null)
                        .build();
                } else {
                    log.warn("日程添加失败: {}", responseBody.get("message"));
                    
                    return ScheduleResponse.builder()
                        .success(false)
                        .errorMessage((String) responseBody.get("message"))
                        .build();
                }
            }
            
        } catch (Exception e) {
            log.error("添加日程时发生错误", e);
        }
        
        log.info("日程添加失败");
        return ScheduleResponse.builder()
            .success(false)
            .errorMessage("添加日程失败")
            .build();
    }
    
    /**
     * 日程响应类
     */
    public static class ScheduleResponse {
        private boolean success;
        private String message;
        private String scheduleId;
        private String errorMessage;
        
        // Builder pattern
        public static ScheduleResponseBuilder builder() {
            return new ScheduleResponseBuilder();
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getScheduleId() { return scheduleId; }
        public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        // Builder class
        public static class ScheduleResponseBuilder {
            private ScheduleResponse response = new ScheduleResponse();
            
            public ScheduleResponseBuilder success(boolean success) {
                response.success = success;
                return this;
            }
            
            public ScheduleResponseBuilder message(String message) {
                response.message = message;
                return this;
            }
            
            public ScheduleResponseBuilder scheduleId(String scheduleId) {
                response.scheduleId = scheduleId;
                return this;
            }
            
            public ScheduleResponseBuilder errorMessage(String errorMessage) {
                response.errorMessage = errorMessage;
                return this;
            }
            
            public ScheduleResponse build() {
                return response;
            }
        }
    }
}
