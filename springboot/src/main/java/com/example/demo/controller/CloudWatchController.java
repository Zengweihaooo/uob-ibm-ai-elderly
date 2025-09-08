package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.CloudWatchService;

import java.util.HashMap;
import java.util.Map;

/**
 * CloudWatch Monitoring Controller
 * Provides endpoints for monitoring and metrics management
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
@RequestMapping("/api/cloudwatch")
@Profile("aws")
public class CloudWatchController {
    
    @Autowired
    private CloudWatchService cloudWatchService;
    
    /**
     * Send custom metric
     */
    @PostMapping("/metric")
    public Map<String, Object> sendMetric(
            @RequestParam String metricName,
            @RequestParam double value,
            @RequestParam(defaultValue = "Count") String unit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            cloudWatchService.putMetric(metricName, value, unit);
            response.put("success", true);
            response.put("message", "Metric sent successfully");
            response.put("metric", metricName);
            response.put("value", value);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send metric: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Record health metric
     */
    @PostMapping("/health-metric")
    public Map<String, Object> recordHealthMetric(
            @RequestParam String type,
            @RequestParam boolean isAbnormal) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            cloudWatchService.recordHealthMetric(type, isAbnormal);
            response.put("success", true);
            response.put("message", "Health metric recorded");
            response.put("type", type);
            response.put("abnormal", isAbnormal);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to record health metric: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Record user activity
     */
    @PostMapping("/user-activity")
    public Map<String, Object> recordUserActivity(@RequestParam String activity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            cloudWatchService.recordUserActivity(activity);
            response.put("success", true);
            response.put("message", "User activity recorded");
            response.put("activity", activity);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to record user activity: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Get metric statistics
     */
    @GetMapping("/statistics")
    public Map<String, Object> getMetricStatistics(
            @RequestParam String metricName,
            @RequestParam(defaultValue = "24") int hours) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object stats = cloudWatchService.getMetricStatistics(metricName, hours);
            
            if (stats != null) {
                response.put("success", true);
                response.put("metricName", metricName);
                response.put("data", stats);
                response.put("message", "Statistics retrieved successfully");
            } else {
                response.put("success", false);
                response.put("message", "No data available for metric: " + metricName);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get statistics: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Create health monitoring alarm
     */
    @PostMapping("/alarm/health")
    public Map<String, Object> createHealthAlarm() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            cloudWatchService.createHealthAlarm();
            response.put("success", true);
            response.put("message", "Health monitoring alarm created successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create alarm: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * CloudWatch health check
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test metric sending
            cloudWatchService.putMetric("CloudWatch.HealthCheck", 1.0, "Count");
            
            response.put("success", true);
            response.put("message", "CloudWatch service is healthy");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "CloudWatch service error: " + e.getMessage());
        }
        
        return response;
    }
}
