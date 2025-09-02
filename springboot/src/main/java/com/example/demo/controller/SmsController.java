package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.SmsService;

/**
 * SMS Controller - test and manage SMS sending features.
 *
 * Provides REST APIs for sending SMS, querying history, and statistics.
 *
 * Author: Yichen Zhang
 * Version: 1.0
 */
@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {

    @Autowired(required = false)
    private SmsService smsService;

    /**
     * Send a test SMS.
     *
     * @param smsData request body containing phoneNumber and message
     * @return send result
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSMS(@RequestBody Map<String, Object> smsData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS service disabled");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) smsData.get("phoneNumber");
            String message = (String) smsData.get("message");
            String messageType = (String) smsData.getOrDefault("messageType", "TEST");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送SMS
            Map<String, Object> smsResult = smsService.sendSMS(phoneNumber, message, messageType);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "SMS send request processed");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Failed to send SMS: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Send a health alert SMS.
     *
     * @param alertData request body with phoneNumber and healthData
     * @return send result
     */
    @PostMapping("/health-alert")
    public ResponseEntity<Map<String, Object>> sendHealthAlert(@RequestBody Map<String, Object> alertData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS service disabled");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) alertData.get("phoneNumber");
            String healthData = (String) alertData.get("healthData");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (healthData == null || healthData.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Health data must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送健康警报SMS
            Map<String, Object> smsResult = smsService.sendHealthAlertSMS(phoneNumber, healthData);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "Health alert SMS request processed");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Failed to send health alert SMS: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Send an emergency SMS.
     *
     * @param emergencyData request body with phoneNumber and emergencyInfo
     * @return send result
     */
    @PostMapping("/emergency")
    public ResponseEntity<Map<String, Object>> sendEmergency(@RequestBody Map<String, Object> emergencyData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS service disabled");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) emergencyData.get("phoneNumber");
            String emergencyInfo = (String) emergencyData.get("emergencyInfo");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Phone number must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (emergencyInfo == null || emergencyInfo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Emergency info must not be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送紧急SMS
            Map<String, Object> smsResult = smsService.sendEmergencySMS(phoneNumber, emergencyInfo);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "Emergency SMS request processed");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Failed to send emergency SMS: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get SMS send history.
     *
     * @return history list
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSmsHistory() {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS service disabled");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> history = smsService.getSmsHistory();
            
            response.put("success", true);
            response.put("message", "SMS history retrieved successfully");
            response.put("history", history);
            response.put("total", history.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
        response.put("success", false);
        response.put("message", "Failed to retrieve SMS history: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get SMS statistics.
     *
     * @return statistics map
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSmsStatistics() {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS service disabled");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = smsService.getSmsStatistics();
            
            response.put("success", true);
            response.put("message", "SMS statistics retrieved successfully");
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to retrieve SMS statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}