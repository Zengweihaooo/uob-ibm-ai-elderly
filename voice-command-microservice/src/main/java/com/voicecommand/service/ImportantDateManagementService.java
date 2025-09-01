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
 * Important Date Management Service
 * Responsible for calling backend Important Date APIs
 */
@Service
public class ImportantDateManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(ImportantDateManagementService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String BACKEND_BASE_URL = "http://localhost:8080";
    private static final String IMPORTANT_DATE_ENDPOINT = "/api/important-dates/add";
    
    /**
     * Add important date
     * 
     * @param importantDateData important date data
     * @return add result
     */
    public ImportantDateResponse addImportantDate(Map<String, Object> importantDateData) {
        try {
            log.info("Start adding important date, data: {}", importantDateData);
            
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Set auth header (use default user token)
            String userToken = "user-token-1-" + System.currentTimeMillis();
            headers.set("Authorization", "Bearer " + userToken);
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", 1L);
            requestBody.put("title", importantDateData.get("title"));
            requestBody.put("date", importantDateData.get("date"));
            requestBody.put("type", importantDateData.get("type"));
            requestBody.put("description", importantDateData.get("description"));
            requestBody.put("enabled", true);
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Send POST request
            String url = BACKEND_BASE_URL + IMPORTANT_DATE_ENDPOINT;
            log.info("Sending request to: {}", url);
            
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
                    log.info("Important date added successfully: {}", responseBody.get("message"));
                    return ImportantDateResponse.builder()
                        .success(true)
                        .message(responseBody.get("message").toString())
                        .importantDateId(extractImportantDateId(responseBody))
                        .build();
                } else {
                    log.error("Failed to add important date: {}", responseBody.get("message"));
                    return ImportantDateResponse.builder()
                        .success(false)
                        .message(responseBody.get("message").toString())
                        .build();
                }
            } else {
                log.error("Failed to add important date, HTTP status: {}", response.getStatusCode());
                return ImportantDateResponse.builder()
                    .success(false)
                    .message("HTTP request failed, status: " + response.getStatusCode())
                    .build();
            }
            
        } catch (Exception e) {
            log.error("Exception occurred while adding important date: {}", e.getMessage(), e);
            return ImportantDateResponse.builder()
                .success(false)
                .message("Failed to add important date: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Extract important date ID from response
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
            log.warn("Failed to extract important date ID: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Important date response class
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
