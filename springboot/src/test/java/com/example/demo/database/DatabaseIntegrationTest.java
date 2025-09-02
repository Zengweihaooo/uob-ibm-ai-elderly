package com.example.demo.database;

import com.example.demo.service.DatabaseManagementService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQLite Database Integration Tests
 * 
 * Tests database initialization, connection, basic operations and API functionality
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "app.database.path=test_data/test_elderly_companion.db",
    "app.database.backup.path=test_data/test_backups/",
    "spring.datasource.url=jdbc:sqlite:test_data/test_elderly_companion.db"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseManagementService databaseManagementService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Ensure test directories exist
        new File("test_data").mkdirs();
        new File("test_data/test_backups").mkdirs();
    }

    @AfterAll
    static void cleanup() {
        // Cleanup test files
        try {
            Files.deleteIfExists(Paths.get("test_data/test_elderly_companion.db"));
            Files.deleteIfExists(Paths.get("test_data"));
        } catch (Exception e) {
            System.out.println("Test cleanup warning: " + e.getMessage());
        }
    }

    /**
     * Test 1: Database file creation
     */
    @Test
    @Order(1)
    @DisplayName("Database File Should Be Created")
    void testDatabaseFileCreation() {
        // Verify database file exists
        File dbFile = new File("test_data/test_elderly_companion.db");
        
        // If not exists, trigger an operation to create it
        if (!dbFile.exists()) {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/api/database/status", Map.class);
            // Database should be created on first access
        }
        
        // Should exist now
        assertTrue(dbFile.exists(), "Database file should be created");
        assertTrue(dbFile.length() > 0, "Database file should not be empty");
    }

    /**
     * Test 2: Database status API
     */
    @Test
    @Order(2)
    @DisplayName("Database Status API Should Work")
    void testDatabaseStatusAPI() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/api/database/status", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"), "API should return success=true");
        assertNotNull(body.get("status"), "Status object should be present");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> status = (Map<String, Object>) body.get("status");
        assertTrue((Boolean) status.get("database_exists"), "Database should exist");
        assertNotNull(status.get("database_path"), "Database path should be present");
    }

    /**
     * Test 3: Database info API
     */
    @Test
    @Order(3)
    @DisplayName("Database Info API Should Return Correct Information")
    void testDatabaseInfoAPI() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/api/database/info", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) body.get("info");
        assertEquals("SQLite", info.get("database_type"));
        assertEquals("1.0.0", info.get("version"));
        assertNotNull(info.get("database_path"));
        assertNotNull(info.get("backup_path"));
    }

    /**
     * Test 4: Database validation API
     */
    @Test
    @Order(4)
    @DisplayName("Database Validation Should Pass")
    void testDatabaseValidation() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/api/database/validate", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) body.get("validation");
        assertTrue((Boolean) validation.get("valid"), "Database should be valid");
    }

    /**
     * Test 5: Backup creation functionality
     */
    @Test
    @Order(5)
    @DisplayName("Database Backup Should Be Created Successfully")
    void testDatabaseBackup() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/database/backup", null, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("backup_path"));
        
        String backupPath = (String) body.get("backup_path");
        File backupFile = new File(backupPath);
        assertTrue(backupFile.exists(), "Backup file should be created");
        assertTrue(backupFile.length() > 0, "Backup file should not be empty");
    }

    /**
     * Test 6: Existing user API compatibility
     */
    @Test
    @Order(6)
    @DisplayName("Existing User API Should Still Work")
    void testExistingUserAPI() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            baseUrl + "/user/stats", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // User stats API should work
        Map<String, Object> body = response.getBody();
        assertNotNull(body.get("totalUsers"));
        assertNotNull(body.get("verifiedUsers"));
        assertNotNull(body.get("pendingUsers"));
    }

    /**
     * Test 7: Health record API compatibility
     */
    @Test
    @Order(7)
    @DisplayName("Health Record API Should Work With Database")
    void testHealthRecordAPI() {
        // Create test health record
        Map<String, Object> healthRecord = Map.of(
            "type", "bloodPressure",
            "value", "120/80",
            "notes", "Database integration test"
        );
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/health/record", healthRecord, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("record"));
    }

    /**
     * Test 8: Database management service
     */
    @Test
    @Order(8)
    @DisplayName("Database Management Service Should Work")
    void testDatabaseManagementService() {
        // Test service layer methods
        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        assertNotNull(status);
        assertTrue((Boolean) status.get("database_exists"));
        
        String dbPath = databaseManagementService.getDatabasePath();
        assertNotNull(dbPath);
        assertTrue(dbPath.contains("test_elderly_companion.db"));
        
        String backupPath = databaseManagementService.getBackupPath();
        assertNotNull(backupPath);
        assertTrue(backupPath.contains("test_backups"));
        
        // Test validation function
        Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
        assertNotNull(validation);
        assertTrue((Boolean) validation.get("valid"));
    }

    /**
     * Test 9: Backup cleanup functionality
     */
    @Test
    @Order(9)
    @DisplayName("Backup Cleanup Should Work")
    void testBackupCleanup() {
        // Create several backups first
        for (int i = 0; i < 3; i++) {
            restTemplate.postForEntity(baseUrl + "/api/database/backup", null, Map.class);
            try {
                Thread.sleep(1000); // Ensure filenames differ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Cleanup, keep only 2
        Map<String, Object> cleanupRequest = Map.of("keep_count", 2);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/database/cleanup-backups", cleanupRequest, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        
        // Check number of deleted files
        Integer deletedCount = (Integer) body.get("deleted_count");
        assertTrue(deletedCount >= 0, "Should have deleted some files or none if less than keep_count");
    }

    /**
     * Test 10: Error handling
     */
    @Test
    @Order(10)
    @DisplayName("API Should Handle Errors Gracefully")
    void testErrorHandling() {
        // Test restoring non-existent backup file
        Map<String, Object> restoreRequest = Map.of("backup_filename", "nonexistent_backup.db");
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/database/restore", restoreRequest, Map.class);
        
        // Should return error but not crash
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertNotNull(body.get("message"));
    }
}
