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
 * Schedule Management Service
 * Used to call the main backend's schedule management API
 */
@Service
@Slf4j
public class ScheduleManagementService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    // Main backend service's schedule management endpoint
    private static final String SCHEDULE_ADD_URL = "http://localhost:8080/api/schedule/activity";
    
    /**
     * Add schedule arrangement
     * 
     * @param scheduleData Schedule data
     * @param userId User ID
     * @return Add result
     */
    public ScheduleResponse addSchedule(Map<String, Object> scheduleData, String userId) {
        if (scheduleData == null || scheduleData.isEmpty()) {
            log.warn("Schedule data is empty, cannot add");
            return ScheduleResponse.builder()
                .success(false)
                .errorMessage("Schedule data is empty")
                .build();
        }
        
        log.info("Starting to add schedule: scheduleData={}, userId={}", scheduleData, userId);
        
        try {
            // Build request headers (simulate authentication)
            String authToken = "user-token-" + userId + "-" + System.currentTimeMillis();
            
            // Create request headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + authToken);
            
            // Create request entity
            org.springframework.http.HttpEntity<Map<String, Object>> requestEntity = 
                new org.springframework.http.HttpEntity<>(scheduleData, headers);
            
            // Call main backend service to add schedule
            ResponseEntity<Map> response = restTemplate.postForEntity(
                SCHEDULE_ADD_URL, 
                requestEntity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    log.info("Schedule added successfully: {}", responseBody.get("message"));
                    
                    return ScheduleResponse.builder()
                        .success(true)
                        .message((String) responseBody.get("message"))
                        .scheduleId(responseBody.get("activity") != null ? 
                            String.valueOf(((Map) responseBody.get("activity")).get("id")) : null)
                        .build();
                } else {
                    log.warn("Schedule addition failed: {}", responseBody.get("message"));
                    
                    return ScheduleResponse.builder()
                        .success(false)
                        .errorMessage((String) responseBody.get("message"))
                        .build();
                }
            }
            
        } catch (Exception e) {
            log.error("Error occurred while adding schedule", e);
        }
        
        log.info("Schedule addition failed");
        return ScheduleResponse.builder()
            .success(false)
            .errorMessage("Failed to add schedule")
            .build();
    }
    
    /**
     * Schedule response class
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
