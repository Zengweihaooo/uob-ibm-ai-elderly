package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.pojo.FamilyContact;
import com.example.demo.service.FamilyService;
import com.example.demo.util.JwtUtil;

/**
 * REST Controller for family contact management
 * 
 * This controller handles family contact operations including adding, updating,
 * deleting contacts, and sending messages to family members.
 * 
 * @author Yichen Zhang
 * @version 1.0
 */
@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = "*")
public class FamilyController {

    @Autowired
    private FamilyService familyService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Add a new family contact
     * 
     * @param contactData Contact information from request body
     * @param authHeader Authorization header
     * @return Created contact or error message
     */
    @PostMapping("/contacts")
    public ResponseEntity<Map<String, Object>> addFamilyContact(
            @RequestBody Map<String, Object> contactData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            // Extract contact data
            String name = (String) contactData.get("name");
            String phoneNumber = (String) contactData.get("phoneNumber");
            String email = (String) contactData.get("email");
            String relationship = (String) contactData.get("relationship");
            String notificationPreference = (String) contactData.getOrDefault("notificationPreference", "ALL");
            Boolean isEmergencyContact = (Boolean) contactData.getOrDefault("isEmergencyContact", false);
            String notes = (String) contactData.get("notes");

            // Validate required fields
            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Contact name is required");
                return ResponseEntity.badRequest().body(response);
            }

            if ((phoneNumber == null || phoneNumber.trim().isEmpty()) && 
                (email == null || email.trim().isEmpty())) {
                response.put("success", false);
                response.put("message", "Either phone number or email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Create and save contact
            FamilyContact contact = familyService.addFamilyContact(
                userId, name, phoneNumber, email, relationship, isEmergencyContact
            );

            response.put("success", true);
            response.put("message", "Family contact added successfully");
            response.put("contact", contact);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add family contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all family contacts for a user
     * 
     * @param authHeader Authorization header
     * @return List of family contacts
     */
    @GetMapping("/contacts")
    public ResponseEntity<Map<String, Object>> getFamilyContacts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<FamilyContact> contacts = familyService.getFamilyContacts(userId);
            
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("totalCount", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching family contacts: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get a specific family contact by ID
     * 
     * @param contactId Contact ID
     * @param authHeader Authorization header
     * @return Contact details
     */
    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> getFamilyContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            FamilyContact contact = familyService.getFamilyContact(userId, contactId);
            
            if (contact == null) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("contact", contact);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update a family contact
     * 
     * @param contactId Contact ID
     * @param contactData Updated contact data
     * @param authHeader Authorization header
     * @return Updated contact
     */
    @PutMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> updateFamilyContact(
            @PathVariable Long contactId,
            @RequestBody Map<String, Object> contactData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            FamilyContact updatedContact = familyService.updateFamilyContact(
                userId, contactId, contactData
            );

            if (updatedContact == null) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("message", "Contact updated successfully");
            response.put("contact", updatedContact);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid data: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Delete a family contact
     * 
     * @param contactId Contact ID
     * @param authHeader Authorization header
     * @return Success message
     */
    @DeleteMapping("/contacts/{contactId}")
    public ResponseEntity<Map<String, Object>> deleteFamilyContact(
            @PathVariable Long contactId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            boolean deleted = familyService.deleteFamilyContact(userId, contactId);

            if (!deleted) {
                response.put("success", false);
                response.put("message", "Contact not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("message", "Contact deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete contact: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Send message to family contact
     * 
     * @param contactId Contact ID
     * @param messageData Message content
     * @param authHeader Authorization header
     * @return Message sending result
     */
    @PostMapping("/contacts/{contactId}/message")
    public ResponseEntity<Map<String, Object>> sendMessageToFamily(
            @PathVariable Long contactId,
            @RequestBody Map<String, Object> messageData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            String message = (String) messageData.get("message");
            String messageType = (String) messageData.getOrDefault("type", "general");

            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message content is required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean sent = familyService.sendMessageToFamily(userId, contactId, message, messageType);

            if (!sent) {
                response.put("success", false);
                response.put("message", "Contact not found or message could not be sent");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "Message sent successfully");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send message: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get emergency contacts
     * 
     * @param authHeader Authorization header
     * @return List of emergency contacts
     */
    @GetMapping("/emergency-contacts")
    public ResponseEntity<Map<String, Object>> getEmergencyContacts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            
            response.put("success", true);
            response.put("emergencyContacts", emergencyContacts);
            response.put("count", emergencyContacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching emergency contacts: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get family contact statistics
     * 
     * @param authHeader Authorization header
     * @return Contact statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFamilyStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            Map<String, Object> stats = familyService.getFamilyStats(userId);
            
            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching family statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== 新增异常时通知家庭成员功能 ==========
    
    /**
     * 发送紧急通知给所有家庭成员
     * 
     * @param emergencyData 紧急情况数据
     * @param authHeader Authorization header
     * @return 通知结果
     */
    @PostMapping("/emergency-notification")
    public ResponseEntity<Map<String, Object>> sendEmergencyNotification(
            @RequestBody Map<String, Object> emergencyData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            String emergencyType = (String) emergencyData.get("emergencyType");
            String description = (String) emergencyData.get("description");

            if (emergencyType == null || emergencyType.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Emergency type is required");
                return ResponseEntity.badRequest().body(response);
            }

            int notifiedCount = familyService.sendEmergencyNotification(userId, emergencyType, description);
            
            response.put("success", true);
            response.put("message", "Emergency notification sent to " + notifiedCount + " family members");
            response.put("notifiedCount", notifiedCount);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send emergency notification: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 发送健康异常通知给家庭成员
     * 
     * @param healthAlertData 健康异常数据
     * @param authHeader Authorization header
     * @return 通知结果
     */
    @PostMapping("/health-alert")
    public ResponseEntity<Map<String, Object>> sendHealthAlert(
            @RequestBody Map<String, Object> healthAlertData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        // Check authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Extract userId from JWT token
            Long userId = jwtUtil.extractUserIdFromHeader(authHeader);
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            String healthDataType = (String) healthAlertData.get("healthDataType");
            String abnormalValue = (String) healthAlertData.get("abnormalValue");
            String alertLevel = (String) healthAlertData.getOrDefault("alertLevel", "medium");

            if (healthDataType == null || abnormalValue == null) {
                response.put("success", false);
                response.put("message", "Health data type and abnormal value are required");
                return ResponseEntity.badRequest().body(response);
            }

            // 构建健康异常消息
            String message = buildHealthAlertMessage(healthDataType, abnormalValue, alertLevel);
            
            // 获取紧急联系人并发送通知
            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            int notifiedCount = 0;
            
            for (FamilyContact contact : emergencyContacts) {
                if (familyService.sendMessageToFamily(userId, contact.getId(), message, "health_alert")) {
                    notifiedCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", "Health alert sent to " + notifiedCount + " family members");
            response.put("notifiedCount", notifiedCount);
            response.put("alertLevel", alertLevel);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to send health alert: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 构建健康异常消息
     * 
     * @param healthDataType 健康数据类型
     * @param abnormalValue 异常值
     * @param alertLevel 警报级别
     * @return 消息内容
     */
    private String buildHealthAlertMessage(String healthDataType, String abnormalValue, String alertLevel) {
        StringBuilder message = new StringBuilder();
        
        // 根据警报级别添加不同的前缀
        switch (alertLevel.toLowerCase()) {
            case "high":
                message.append("🚨 紧急健康警报 🚨\n\n");
                break;
            case "medium":
                message.append("⚠️ 健康异常警报 ⚠️\n\n");
                break;
            case "low":
                message.append("📊 健康数据异常 📊\n\n");
                break;
            default:
                message.append("🏥 健康异常通知 🏥\n\n");
        }
        
        message.append("检测到异常健康数据：\n\n");
        message.append("数据类型：").append(getHealthDataTypeDisplayName(healthDataType)).append("\n");
        message.append("异常值：").append(abnormalValue).append("\n");
        message.append("检测时间：").append(java.time.LocalDateTime.now()).append("\n\n");
        
        if ("high".equals(alertLevel)) {
            message.append("⚠️ 请立即关注并联系用户确认健康状况！\n");
            message.append("如有必要，请立即联系紧急医疗服务。\n\n");
        } else if ("medium".equals(alertLevel)) {
            message.append("请及时关注用户的健康状况。\n");
            message.append("建议尽快联系用户了解情况。\n\n");
        } else {
            message.append("请关注用户的健康状况变化。\n\n");
        }
        
        message.append("此通知由IBM AI Elderly系统自动发送。");
        
        return message.toString();
    }
    
    /**
     * 获取健康数据类型的显示名称
     * 
     * @param healthDataType 健康数据类型
     * @return 显示名称
     */
    private String getHealthDataTypeDisplayName(String healthDataType) {
        switch (healthDataType.toLowerCase()) {
            case "bloodpressure":
                return "血压";
            case "bloodsugar":
                return "血糖";
            case "steps":
                return "步数";
            case "heartrate":
                return "心率";
            case "temperature":
                return "体温";
            default:
                return healthDataType;
        }
    }
    
    // ========== 新增异常通知功能结束 ==========
}