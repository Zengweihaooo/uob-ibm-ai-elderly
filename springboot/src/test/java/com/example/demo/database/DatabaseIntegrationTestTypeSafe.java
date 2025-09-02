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
 * SQLite Database Integration Tests (Type Safe Version)
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
public class DatabaseIntegrationTestTypeSafe {

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
            ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
                baseUrl + "/api/database/status", 
                getMapClass()
            );
            // Database should be created on first access
            assertNotNull(response, "Response should not be null");
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
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
            baseUrl + "/api/database/status", 
            getMapClass()
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        
        Object successObj = body.get("success");
        assertTrue(successObj instanceof Boolean && (Boolean) successObj, 
                   "API should return success=true");
        assertNotNull(body.get("status"), "Status object should be present");
        
        Object statusObj = body.get("status");
        if (statusObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> status = (Map<String, Object>) statusObj;
            Object dbExistsObj = status.get("database_exists");
            assertTrue(dbExistsObj instanceof Boolean && (Boolean) dbExistsObj, 
                       "Database should exist");
            assertNotNull(status.get("database_path"), "Database path should be present");
        } else {
            fail("Status should be a Map object");
        }
    }

    /**
     * Test 3: Database info API
     */
    @Test
    @Order(3)
    @DisplayName("Database Info API Should Return Correct Information")
    void testDatabaseInfoAPI() {
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
            baseUrl + "/api/database/info", 
            getMapClass()
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        
        Object successObj = body.get("success");
        assertTrue(successObj instanceof Boolean && (Boolean) successObj);
        
        Object infoObj = body.get("info");
        if (infoObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) infoObj;
            assertEquals("SQLite", info.get("database_type"));
            assertEquals("1.0.0", info.get("version"));
            assertNotNull(info.get("database_path"));
            assertNotNull(info.get("backup_path"));
        } else {
            fail("Info should be a Map object");
        }
    }

    /**
     * Test 4: Database validation API
     */
    @Test
    @Order(4)
    @DisplayName("Database Validation Should Pass")
    void testDatabaseValidation() {
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(
            baseUrl + "/api/database/validate", 
            getMapClass()
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        
        Object successObj = body.get("success");
        assertTrue(successObj instanceof Boolean && (Boolean) successObj);
        
        Object validationObj = body.get("validation");
        if (validationObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> validation = (Map<String, Object>) validationObj;
            Object validObj = validation.get("valid");
            assertTrue(validObj instanceof Boolean && (Boolean) validObj, 
                       "Database should be valid");
        } else {
            fail("Validation should be a Map object");
        }
    }

    /**
     * Test 5: Backup creation functionality
     */
    @Test
    @Order(5)
    @DisplayName("Database Backup Should Be Created Successfully")
    void testDatabaseBackup() {
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            baseUrl + "/api/database/backup", 
            null, 
            getMapClass()
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        
        Object successObj = body.get("success");
        assertTrue(successObj instanceof Boolean && (Boolean) successObj);
        assertNotNull(body.get("backup_path"));
        
        String backupPath = (String) body.get("backup_path");
        File backupFile = new File(backupPath);
        assertTrue(backupFile.exists(), "Backup file should be created");
        assertTrue(backupFile.length() > 0, "Backup file should not be empty");
    }

    /**
     * Test 6: Database management service
     */
    @Test
    @Order(6)
    @DisplayName("Database Management Service Should Work")
    void testDatabaseManagementService() {
        // Test service layer methods
        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        assertNotNull(status);
        Object dbExistsObj = status.get("database_exists");
        assertTrue(dbExistsObj instanceof Boolean && (Boolean) dbExistsObj);
        
        String dbPath = databaseManagementService.getDatabasePath();
        assertNotNull(dbPath);
        assertTrue(dbPath.contains("test_elderly_companion.db"));
        
        String backupPath = databaseManagementService.getBackupPath();
        assertNotNull(backupPath);
        assertTrue(backupPath.contains("test_backups"));
        
        // Test validation function
        Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
        assertNotNull(validation);
        Object validObj = validation.get("valid");
        assertTrue(validObj instanceof Boolean && (Boolean) validObj);
    }

    /**
     * Test 7: Error handling
     */
    @Test
    @Order(7)
    @DisplayName("API Should Handle Errors Gracefully")
    void testErrorHandling() {
        // Test restoring a non-existent backup file
        Map<String, Object> restoreRequest = Map.of("backup_filename", "nonexistent_backup.db");
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            baseUrl + "/api/database/restore", 
            restoreRequest, 
            getMapClass()
        );
        
        // Should return error but not crash
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Error response body should not be null");
        
        Object successObj = body.get("success");
        assertTrue(successObj instanceof Boolean && !(Boolean) successObj);
        assertNotNull(body.get("message"));
    }

    /**
     * Helper method to avoid raw type warnings
     */
    @SuppressWarnings("unchecked")
    private Class<Map<String, Object>> getMapClass() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
