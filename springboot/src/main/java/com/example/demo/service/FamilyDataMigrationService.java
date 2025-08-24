package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;

/**
 * Data migration service for family contacts
 * 
 * This service handles migration of family contact data from in-memory storage
 * to database storage during application startup.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Component
@Order(1)  // Execute early in the startup sequence
public class FamilyDataMigrationService implements CommandLineRunner {

    @Autowired
    private FamilyContactMapper familyContactMapper;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Family Data Migration Service Started ===");
        
        try {
            // Check if database already has data
            checkDatabaseStatus();
            
            // Initialize sample data if database is empty (for testing)
            initializeSampleDataIfNeeded();
            
            System.out.println("=== Family Data Migration Service Completed ===");
        } catch (Exception e) {
            System.err.println("Error during family data migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check database status and print statistics
     */
    private void checkDatabaseStatus() {
        try {
            // Count total contacts in database
            List<FamilyContact> allContacts = familyContactMapper.findByUserId(1L);
            System.out.println("Database check - Found " + allContacts.size() + " family contacts for user 1");
            
            if (allContacts.size() > 0) {
                System.out.println("Existing family contacts in database:");
                for (FamilyContact contact : allContacts) {
                    System.out.println("  - " + contact.getName() + " (" + contact.getRelationship() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking database status: " + e.getMessage());
        }
    }

    /**
     * Initialize sample data if database is empty
     */
    private void initializeSampleDataIfNeeded() {
        try {
            // Initialize sample data for multiple users
            Long[] userIds = {1L, 2L, 3L}; // Support multiple users
            
            for (Long userId : userIds) {
                List<FamilyContact> existingContacts = familyContactMapper.findByUserId(userId);
                
                if (existingContacts == null || existingContacts.isEmpty()) {
                    System.out.println("No family contacts found for user " + userId + ". Initializing sample data...");
                    createSampleContactsForUser(userId);
                } else {
                    System.out.println("User " + userId + " already has " + existingContacts.size() + " contacts. Skipping.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing sample data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create sample contacts for a specific user
     */
    private void createSampleContactsForUser(Long userId) {
        try {
            // Create sample contacts for testing
            FamilyContact contact1 = new FamilyContact();
            contact1.setUserId(userId);
            contact1.setName("张三");
            contact1.setRelationship("儿子");
            contact1.setPhone("+86 13800138001");
            contact1.setEmail("zhangsan@example.com");
            contact1.setAddress("北京市朝阳区");
            contact1.setIsEmergencyContact(true);
            contact1.setIsActive(true);
            contact1.setCreatedAt(LocalDateTime.now());
            contact1.setUpdatedAt(LocalDateTime.now());
            
            FamilyContact contact2 = new FamilyContact();
            contact2.setUserId(userId);
            contact2.setName("李四");
            contact2.setRelationship("女儿");
            contact2.setPhone("+86 13800138002");
            contact2.setEmail("lisi@example.com");
            contact2.setAddress("北京市海淀区");
            contact2.setIsEmergencyContact(false);
            contact2.setIsActive(true);
            contact2.setCreatedAt(LocalDateTime.now());
            contact2.setUpdatedAt(LocalDateTime.now());
            
            FamilyContact contact3 = new FamilyContact();
            contact3.setUserId(userId);
            contact3.setName("王医生");
            contact3.setRelationship("家庭医生");
            contact3.setPhone("+86 13800138003");
            contact3.setEmail("doctor.wang@hospital.com");
            contact3.setIsEmergencyContact(true);
            contact3.setIsActive(true);
            contact3.setCreatedAt(LocalDateTime.now());
            contact3.setUpdatedAt(LocalDateTime.now());
            
            // Insert sample contacts
            familyContactMapper.insert(contact1);
            System.out.println("  Added sample contact: " + contact1.getName() + " for user " + userId);
            
            familyContactMapper.insert(contact2);
            System.out.println("  Added sample contact: " + contact2.getName() + " for user " + userId);
            
            familyContactMapper.insert(contact3);
            System.out.println("  Added sample contact: " + contact3.getName() + " for user " + userId);
            
            System.out.println("Sample family contacts initialized successfully for user " + userId + "!");
        } catch (Exception e) {
            System.err.println("Error creating sample contacts for user " + userId + ": " + e.getMessage());
        }
    }

    /**
     * Migrate data from a list (if you have existing in-memory data to migrate)
     * This method can be called manually if needed
     */
    public void migrateFromList(List<FamilyContact> contacts) {
        if (contacts == null || contacts.isEmpty()) {
            System.out.println("No contacts to migrate.");
            return;
        }
        
        System.out.println("Starting migration of " + contacts.size() + " contacts...");
        int successCount = 0;
        int failCount = 0;
        
        for (FamilyContact contact : contacts) {
            try {
                // Reset ID to let database generate new one
                contact.setId(null);
                
                // Ensure timestamps are set
                if (contact.getCreatedAt() == null) {
                    contact.setCreatedAt(LocalDateTime.now());
                }
                if (contact.getUpdatedAt() == null) {
                    contact.setUpdatedAt(LocalDateTime.now());
                }
                
                // Insert into database
                familyContactMapper.insert(contact);
                successCount++;
                System.out.println("  Migrated: " + contact.getName());
            } catch (Exception e) {
                failCount++;
                System.err.println("  Failed to migrate: " + contact.getName() + " - " + e.getMessage());
            }
        }
        
        System.out.println("Migration completed: " + successCount + " succeeded, " + failCount + " failed.");
    }
}
