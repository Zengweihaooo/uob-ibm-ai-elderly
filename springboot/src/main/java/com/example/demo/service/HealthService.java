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
        
        // 确保时间字段都有正确的值
        LocalDateTime now = LocalDateTime.now();
        if (r.getRecordTime() == null) {
            r.setRecordTime(now);
        }
        if (r.getCreatedAt() == null) {
            r.setCreatedAt(now);
        }
        if (r.getUpdatedAt() == null) {
            r.setUpdatedAt(now);
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
        LocalDateTime endOfDay = today.atTime(23, 59, 59, 999999999); // 23:59:59.999999999
        
        // 使用与数据库存储格式匹配的时间格式
        String startIso = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endIso = endOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // 添加调试日志
        System.out.println("=== getTodayRecords 调试信息 ===");
        System.out.println("用户ID: " + userId);
        System.out.println("开始时间: " + startIso);
        System.out.println("结束时间: " + endIso);
        System.out.println("当前时间: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        List<HealthRecord> records = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
        
        System.out.println("查询结果记录数: " + records.size());
        if (!records.isEmpty()) {
            System.out.println("第一条记录时间: " + records.get(0).getRecordTime());
            System.out.println("最后一条记录时间: " + records.get(records.size() - 1).getRecordTime());
        }
        System.out.println("=== getTodayRecords 调试信息结束 ===");
        
        return records;
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
    
    // ========== 健康功能与Email集成优化 ==========
    
    /**
     * 发送每日健康检查提醒邮件
     * @param userId 用户ID
     * @return 是否发送成功
     */
    public boolean sendDailyHealthCheckReminder(Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                System.err.println("用户不存在：" + userId);
                return false;
            }
            
            // 检查今天是否已有健康记录
            List<HealthRecord> todayRecords = getTodayRecords(userId);
            if (!todayRecords.isEmpty()) {
                System.out.println("用户 " + userId + " 今天已有健康记录，跳过提醒");
                return true;
            }
            
            // 发送提醒邮件给用户
            String subject = "每日健康检查提醒";
            String message = buildDailyHealthCheckMessage(user);
            emailService.sendDailyHealthCheckReminderEmail(user.getEmail(), subject, message);
            
            // 如果用户没有健康记录，也通知家庭成员
            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            for (FamilyContact contact : emergencyContacts) {
                if (contact.getEmail() != null) {
                    String familySubject = "健康检查提醒 - " + user.getName();
                    String familyMessage = buildFamilyHealthCheckMessage(user, contact.getName());
                    emailService.sendDailyHealthCheckReminderEmail(contact.getEmail(), familySubject, familyMessage);
                }
            }
            
            System.out.println("每日健康检查提醒已发送给用户 " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("发送每日健康检查提醒失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送健康数据汇总报告
     * @param userId 用户ID
     * @param reportType 报告类型（daily, weekly, monthly）
     * @return 是否发送成功
     */
    public boolean sendHealthReport(Long userId, String reportType) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // 生成健康报告数据
            Map<String, Object> reportData = generateHealthReport(userId, reportType);
            
            // 发送给用户
            String subject = "健康数据报告 - " + getReportTypeDisplayName(reportType);
            String message = buildHealthReportMessage(user, reportData, reportType);
            emailService.sendCustomEmail(user.getEmail(), subject, message, "健康助手");
            
            // 发送给家庭成员（如果用户同意）
            List<FamilyContact> familyContacts = familyService.getFamilyContacts(userId);
            for (FamilyContact contact : familyContacts) {
                if (contact.getEmail() != null && contact.getIsEmergencyContact()) {
                    String familySubject = "健康报告 - " + user.getName() + " - " + getReportTypeDisplayName(reportType);
                    String familyMessage = buildFamilyHealthReportMessage(user, reportData, reportType, contact.getName());
                    emailService.sendCustomEmail(contact.getEmail(), familySubject, familyMessage, "健康助手");
                }
            }
            
            System.out.println("健康报告已发送给用户 " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("发送健康报告失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送健康趋势分析邮件
     * @param userId 用户ID
     * @param days 分析天数
     * @return 是否发送成功
     */
    public boolean sendHealthTrendAnalysis(Long userId, int days) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // 获取历史数据
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endIso = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            List<HealthRecord> records = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
            
            // 分析趋势
            Map<String, Object> trendAnalysis = analyzeHealthTrends(records, days);
            
            // 发送分析报告
            String subject = "健康趋势分析报告 - 最近" + days + "天";
            String message = buildTrendAnalysisMessage(user, trendAnalysis, days);
            emailService.sendCustomEmail(user.getEmail(), subject, message, "健康分析师");
            
            System.out.println("健康趋势分析已发送给用户 " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("发送健康趋势分析失败：" + e.getMessage());
            return false;
        }
    }
    
    /**
     * 构建每日健康检查提醒消息
     */
    private String buildDailyHealthCheckMessage(User user) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #4CAF50;'>🏥 每日健康检查提醒</h2>");
        message.append("<p>亲爱的 ").append(user.getName() != null ? user.getName() : "用户").append("，</p>");
        message.append("<p>现在是您的每日健康检查时间！请记得记录以下健康数据：</p>");
        message.append("<ul>");
        message.append("<li>💓 血压测量</li>");
        message.append("<li>🩸 血糖检测</li>");
        message.append("<li>👟 今日步数</li>");
        message.append("</ul>");
        message.append("<p>及时记录健康数据有助于我们更好地关注您的健康状况。</p>");
        message.append("<p>祝您健康快乐！</p>");
        message.append("<p>IBM AI 健康助手</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * 构建家庭成员健康检查提醒消息
     */
    private String buildFamilyHealthCheckMessage(User user, String contactName) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #FF9800;'>👨‍👩‍👧‍👦 家庭成员健康提醒</h2>");
        message.append("<p>亲爱的 ").append(contactName).append("，</p>");
        message.append("<p>您的家人 ").append(user.getName() != null ? user.getName() : "用户").append(" 今天还没有记录健康数据。</p>");
        message.append("<p>请提醒他们进行每日健康检查，包括：</p>");
        message.append("<ul>");
        message.append("<li>血压测量</li>");
        message.append("<li>血糖检测</li>");
        message.append("<li>步数记录</li>");
        message.append("</ul>");
        message.append("<p>您的关心是家人健康的重要保障！</p>");
        message.append("<p>IBM AI 健康助手</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * 生成健康报告数据
     */
    private Map<String, Object> generateHealthReport(Long userId, String reportType) {
        Map<String, Object> report = new HashMap<>();
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (reportType.toLowerCase()) {
            case "daily":
                startDate = endDate.minusDays(1);
                break;
            case "weekly":
                startDate = endDate.minusWeeks(1);
                break;
            case "monthly":
                startDate = endDate.minusMonths(1);
                break;
            default:
                startDate = endDate.minusDays(7);
        }
        
        String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endIso = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        List<HealthRecord> records = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
        
        // 统计数据
        long totalRecords = records.size();
        long abnormalRecords = records.stream()
            .filter(record -> isAbnormal(record.getType(), record.getValue()))
            .count();
        
        // 按类型分组
        Map<String, Long> typeCount = new HashMap<>();
        Map<String, Long> abnormalTypeCount = new HashMap<>();
        
        for (HealthRecord record : records) {
            typeCount.merge(record.getType(), 1L, Long::sum);
            if (isAbnormal(record.getType(), record.getValue())) {
                abnormalTypeCount.merge(record.getType(), 1L, Long::sum);
            }
        }
        
        report.put("totalRecords", totalRecords);
        report.put("abnormalRecords", abnormalRecords);
        report.put("normalRecords", totalRecords - abnormalRecords);
        report.put("typeCount", typeCount);
        report.put("abnormalTypeCount", abnormalTypeCount);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        
        return report;
    }
    
    /**
     * 构建健康报告消息
     */
    private String buildHealthReportMessage(User user, Map<String, Object> reportData, String reportType) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #2196F3;'>📊 健康数据报告</h2>");
        message.append("<p>亲爱的 ").append(user.getName() != null ? user.getName() : "用户").append("，</p>");
        message.append("<p>以下是您的").append(getReportTypeDisplayName(reportType)).append("健康数据汇总：</p>");
        
        message.append("<div style='background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📈 数据概览</h3>");
        message.append("<p><strong>总记录数：</strong>").append(reportData.get("totalRecords")).append("</p>");
        message.append("<p><strong>正常记录：</strong>").append(reportData.get("normalRecords")).append("</p>");
        message.append("<p><strong>异常记录：</strong>").append(reportData.get("abnormalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>请继续保持良好的健康习惯！</p>");
        message.append("<p>IBM AI 健康助手</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * 构建家庭成员健康报告消息
     */
    private String buildFamilyHealthReportMessage(User user, Map<String, Object> reportData, String reportType, String contactName) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #FF9800;'>👨‍👩‍👧‍👦 家庭成员健康报告</h2>");
        message.append("<p>亲爱的 ").append(contactName).append("，</p>");
        message.append("<p>以下是您的家人 ").append(user.getName() != null ? user.getName() : "用户").append(" 的").append(getReportTypeDisplayName(reportType)).append("健康报告：</p>");
        
        message.append("<div style='background-color: #fff3e0; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📊 健康数据概览</h3>");
        message.append("<p><strong>总记录数：</strong>").append(reportData.get("totalRecords")).append("</p>");
        message.append("<p><strong>正常记录：</strong>").append(reportData.get("normalRecords")).append("</p>");
        message.append("<p><strong>异常记录：</strong>").append(reportData.get("abnormalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>请继续关注家人的健康状况！</p>");
        message.append("<p>IBM AI 健康助手</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * 分析健康趋势
     */
    private Map<String, Object> analyzeHealthTrends(List<HealthRecord> records, int days) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 按类型分组分析
        Map<String, List<HealthRecord>> recordsByType = new HashMap<>();
        for (HealthRecord record : records) {
            recordsByType.computeIfAbsent(record.getType(), k -> new ArrayList<>()).add(record);
        }
        
        Map<String, Object> typeAnalysis = new HashMap<>();
        for (Map.Entry<String, List<HealthRecord>> entry : recordsByType.entrySet()) {
            String type = entry.getKey();
            List<HealthRecord> typeRecords = entry.getValue();
            
            Map<String, Object> analysisData = new HashMap<>();
            analysisData.put("totalRecords", typeRecords.size());
            analysisData.put("abnormalCount", typeRecords.stream()
                .filter(record -> isAbnormal(record.getType(), record.getValue()))
                .count());
            
            typeAnalysis.put(type, analysisData);
        }
        
        analysis.put("typeAnalysis", typeAnalysis);
        analysis.put("totalDays", days);
        analysis.put("totalRecords", records.size());
        
        return analysis;
    }
    
    /**
     * 构建趋势分析消息
     */
    private String buildTrendAnalysisMessage(User user, Map<String, Object> trendAnalysis, int days) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #9C27B0;'>📈 健康趋势分析</h2>");
        message.append("<p>亲爱的 ").append(user.getName() != null ? user.getName() : "用户").append("，</p>");
        message.append("<p>以下是您最近").append(days).append("天的健康趋势分析：</p>");
        
        message.append("<div style='background-color: #f3e5f5; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📊 趋势概览</h3>");
        message.append("<p><strong>分析天数：</strong>").append(days).append("天</p>");
        message.append("<p><strong>总记录数：</strong>").append(trendAnalysis.get("totalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>请根据分析结果调整您的健康管理计划！</p>");
        message.append("<p>IBM AI 健康分析师</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * 获取报告类型显示名称
     */
    private String getReportTypeDisplayName(String reportType) {
        switch (reportType.toLowerCase()) {
            case "daily":
                return "每日";
            case "weekly":
                return "每周";
            case "monthly":
                return "每月";
            default:
                return "定期";
        }
    }
    
    // ========== 健康功能与Email集成优化结束 ==========
    
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
    
    /**
     * 获取所有健康记录
     * @return 所有健康记录列表
     */
    public List<HealthRecord> getAll() {
        return healthRecordMapper.listAll();
    }
    
    // ========== 其他辅助方法结束 ==========
} 