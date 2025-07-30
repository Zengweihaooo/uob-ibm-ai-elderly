package com.example.demo.controller;

import com.example.demo.pojo.HealthRecord;
import com.example.demo.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private HealthService healthService;

    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> addHealthRecord(
            @RequestBody Map<String, String> recordData,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // TODO: Extract userId from JWT token
            Long userId = 1L;

            String type = recordData.get("type"); // bloodPressure, bloodSugar, steps
            String value = recordData.get("value");

            if (type == null || value == null) {
                response.put("success", false);
                response.put("message", "Type and value are required");
                return ResponseEntity.badRequest().body(response);
            }

            HealthRecord record = healthService.addHealthRecord(userId, type, value);

            // Check for abnormal values and send email notification
            boolean isAbnormal = healthService.checkAbnormalAndNotify(type, value, userId);
            
            if (isAbnormal) {
                response.put("alert", "Abnormal value detected! Email notification has been sent to emergency contact.");
                response.put("abnormal", true);
            } else {
                response.put("abnormal", false);
            }

            response.put("success", true);
            response.put("record", record);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to record health data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayHealthRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = 1L;

            List<HealthRecord> records = healthService.getTodayRecords(userId);
            response.put("success", true);
            response.put("records", records);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching today's health data");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get health data statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHealthStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Map<String, Object> response = new HashMap<>();

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Long userId = 1L;
            List<HealthRecord> todayRecords = healthService.getTodayRecords(userId);
            
            // Count abnormal records for today
            long abnormalCount = todayRecords.stream()
                .filter(record -> healthService.isAbnormal(record.getType(), record.getValue()))
                .count();
            
            response.put("success", true);
            response.put("totalRecords", todayRecords.size());
            response.put("abnormalCount", abnormalCount);
            response.put("normalCount", todayRecords.size() - abnormalCount);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching health statistics");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
