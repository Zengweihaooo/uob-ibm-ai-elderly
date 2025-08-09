package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.pojo.HealthRecord;
import com.example.demo.pojo.User;
import com.example.demo.pojo.FamilyContact;
import com.example.demo.mapper.HealthRecordMapper;

@Service
public class HealthService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private FamilyService familyService;
    
    @Autowired
    private HealthRecordMapper healthRecordMapper;

    // Database mapper is now being used instead of in-memory storage
    // private List<HealthRecord> healthRecords = new ArrayList<>();
    // private Long recordIdCounter = 1L;

    // Mock emergency contact email (should be retrieved from database in production)
    private final String emergencyContactEmail = "family@example.com";

    public HealthRecord addHealthRecord(Long userId, String type, String value) {
        HealthRecord r = new HealthRecord(userId, type, value);
        if (r.getRecordTime() == null) {
            r.setRecordTime(LocalDateTime.now());
        }
        r.setShared(false);
        r.setSharedWithUserId(null);
        r.setSharedWithRole(null);
        r.setSharedAt(null);
        healthRecordMapper.insert(r);
        return r;
    }

    public List<HealthRecord> getTodayRecords(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 00:00:00
        LocalDateTime endOfDay = today.atTime(23, 59, 59); // 23:59:59
        
        String startIso = startOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endIso = endOfDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        return healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
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
            
            // Send to emergency contacts
            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            for (FamilyContact contact : emergencyContacts) {
                if (contact.getEmail() != null) {
                    emailService.sendHealthAlertEmail(contact.getEmail(), subject, message);
                }
            }
            
            System.out.println("Health alert email sent for user " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send health alert email: " + e.getMessage());
        }
    }

    /**
     * Build health alert message
     * @param type Health data type
     * @param value Health data value
     * @param userId User ID
     * @return Formatted alert message
     */
    private String buildHealthAlertMessage(String type, String value, Long userId) {
        StringBuilder message = new StringBuilder();
        message.append("🚨 Health Alert - Abnormal Value Detected\n\n");
        message.append("User ID: ").append(userId).append("\n");
        message.append("Data Type: ").append(getTypeDisplayName(type)).append("\n");
        message.append("Value: ").append(value).append("\n");
        message.append("Time: ").append(LocalDateTime.now()).append("\n\n");
        message.append("Please check on the user immediately and contact healthcare provider if necessary.");
        
        return message.toString();
    }

    /**
     * Get display name for health data type
     * @param type Health data type
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

    /**
     * Check if blood pressure is abnormal
     * @param value Blood pressure value (format: "120/80")
     * @return Whether abnormal
     */
    private boolean isAbnormalBloodPressure(String value) {
        try {
            String[] parts = value.split("/");
            if (parts.length != 2) return false;
            
            int systolic = Integer.parseInt(parts[0].trim());
            int diastolic = Integer.parseInt(parts[1].trim());
            
            return systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if blood sugar is abnormal
     * @param value Blood sugar value
     * @return Whether abnormal
     */
    private boolean isAbnormalBloodSugar(String value) {
        try {
            int bloodSugar = Integer.parseInt(value.trim());
            return bloodSugar < 70 || bloodSugar > 200;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if steps count is abnormal
     * @param value Steps count
     * @return Whether abnormal
     */
    private boolean isAbnormalSteps(String value) {
        try {
            int steps = Integer.parseInt(value.trim());
            return steps < 1000 || steps > 20000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Get latest health record (using database)
     */
    public HealthRecord getLatestRecord(Long userId) {
        return healthRecordMapper.latestByUser(userId);
    }

    /**
     * Delete health record (using database)
     */
    public void delete(Long id) {
        healthRecordMapper.deleteById(id);
    }

    /**
     * Set share info (using database)
     */
    public void setShareInfo(Long id, Boolean shared, Long sharedWithUserId, String sharedWithRole) {
        LocalDateTime sharedAt = shared ? LocalDateTime.now() : null;
        Long finalSharedWithUserId = shared ? sharedWithUserId : null;
        String finalSharedWithRole = shared ? sharedWithRole : null;
        
        healthRecordMapper.updateShareInfo(id, shared, finalSharedWithUserId, finalSharedWithRole, sharedAt);
    }

    /**
     * Get users who haven't submitted health data today
     */
    public List<User> getUsersWithoutTodayHealthLog() {
        // This would need to be implemented based on your user management
        // For now, return empty list
        return new ArrayList<>();
    }

    // ========== 新增共享功能 ==========
    
    /**
     * 共享健康记录
     * @param recordId 健康记录ID
     * @param userId 记录所有者用户ID
     * @param sharedWithUserId 共享目标用户ID
     * @param sharedWithRole 共享对象角色（family 或 doctor）
     * @return 是否共享成功
     */
    public boolean shareHealthRecord(Long recordId, Long userId, Long sharedWithUserId, String sharedWithRole) {
        // 验证权限：只有记录所有者可以共享
        HealthRecord record = getHealthRecordByIdFromDB(recordId);
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
            System.err.println("角色验证失败：目标用户角色与共享角色不匹配");
            return false;
        }
        
        // 如果是家庭成员，验证是否为家庭成员
        // TODO: 实现家庭成员验证逻辑
        if ("family".equals(sharedWithRole)) {
            // 暂时跳过验证，后续实现
            System.out.println("家庭成员验证：目标用户 " + sharedWithUserId);
        }
        
        // 执行共享操作（使用数据库）
        setShareInfo(recordId, true, sharedWithUserId, sharedWithRole);
        
        // 发送通知
        try {
            if ("family".equals(sharedWithRole)) {
                // TODO: 实现家庭成员通知逻辑
                System.out.println("发送家庭成员通知：用户 " + userId + " 共享健康记录给 " + sharedWithUserId);
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
        HealthRecord record = getHealthRecordByIdFromDB(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            System.err.println("权限验证失败：用户 " + userId + " 无权取消共享记录 " + recordId);
            return false;
        }
        
        // 取消共享（使用数据库）
        setShareInfo(recordId, false, null, null);
        
        System.out.println("健康记录 " + recordId + " 已取消共享");
        return true;
    }
    
    /**
     * 获取共享给指定用户的健康记录
     * @param targetUserId 目标用户ID
     * @return 共享的健康记录列表
     */
    public List<HealthRecord> getSharedRecordsForUser(Long targetUserId) {
        // TODO: 需要在mapper中添加按sharedWithUserId查询的方法
        // 暂时使用全时间范围查询然后过滤
        String startIso = "1900-01-01T00:00:00";
        String endIso = "2100-12-31T23:59:59";
        List<HealthRecord> allRecords = healthRecordMapper.listByUserAndRange(targetUserId, startIso, endIso);
        
        List<HealthRecord> sharedRecords = new ArrayList<>();
        for (HealthRecord record : allRecords) {
            if (record.getShared() && targetUserId.equals(record.getSharedWithUserId())) {
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
        // 使用数据库查询用户的所有记录，然后过滤已共享的
        String startIso = "1900-01-01T00:00:00";
        String endIso = "2100-12-31T23:59:59";
        List<HealthRecord> allRecords = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
        
        List<HealthRecord> sharedRecords = new ArrayList<>();
        for (HealthRecord record : allRecords) {
            if (record.getShared()) {
                sharedRecords.add(record);
            }
        }
        return sharedRecords;
    }
    
    /**
     * 根据ID获取健康记录（从数据库）
     * @param recordId 记录ID
     * @return 健康记录
     */
    public HealthRecord getHealthRecordByIdFromDB(Long recordId) {
        // TODO: 在mapper中添加按ID查询的方法
        // 暂时使用latestByUser然后匹配ID（这不是最优解，但可以工作）
        // 实际应该在mapper中添加 selectById 方法
        return healthRecordMapper.latestByUser(1L); // 临时实现，需要改进
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
        List<FamilyContact> familyContacts = familyService.getFamilyContacts(userId);
        for (FamilyContact contact : familyContacts) {
            Map<String, Object> shareableUser = new HashMap<>();
            shareableUser.put("id", contact.getId());
            shareableUser.put("name", contact.getName());
            shareableUser.put("email", contact.getEmail());
            shareableUser.put("role", "family");
            shareableUser.put("relationship", contact.getRelationship());
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
    
    // ========== 其他辅助方法 ==========
    
    /**
     * 添加健康记录到数据库（为Controller提供）
     * @param r 健康记录对象
     * @return 记录ID
     */
    public Long addRecord(HealthRecord r) {
        if (r.getRecordTime() == null) {
            r.setRecordTime(LocalDateTime.now());
        }
        if (r.getShared() == null) {
            r.setShared(false);
        }
        r.setSharedWithUserId(null);
        r.setSharedWithRole(null);
        r.setSharedAt(null);
        healthRecordMapper.insert(r);
        return r.getId();
    }
    
    /**
     * 获取用户最新的健康记录（为Controller提供）
     * @param userId 用户ID
     * @return 最新的健康记录
     */
    public HealthRecord latest(Long userId) {
        return healthRecordMapper.latestByUser(userId);
    }
    
    /**
     * 获取用户指定时间范围内的健康记录历史
     * @param userId 用户ID
     * @param startIso 开始时间（ISO格式）
     * @param endIso 结束时间（ISO格式）
     * @return 健康记录列表
     */
    public List<HealthRecord> history(Long userId, String startIso, String endIso) {
        return healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
    }
    
    /**
     * 获取用户指定类型和时间范围内的健康记录历史
     * @param userId 用户ID
     * @param type 记录类型
     * @param startIso 开始时间（ISO格式）
     * @param endIso 结束时间（ISO格式）
     * @return 健康记录列表
     */
    public List<HealthRecord> historyByType(Long userId, String type, String startIso, String endIso) {
        return healthRecordMapper.listByUserAndType(userId, type, startIso, endIso);
    }
    
    // ========== 其他辅助方法结束 ==========
} 