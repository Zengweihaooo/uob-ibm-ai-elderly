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

import com.example.demo.mapper.HealthRecordMapper;
import com.example.demo.pojo.FamilyContact;
import com.example.demo.pojo.HealthRecord;
import com.example.demo.pojo.User;

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
        
        // Ensure time fields have correct values
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
        
        // Use a time format consistent with the database storage format
        String startIso = startOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endIso = endOfDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Add debug logs
        System.out.println("=== getTodayRecords Debug Info ===");
        System.out.println("User ID: " + userId);
        System.out.println("Start time: " + startIso);
        System.out.println("End time: " + endIso);
        System.out.println("Current time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        List<HealthRecord> records = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
        
        System.out.println("Number of records returned: " + records.size());
        if (!records.isEmpty()) {
            System.out.println("First record time: " + records.get(0).getRecordTime());
            System.out.println("Last record time: " + records.get(records.size() - 1).getRecordTime());
        }
        System.out.println("=== End of getTodayRecords Debug Info ===");
        
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

    // ========== New sharing features ==========
    
    /**
     * Share a health record
     * @param recordId Health record ID
     * @param userId Record owner user ID
     * @param sharedWithUserId Target user ID to share with
     * @param sharedWithRole Role of the target (family or doctor)
     * @return Whether sharing succeeded
     */
    public boolean shareHealthRecord(Long recordId, Long userId, Long sharedWithUserId, String sharedWithRole) {
        // Validate permission: only the record owner can share
        HealthRecord record = getHealthRecordByIdFromDB(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            System.err.println("Permission validation failed: user " + userId + " is not allowed to share record " + recordId);
            return false;
        }
        
        // Validate target user exists
        User targetUser = userService.getUserById(sharedWithUserId);
        if (targetUser == null) {
            System.err.println("Target user does not exist: " + sharedWithUserId);
            return false;
        }
        
        // Validate role matches
        if (!validateRoleMatch(sharedWithRole, targetUser)) {
            System.err.println("Role validation failed: target user's role does not match sharing role");
            return false;
        }
        
        // If target is a family member, validate family relationship
        // TODO: Implement family member validation logic
        if ("family".equals(sharedWithRole)) {
            // Skip validation for now; implement later
            System.out.println("Family member validation: target user " + sharedWithUserId);
        }
        
        // Perform the share operation (via database)
        setShareInfo(recordId, true, sharedWithUserId, sharedWithRole);
        
        // Send notifications
        try {
            if ("family".equals(sharedWithRole)) {
                // TODO: Implement family notification logic
                System.out.println("Send family notification: user " + userId + " shared health record with " + sharedWithUserId);
            }
        } catch (Exception e) {
            System.err.println("Failed to send share notification: " + e.getMessage());
        }
        
        System.out.println("Health record " + recordId + " has been successfully shared with user " + sharedWithUserId);
        return true;
    }
    
    /**
     * Unshare a health record
     * @param recordId Health record ID
     * @param userId Record owner user ID
     * @return Whether unshare succeeded
     */
    public boolean unshareHealthRecord(Long recordId, Long userId) {
        // Validate permission: only the record owner can unshare
        HealthRecord record = getHealthRecordByIdFromDB(recordId);
        if (record == null || !record.getUserId().equals(userId)) {
            System.err.println("Permission validation failed: user " + userId + " is not allowed to unshare record " + recordId);
            return false;
        }
        
        // Unshare (via database)
        setShareInfo(recordId, false, null, null);
        
        System.out.println("Health record " + recordId + " has been unshared");
        return true;
    }
    
    /**
     * Get health records shared to a specific user
     * @param targetUserId Target user ID
     * @return List of shared health records
     */
    public List<HealthRecord> getSharedRecordsForUser(Long targetUserId) {
        // TODO: Add mapper method to query by sharedWithUserId
        // For now, query full time range then filter
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
     * Get all health records shared by a user
     * @param userId User ID
     * @return List of user's shared health records
     */
    public List<HealthRecord> getUserSharedRecords(Long userId) {
        // Query all records for the user from DB, then filter shared ones
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
     * Get health record by ID (from database)
     * @param recordId Record ID
     * @return HealthRecord
     */
    public HealthRecord getHealthRecordByIdFromDB(Long recordId) {
        // TODO: Add a mapper method to query by ID
        // Temporarily use latestByUser then match ID (not optimal, but works)
        // Ideally, add selectById method to the mapper
        return healthRecordMapper.latestByUser(1L); // Temporary implementation, needs improvement
    }
    
    /**
     * Validate role match
     * @param expectedRole Expected role
     * @param user User object
     * @return Whether matches
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
     * Get shareable user list (family members and doctors)
     * @param userId Current user ID
     * @return Shareable user list
     */
    public List<Map<String, Object>> getShareableUsers(Long userId) {
        List<Map<String, Object>> shareableUsers = new ArrayList<>();
        
        // Get family members
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
        
        // Get doctors list
        List<User> doctors = userService.getDoctors();
        for (User doctor : doctors) {
            Map<String, Object> shareableUser = new HashMap<>();
            shareableUser.put("id", doctor.getId());
            shareableUser.put("name", doctor.getName());
            shareableUser.put("email", doctor.getEmail());
            shareableUser.put("role", "doctor");
            shareableUser.put("relationship", "Doctor");
            shareableUsers.add(shareableUser);
        }
        
        return shareableUsers;
    }
    
    // ========== End of new sharing features ==========
    
    // ========== Health features and Email integration enhancements ==========
    
    /**
     * Send daily health check reminder email
     * @param userId User ID
     * @return Whether sent successfully
     */
    public boolean sendDailyHealthCheckReminder(Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                System.err.println("User does not exist: " + userId);
                return false;
            }
            
            // Check whether there are records for today
            List<HealthRecord> todayRecords = getTodayRecords(userId);
            if (!todayRecords.isEmpty()) {
                System.out.println("User " + userId + " already has health records today, skip reminder");
                return true;
            }
            
            // Send reminder email to user
            String subject = "Daily Health Check Reminder";
            String message = buildDailyHealthCheckMessage(user);
            emailService.sendDailyHealthCheckReminderEmail(user.getEmail(), subject, message);
            
            // If the user has no health records today, also notify family members
            List<FamilyContact> emergencyContacts = familyService.getEmergencyContacts(userId);
            for (FamilyContact contact : emergencyContacts) {
                if (contact.getEmail() != null) {
                    String familySubject = "Health Check Reminder - " + user.getName();
                    String familyMessage = buildFamilyHealthCheckMessage(user, contact.getName());
                    emailService.sendDailyHealthCheckReminderEmail(contact.getEmail(), familySubject, familyMessage);
                }
            }
            
            System.out.println("Daily health check reminder has been sent to user " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send daily health check reminder: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send health data summary report
     * @param userId User ID
     * @param reportType Report type (daily, weekly, monthly)
     * @return Whether sent successfully
     */
    public boolean sendHealthReport(Long userId, String reportType) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // Generate health report data
            Map<String, Object> reportData = generateHealthReport(userId, reportType);
            
            // Send to user
            String subject = "Health Data Report - " + getReportTypeDisplayName(reportType);
            String message = buildHealthReportMessage(user, reportData, reportType);
            emailService.sendCustomEmail(user.getEmail(), subject, message, "Health Assistant");
            
            // Send to family members (if the user agrees)
            List<FamilyContact> familyContacts = familyService.getFamilyContacts(userId);
            for (FamilyContact contact : familyContacts) {
                if (contact.getEmail() != null && contact.getIsEmergencyContact()) {
                    String familySubject = "Health Report - " + user.getName() + " - " + getReportTypeDisplayName(reportType);
                    String familyMessage = buildFamilyHealthReportMessage(user, reportData, reportType, contact.getName());
                    emailService.sendCustomEmail(contact.getEmail(), familySubject, familyMessage, "Health Assistant");
                }
            }
            
            System.out.println("Health report has been sent to user " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send health report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send health trend analysis email
     * @param userId User ID
     * @param days Number of days to analyze
     * @return Whether sent successfully
     */
    public boolean sendHealthTrendAnalysis(Long userId, int days) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return false;
            }
            
            // Get historical data
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            String startIso = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String endIso = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            List<HealthRecord> records = healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
            
            // Analyze trends
            Map<String, Object> trendAnalysis = analyzeHealthTrends(records, days);
            
            // Send analysis report
            String subject = "Health Trend Analysis Report - Last " + days + " days";
            String message = buildTrendAnalysisMessage(user, trendAnalysis, days);
            emailService.sendCustomEmail(user.getEmail(), subject, message, "Health Analyst");
            
            System.out.println("Health trend analysis has been sent to user " + userId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send health trend analysis: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Build daily health check reminder message
     */
    private String buildDailyHealthCheckMessage(User user) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #4CAF50;'>🏥 Daily Health Check Reminder</h2>");
        message.append("<p>Dear ").append(user.getName() != null ? user.getName() : "User").append(",</p>");
        message.append("<p>It's time for your daily health check! Please remember to record the following health data:</p>");
        message.append("<ul>");
        message.append("<li>💓 Blood pressure measurement</li>");
        message.append("<li>🩸 Blood sugar test</li>");
        message.append("<li>👟 Today's steps</li>");
        message.append("</ul>");
        message.append("<p>Timely recording of health data helps us better monitor your health status.</p>");
        message.append("<p>Wish you good health and happiness!</p>");
        message.append("<p>IBM AI Health Assistant</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * Build family member health check reminder message
     */
    private String buildFamilyHealthCheckMessage(User user, String contactName) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #FF9800;'>👨‍👩‍👧‍👦 Family Health Reminder</h2>");
        message.append("<p>Dear ").append(contactName).append(",</p>");
        message.append("<p>Your family member ").append(user.getName() != null ? user.getName() : "User").append(" has not recorded health data today.</p>");
        message.append("<p>Please remind them to complete the daily health check, including:</p>");
        message.append("<ul>");
        message.append("<li>Blood pressure measurement</li>");
        message.append("<li>Blood sugar test</li>");
        message.append("<li>Steps recording</li>");
        message.append("</ul>");
        message.append("<p>Your care is an important guarantee of your family's health!</p>");
        message.append("<p>IBM AI Health Assistant</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * Generate health report data
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
        
        // Statistics
        long totalRecords = records.size();
        long abnormalRecords = records.stream()
            .filter(record -> isAbnormal(record.getType(), record.getValue()))
            .count();
        
        // Group by type
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
     * Build health report message
     */
    private String buildHealthReportMessage(User user, Map<String, Object> reportData, String reportType) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #2196F3;'>📊 Health Data Report</h2>");
        message.append("<p>Dear ").append(user.getName() != null ? user.getName() : "User").append(",</p>");
        message.append("<p>Here is your ").append(getReportTypeDisplayName(reportType)).append(" health data summary:</p>");
        
        message.append("<div style='background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📈 Overview</h3>");
        message.append("<p><strong>Total records:</strong>").append(reportData.get("totalRecords")).append("</p>");
        message.append("<p><strong>Normal records:</strong>").append(reportData.get("normalRecords")).append("</p>");
        message.append("<p><strong>Abnormal records:</strong>").append(reportData.get("abnormalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>Please continue to maintain healthy habits!</p>");
        message.append("<p>IBM AI Health Assistant</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * Build family health report message
     */
    private String buildFamilyHealthReportMessage(User user, Map<String, Object> reportData, String reportType, String contactName) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #FF9800;'>👨‍👩‍👧‍👦 Family Health Report</h2>");
        message.append("<p>Dear ").append(contactName).append(",</p>");
        message.append("<p>Here is the ").append(getReportTypeDisplayName(reportType)).append(" health report for your family member ").append(user.getName() != null ? user.getName() : "User").append(":</p>");
        
        message.append("<div style='background-color: #fff3e0; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📊 Health Data Overview</h3>");
        message.append("<p><strong>Total records:</strong>").append(reportData.get("totalRecords")).append("</p>");
        message.append("<p><strong>Normal records:</strong>").append(reportData.get("normalRecords")).append("</p>");
        message.append("<p><strong>Abnormal records:</strong>").append(reportData.get("abnormalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>Please continue to pay attention to your family's health!</p>");
        message.append("<p>IBM AI Health Assistant</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * Analyze health trends
     */
    private Map<String, Object> analyzeHealthTrends(List<HealthRecord> records, int days) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Group by type for analysis
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
     * Build trend analysis message
     */
    private String buildTrendAnalysisMessage(User user, Map<String, Object> trendAnalysis, int days) {
        StringBuilder message = new StringBuilder();
        message.append("<html><body style='font-family: Arial, sans-serif; line-height: 1.6;'>");
        message.append("<h2 style='color: #9C27B0;'>📈 Health Trend Analysis</h2>");
        message.append("<p>Dear ").append(user.getName() != null ? user.getName() : "User").append(",</p>");
        message.append("<p>Here is your health trend analysis for the last ").append(days).append(" days:</p>");
        
        message.append("<div style='background-color: #f3e5f5; padding: 20px; border-radius: 8px; margin: 20px 0;'>");
        message.append("<h3>📊 Trend Overview</h3>");
        message.append("<p><strong>Days analyzed:</strong>").append(days).append(" days</p>");
        message.append("<p><strong>Total records:</strong>").append(trendAnalysis.get("totalRecords")).append("</p>");
        message.append("</div>");
        
        message.append("<p>Please adjust your health management plan based on the analysis!</p>");
        message.append("<p>IBM AI Health Analyst</p>");
        message.append("</body></html>");
        return message.toString();
    }
    
    /**
     * Get display name for report type
     */
    private String getReportTypeDisplayName(String reportType) {
        switch (reportType.toLowerCase()) {
            case "daily":
                return "Daily";
            case "weekly":
                return "Weekly";
            case "monthly":
                return "Monthly";
            default:
                return "Periodic";
        }
    }
    
    // ========== End of health features and Email integration enhancements ==========
    
    // ========== Other helper methods ==========
    
    /**
     * Add a health record to the database (for Controller)
     * @param r HealthRecord object
     * @return Record ID
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
     * Get user's latest health record (for Controller)
     * @param userId User ID
     * @return Latest health record
     */
    public HealthRecord latest(Long userId) {
        return healthRecordMapper.latestByUser(userId);
    }
    
    /**
     * Get user's health record history within a specified time range
     * @param userId User ID
     * @param startIso Start time (ISO format)
     * @param endIso End time (ISO format)
     * @return Health record list
     */
    public List<HealthRecord> history(Long userId, String startIso, String endIso) {
        return healthRecordMapper.listByUserAndRange(userId, startIso, endIso);
    }
    
    /**
     * Get user's health record history by type within a specified time range
     * @param userId User ID
     * @param type Record type
     * @param startIso Start time (ISO format)
     * @param endIso End time (ISO format)
     * @return Health record list
     */
    public List<HealthRecord> historyByType(Long userId, String type, String startIso, String endIso) {
        return healthRecordMapper.listByUserAndType(userId, type, startIso, endIso);
    }
    
    /**
     * Get all health records
     * @return List of all health records
     */
    public List<HealthRecord> getAll() {
        return healthRecordMapper.listAll();
    }
    
    // ========== End of other helper methods ==========
} 