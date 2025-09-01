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
 * SMS控制器 - 用于测试和管理SMS发送功能
 * 
 * 提供SMS发送、历史查询和统计功能的REST API
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*")
public class SmsController {

    @Autowired(required = false)
    private SmsService smsService;

    /**
     * 发送测试SMS
     * 
     * @param smsData 包含电话号码和消息内容的请求数据
     * @return SMS发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSMS(@RequestBody Map<String, Object> smsData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS服务已禁用");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) smsData.get("phoneNumber");
            String message = (String) smsData.get("message");
            String messageType = (String) smsData.getOrDefault("messageType", "TEST");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "电话号码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送SMS
            Map<String, Object> smsResult = smsService.sendSMS(phoneNumber, message, messageType);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "SMS发送请求已处理");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "SMS发送失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 发送健康警报SMS
     * 
     * @param alertData 包含电话号码和健康数据的请求数据
     * @return SMS发送结果
     */
    @PostMapping("/health-alert")
    public ResponseEntity<Map<String, Object>> sendHealthAlert(@RequestBody Map<String, Object> alertData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS服务已禁用");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) alertData.get("phoneNumber");
            String healthData = (String) alertData.get("healthData");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "电话号码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (healthData == null || healthData.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "健康数据不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送健康警报SMS
            Map<String, Object> smsResult = smsService.sendHealthAlertSMS(phoneNumber, healthData);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "健康警报SMS发送请求已处理");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "健康警报SMS发送失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 发送紧急SMS
     * 
     * @param emergencyData 包含电话号码和紧急信息的请求数据
     * @return SMS发送结果
     */
    @PostMapping("/emergency")
    public ResponseEntity<Map<String, Object>> sendEmergency(@RequestBody Map<String, Object> emergencyData) {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS服务已禁用");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = (String) emergencyData.get("phoneNumber");
            String emergencyInfo = (String) emergencyData.get("emergencyInfo");
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "电话号码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (emergencyInfo == null || emergencyInfo.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "紧急信息不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送紧急SMS
            Map<String, Object> smsResult = smsService.sendEmergencySMS(phoneNumber, emergencyInfo);
            
            response.put("success", smsResult.get("success"));
            response.put("message", "紧急SMS发送请求已处理");
            response.put("smsResult", smsResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "紧急SMS发送失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取SMS发送历史
     * 
     * @return SMS发送历史列表
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getSmsHistory() {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS服务已禁用");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> history = smsService.getSmsHistory();
            
            response.put("success", true);
            response.put("message", "SMS历史记录获取成功");
            response.put("history", history);
            response.put("total", history.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取SMS历史记录失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 获取SMS统计信息
     * 
     * @return SMS统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSmsStatistics() {
        if (smsService == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "SMS服务已禁用");
            return ResponseEntity.status(503).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = smsService.getSmsStatistics();
            
            response.put("success", true);
            response.put("message", "SMS统计信息获取成功");
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取SMS统计信息失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}