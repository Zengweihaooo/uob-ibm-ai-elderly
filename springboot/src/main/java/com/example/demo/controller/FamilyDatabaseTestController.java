package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;
import com.example.demo.service.FamilyService;

/**
 * Test controller for family database functionality
 * 
 * This controller provides endpoints to test and verify that
 * family contacts are properly stored in and retrieved from the database.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@RestController
@RequestMapping("/api/test/family-db")
@CrossOrigin(origins = "*")
public class FamilyDatabaseTestController {

    @Autowired
    private FamilyService familyService;
    
    @Autowired
    private FamilyContactMapper familyContactMapper;

    /**
     * Test database connection and get statistics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test database connection by counting contacts
            int totalContacts = familyContactMapper.countByUserId(1L);
            
            response.put("success", true);
            response.put("message", "Database connection successful");
            response.put("totalContactsForUser1", totalContacts);
            
            // Get all contacts for user 1
            List<FamilyContact> contacts = familyContactMapper.findByUserId(1L);
            response.put("contacts", contacts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Database connection failed: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test adding a contact through service (which uses database)
     */
    @PostMapping("/add-test-contact")
    public ResponseEntity<Map<String, Object>> addTestContact() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Add a test contact
            FamilyContact testContact = familyService.addFamilyContact(
                1L,  // userId
                "测试联系人_" + System.currentTimeMillis(),
                "+86 13800138000",
                "test@example.com",
                "朋友",
                false
            );
            
            response.put("success", true);
            response.put("message", "Test contact added successfully");
            response.put("contact", testContact);
            
            // Verify it's in database
            FamilyContact dbContact = familyContactMapper.findById(testContact.getId());
            response.put("verifiedInDatabase", dbContact != null);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to add test contact: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get all contacts for a specific user directly from database
     */
    @GetMapping("/user/{userId}/contacts")
    public ResponseEntity<Map<String, Object>> getUserContactsFromDb(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get from database directly
            List<FamilyContact> dbContacts = familyContactMapper.findByUserId(userId);
            
            // Get from service (which should also use database now)
            List<FamilyContact> serviceContacts = familyService.getFamilyContacts(userId);
            
            response.put("success", true);
            response.put("directFromDatabase", dbContacts);
            response.put("fromService", serviceContacts);
            response.put("databaseCount", dbContacts.size());
            response.put("serviceCount", serviceContacts.size());
            response.put("countsMatch", dbContacts.size() == serviceContacts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get contacts: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Test emergency contacts retrieval
     */
    @GetMapping("/user/{userId}/emergency-contacts")
    public ResponseEntity<Map<String, Object>> getEmergencyContacts(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get emergency contacts from database
            List<FamilyContact> dbEmergencyContacts = familyContactMapper.findEmergencyContactsByUserId(userId);
            
            // Get emergency contacts from service
            List<FamilyContact> serviceEmergencyContacts = familyService.getEmergencyContacts(userId);
            
            response.put("success", true);
            response.put("directFromDatabase", dbEmergencyContacts);
            response.put("fromService", serviceEmergencyContacts);
            response.put("count", dbEmergencyContacts.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get emergency contacts: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Clear all contacts for testing (be careful!)
     */
    @PostMapping("/clear-user/{userId}")
    public ResponseEntity<Map<String, Object>> clearUserContacts(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FamilyContact> contacts = familyContactMapper.findByUserId(userId);
            int deletedCount = 0;
            
            for (FamilyContact contact : contacts) {
                familyContactMapper.deleteById(contact.getId());
                deletedCount++;
            }
            
            response.put("success", true);
            response.put("message", "Cleared " + deletedCount + " contacts for user " + userId);
            response.put("deletedCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear contacts: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}
