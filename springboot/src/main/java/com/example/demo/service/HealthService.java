package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.HealthRecord;
import com.example.demo.pojo.User;

@Service
public class HealthService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;
    
    // ========== 新增依赖注入 ==========
    @Autowired
    private FamilyService familyService;
    // ========== 新增依赖结束 ==========

    // In-memory storage for health records (consider using database in production)
    private List<HealthRecord> healthRecords = new ArrayList<>();
    private Long recordIdCounter = 1L;

    // Mock emergency contact email (should be retrieved from database in production)
    private final String emergencyContactEmail = "family@example.com";

    public HealthRecord addHealthRecord(Long userId, String type, String value) {
        HealthRecord record = new HealthRecord(userId, type, value);
        record.setId(recordIdCounter++);
        healthRecords.add(record);
        return record;
    }

    public List<HealthRecord> getTodayRecords(Long userId) {
        LocalDate today = LocalDate.now();
        List<HealthRecord> todayRecords = new ArrayList<>();
        
        for (HealthRecord record : healthRecords) {
            if (record.getUserId().equals(userId) && 
                record.getRecordTime().toLocalDate().equals(today)) {
                todayRecords.add(record);
            }
        }
        
        return todayRecords;
    }

    public boolean isAbnormal(String type, String value) {
        try {
            switch (type.toLowerCase()) {
                case "bloodpressure":
                    return isAbnormalBloodPressure(value);
                case "bloodsugar":
                    return isAbnormalBloodSugar(value);
                case "steps":
                    return isAbnormalSteps(value);
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check for abnormal values and send email notification
     * @param type Health data type
     * @param value Health data value
     * @param userId User ID
     * @return Whether the value is abnormal
     */
    public boolean checkAbnormalAndNotify(String type, String value, Long userId) {
        boolean isAbnormal = isAbnormal(type, value);
        
        if (isAbnormal) {
            try {
                sendHealthAlertEmail(type, value, userId);
            } catch (Exception e) {
                System.err.println("Failed to send health alert email: " + e.getMessage());
            }
        }
        
        return isAbnormal;
    }

    /**
     * Send health alert email
     * @param type Health data type
     * @param value Health data value
     * @param userId User ID
     */
    private void sendHealthAlertEmail(String type, String value, Long userId) {
        try {
            String subject = "Health Alert - Attention Required";
            String message = buildHealthAlertMessage(type, value, userId);
            
            // Use EmailService to send email
            emailService.sendHealthAlertEmail(emergencyContactEmail, subject, message);
            
            System.out.println("Health alert email sent for user " + userId + 
                             " with abnormal " + type + " value: " + value);
        } catch (Exception e) {
            System.err.println("Failed to send health alert email: " + e.getMessage());
        }
    }

    /**
     * Build health alert email content
     * @param type Health data type
     * @param value Health data value
     * @param userId User ID
     * @return Email message content
     */
    private String buildHealthAlertMessage(String type, String value, Long userId) {
        StringBuilder message = new StringBuilder();
        message.append("Dear Family Member:\n\n");
        message.append("Abnormal health data detected for User ID ").append(userId).append(":\n\n");
        message.append("Data Type: ").append(getTypeDisplayName(type)).append("\n");
        message.append("Abnormal Value: ").append(value).append("\n");
        message.append("Detection Time: ").append(LocalDateTime.now()).append("\n\n");
        message.append("Please pay immediate attention and contact the user to confirm their health status.\n\n");
        message.append("This email was automatically sent by the IBM AI Elderly system.");
        
        return message.toString();
    }

    /**
     * Get display name for health data type
     * @param type Data type
     * @return Display name
     */
    private String getTypeDisplayName(String type) {
        switch (type.toLowerCase()) {
            case "bloodpressure":
                return "Blood Pressure";
            case "bloodsugar":
                return "Blood Sugar";
            case "steps":
                return "Daily Steps";
            default:
                return type;
        }
    }

    private boolean isAbnormalBloodPressure(String value) {
        // Blood pressure format: "120/80"
        String[] parts = value.split("/");
        if (parts.length != 2) return false;
        
        int systolic = Integer.parseInt(parts[0]);
        int diastolic = Integer.parseInt(parts[1]);
        
        // Abnormal blood pressure: Systolic > 140 or < 90, Diastolic > 90 or < 60
        return systolic > 140 || systolic < 90 || diastolic > 90 || diastolic < 60;
    }

    private boolean isAbnormalBloodSugar(String value) {
        // Blood sugar value (mg/dL)
        int bloodSugar = Integer.parseInt(value);
        
        // Abnormal blood sugar: < 70 or > 200
        return bloodSugar < 70 || bloodSugar > 200;
    }

    private boolean isAbnormalSteps(String value) {
        // Daily steps count
        int steps = Integer.parseInt(value);
        
        // Abnormal steps: < 1000 (too few) or > 20000 (too many)
        return steps < 1000 || steps > 20000;
    }

    // Get all records (for testing)
    public List<HealthRecord> getAllRecords() {
        return new ArrayList<>(healthRecords);
    }

    // Clear all records (for testing)
    public void clearAllRecords() {
        healthRecords.clear();
        recordIdCounter = 1L;
    }


    public List<User> getUsersWithoutTodayHealthLog() {
        // Obtain all users and health records
        List<User> users = userService.getAllUsers();
        List<HealthRecord> healthRecords = getAllRecords();

        // Get the users without today's health records
        List<User> usersWithoutTodayHealthLog = new ArrayList<>();
        for (User user : users) {
            if (!healthRecords.stream().anyMatch(record -> record.getUserId().equals(user.getId()) && record.getRecordTime().toLocalDate().equals(LocalDate.now()))) {
                usersWithoutTodayHealthLog.add(user);
            }
        }
        return usersWithoutTodayHealthLog;
    }
    
    // ========== 新增共享功能相关方法 ==========
    
    /**
     * 共享健康记录给家庭成员或医生
     * @param recordId 健康记录ID
     * @param userId 记录所有者用户ID
     * @param sharedWithUserId 共享目标用户ID
     * @param sharedWithRole 共享对象角色（family 或 doctor）
     * @return 是否共享成功
     */
    public boolean shareHealthRecord(Long recordId, Long userId, Long sharedWithUserId, String sharedWithRole) {
        // 验证权限：只有记录所有者可以共享
        HealthRecord record = getHealthRecordById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            System.err.println("权限验证失败：用户 " + userId + " 无权共享记录 " + recordId);
            return false;
        }
        
        // 验证目标用户是否存在
        User targetUser = userService.getUserById(sharedWithUserId);
        if (targetUser == null) {
            System.err.println("目标用户不存在：" + sharedWithUserId);
            return false;
        }
        
        // 验证角色匹配
        if (!validateRoleMatch(sharedWithRole, targetUser)) {
            System.err.println("角色验证失败：目标用户角色与指定角色不匹配");
            return false;
        }
        
        // 如果是家庭成员，验证是否为家庭成员
        if ("family".equals(sharedWithRole) && !familyService.isFamilyMember(userId, sharedWithUserId)) {
            System.err.println("目标用户不是家庭成员：" + sharedWithUserId);
            return false;
        }
        
        // 执行共享操作
        record.setShared(true);
        record.setSharedWithUserId(sharedWithUserId);
        record.setSharedWithRole(sharedWithRole);
        record.setSharedAt(LocalDateTime.now());
        
        // 发送通知
        try {
            if ("family".equals(sharedWithRole)) {
                familyService.notifyFamilyMemberOfHealthShare(userId, record, sharedWithUserId);
            }
        } catch (Exception e) {
            System.err.println("发送共享通知失败：" + e.getMessage());
        }
        
        System.out.println("健康记录 " + recordId + " 已成功共享给用户 " + sharedWithUserId);
        return true;
    }
    
    /**
     * 取消共享健康记录
     * @param recordId 健康记录ID
     * @param userId 记录所有者用户ID
     * @return 是否取消成功
     */
    public boolean unshareHealthRecord(Long recordId, Long userId) {
        // 验证权限：只有记录所有者可以取消共享
        HealthRecord record = getHealthRecordById(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            System.err.println("权限验证失败：用户 " + userId + " 无权取消共享记录 " + recordId);
            return false;
        }
        
        // 取消共享
        record.setShared(false);
        record.setSharedWithUserId(null);
        record.setSharedWithRole(null);
        record.setSharedAt(null);
        
        System.out.println("健康记录 " + recordId + " 已取消共享");
        return true;
    }
    
    /**
     * 获取共享给指定用户的健康记录
     * @param targetUserId 目标用户ID
     * @return 共享的健康记录列表
     */
    public List<HealthRecord> getSharedRecordsForUser(Long targetUserId) {
        List<HealthRecord> sharedRecords = new ArrayList<>();
        
        for (HealthRecord record : healthRecords) {
            if (record.isShared() && record.getSharedWithUserId().equals(targetUserId)) {
                sharedRecords.add(record);
            }
        }
        
        return sharedRecords;
    }
    
    /**
     * 获取用户共享的所有健康记录
     * @param userId 用户ID
     * @return 用户共享的健康记录列表
     */
    public List<HealthRecord> getUserSharedRecords(Long userId) {
        List<HealthRecord> sharedRecords = new ArrayList<>();
        
        for (HealthRecord record : healthRecords) {
            if (record.getUserId().equals(userId) && record.isShared()) {
                sharedRecords.add(record);
            }
        }
        
        return sharedRecords;
    }
    
    /**
     * 根据ID获取健康记录
     * @param recordId 记录ID
     * @return 健康记录
     */
    public HealthRecord getHealthRecordById(Long recordId) {
        return healthRecords.stream()
                .filter(record -> record.getId().equals(recordId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 验证角色匹配
     * @param expectedRole 期望的角色
     * @param user 用户对象
     * @return 是否匹配
     */
    private boolean validateRoleMatch(String expectedRole, User user) {
        if ("family".equals(expectedRole)) {
            return user.getRole() == User.UserRole.FAMILY;
        } else if ("doctor".equals(expectedRole)) {
            return user.getRole() == User.UserRole.DOCTOR;
        }
        return false;
    }
    
    /**
     * 获取可共享的用户列表（家庭成员和医生）
     * @param userId 当前用户ID
     * @return 可共享的用户列表
     */
    public List<Map<String, Object>> getShareableUsers(Long userId) {
        List<Map<String, Object>> shareableUsers = new ArrayList<>();
        
        // 获取家庭成员
        List<Map<String, Object>> familyMembers = familyService.getFamilyMembersForSharing(userId);
        for (Map<String, Object> member : familyMembers) {
            Map<String, Object> shareableUser = new HashMap<>();
            shareableUser.put("id", member.get("id"));
            shareableUser.put("name", member.get("name"));
            shareableUser.put("email", member.get("email"));
            shareableUser.put("role", "family");
            shareableUser.put("relationship", member.get("relationship"));
            shareableUsers.add(shareableUser);
        }
        
        // 获取医生列表
        List<User> doctors = userService.getDoctors();
        for (User doctor : doctors) {
            Map<String, Object> shareableUser = new HashMap<>();
            shareableUser.put("id", doctor.getId());
            shareableUser.put("name", doctor.getName());
            shareableUser.put("email", doctor.getEmail());
            shareableUser.put("role", "doctor");
            shareableUser.put("relationship", "医生");
            shareableUsers.add(shareableUser);
        }
        
        return shareableUsers;
    }
    
    // ========== 新增共享功能结束 ==========
} 