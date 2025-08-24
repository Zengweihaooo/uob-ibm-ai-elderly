package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.demo.mapper.FamilyContactMapper;
import com.example.demo.pojo.FamilyContact;
import com.example.demo.pojo.User;

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

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Family Contact Service Initialized ===");
        System.out.println("Family contact management ready for multi-user usage");
        System.out.println("Users can add contacts via /api/family/contacts endpoint");
    }

    /**
     * Check database status and print statistics
     * 已弃用 - 不再需要检查特定用户的数据
     */
    @Deprecated
    private void checkDatabaseStatus() {
        try {
            // 统计所有用户的联系人总数，而不是特定用户
            List<User> allUsers = userService.getAllUsers();
            System.out.println("Database check - Found " + allUsers.size() + " registered users");
            
            // 可以统计所有联系人总数，但不显示具体用户信息
            // int totalContacts = familyContactMapper.countAllContacts();
            // System.out.println("Total family contacts in database: " + totalContacts);
            
        } catch (Exception e) {
            System.err.println("Error checking database status: " + e.getMessage());
        }
    }

    /**
     * Initialize sample data if database is empty
     * 已弃用 - 改为手动创建联系人
     */
    @Deprecated
    private void initializeSampleDataIfNeeded() {
        // 不再自动创建测试数据
        System.out.println("Automatic sample data creation disabled");
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
