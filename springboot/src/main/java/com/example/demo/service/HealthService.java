package com.example.demo.service;

import com.example.demo.pojo.HealthRecord;
import com.example.demo.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HealthService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

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
} 