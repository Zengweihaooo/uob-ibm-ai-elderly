package com.voicecommand.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

/**
 * 重要日期管理服务
 * 负责调用后端的重要日期API
 */
@Service
public class ImportantDateManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(ImportantDateManagementService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String BACKEND_BASE_URL = "http://localhost:8080";
    private static final String IMPORTANT_DATE_ENDPOINT = "/api/important-dates/add";
    
    /**
     * 添加重要日期
     * 
     * @param importantDateData 重要日期数据
     * @return 添加结果
     */
    public ImportantDateResponse addImportantDate(Map<String, Object> importantDateData) {
        try {
            log.info("开始添加重要日期，数据: {}", importantDateData);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 设置认证头（使用默认用户token）
            String userToken = "user-token-1-" + System.currentTimeMillis();
            headers.set("Authorization", "Bearer " + userToken);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", 1L);
            requestBody.put("title", importantDateData.get("title"));
            requestBody.put("date", importantDateData.get("date"));
            requestBody.put("type", importantDateData.get("type"));
            requestBody.put("description", importantDateData.get("description"));
            requestBody.put("enabled", true);
            
            // 创建HTTP实体
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // 发送POST请求
            String url = BACKEND_BASE_URL + IMPORTANT_DATE_ENDPOINT;
            log.info("发送请求到: {}", url);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                boolean success = Boolean.TRUE.equals(responseBody.get("success"));
                
                if (success) {
                    log.info("重要日期添加成功: {}", responseBody.get("message"));
                    return ImportantDateResponse.builder()
                        .success(true)
                        .message(responseBody.get("message").toString())
                        .importantDateId(extractImportantDateId(responseBody))
                        .build();
                } else {
                    log.error("重要日期添加失败: {}", responseBody.get("message"));
                    return ImportantDateResponse.builder()
                        .success(false)
                        .message(responseBody.get("message").toString())
                        .build();
                }
            } else {
                log.error("重要日期添加失败，HTTP状态码: {}", response.getStatusCode());
                return ImportantDateResponse.builder()
                    .success(false)
                    .message("HTTP请求失败，状态码: " + response.getStatusCode())
                    .build();
            }
            
        } catch (Exception e) {
            log.error("添加重要日期时发生异常: {}", e.getMessage(), e);
            return ImportantDateResponse.builder()
                .success(false)
                .message("添加重要日期失败: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * 从响应中提取重要日期ID
     */
    private String extractImportantDateId(Map<String, Object> responseBody) {
        try {
            if (responseBody.containsKey("importantDate")) {
                Object importantDateObj = responseBody.get("importantDate");
                if (importantDateObj instanceof Map) {
                    Map<String, Object> importantDate = (Map<String, Object>) importantDateObj;
                    if (importantDate.containsKey("id")) {
                        return importantDate.get("id").toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取重要日期ID失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 重要日期响应类
     */
    public static class ImportantDateResponse {
        private boolean success;
        private String message;
        private String importantDateId;
        
        public ImportantDateResponse() {}
        
        public ImportantDateResponse(boolean success, String message, String importantDateId) {
            this.success = success;
            this.message = message;
            this.importantDateId = importantDateId;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private boolean success;
            private String message;
            private String importantDateId;
            
            public Builder success(boolean success) {
                this.success = success;
                return this;
            }
            
            public Builder message(String message) {
                this.message = message;
                return this;
            }
            
            public Builder importantDateId(String importantDateId) {
                this.importantDateId = importantDateId;
                return this;
            }
            
            public ImportantDateResponse build() {
                return new ImportantDateResponse(success, message, importantDateId);
            }
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getImportantDateId() { return importantDateId; }
        public void setImportantDateId(String importantDateId) { this.importantDateId = importantDateId; }
        
        @Override
        public String toString() {
            return "ImportantDateResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", importantDateId='" + importantDateId + '\'' +
                '}';
        }
    }
}
