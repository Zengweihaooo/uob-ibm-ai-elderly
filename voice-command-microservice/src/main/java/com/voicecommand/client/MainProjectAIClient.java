package com.voicecommand.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class MainProjectAIClient {
    
    private static final Logger log = LoggerFactory.getLogger(MainProjectAIClient.class);
    
    @Value("${main.project.base.url:http://localhost:8080}")
    private String mainProjectBaseUrl;
    
    private final RestTemplate restTemplate;
    
    public MainProjectAIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public Map<String, Object> chatWithGemini(Map<String, Object> request) {
        try {
            String url = mainProjectBaseUrl + "/api/gemini/chat";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Calling main project AI service: {}", url);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            log.info("Main project AI response received: {}", response.getBody());
            
            return response.getBody();
            
        } catch (Exception e) {
            log.error("Failed to call main project AI service", e);
            throw new RuntimeException("Main project AI service unavailable", e);
        }
    }
    
    public boolean isMainProjectAIAvailable() {
        try {
            String url = mainProjectBaseUrl + "/api/gemini/status";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getBody() != null) {
                String status = (String) response.getBody().get("status");
                return "ready".equals(status);
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Main project AI service not available: {}", e.getMessage());
            return false;
        }
    }
}
