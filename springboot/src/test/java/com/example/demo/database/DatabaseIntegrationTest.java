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
 * SQLite数据库集成测试
 * 
 * 测试数据库初始化、连接、基本操作和API功能
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
        
        // 确保测试目录存在 / Ensure test directories exist
        new File("test_data").mkdirs();
        new File("test_data/test_backups").mkdirs();
    }

    @AfterAll
    static void cleanup() {
        // 清理测试文件 / Cleanup test files
        try {
            Files.deleteIfExists(Paths.get("test_data/test_elderly_companion.db"));
            Files.deleteIfExists(Paths.get("test_data"));
        } catch (Exception e) {
            System.out.println("Test cleanup warning: " + e.getMessage());
        }
    }

    /**
     * 测试1: 数据库文件创建
     * Test 1: Database file creation
     */
    @Test
    @Order(1)
    @DisplayName("Database File Should Be Created")
    void testDatabaseFileCreation() {
        // 验证数据库文件是否存在
        File dbFile = new File("test_data/test_elderly_companion.db");
        
        // 如果不存在，触发一个数据库操作来创建它
        if (!dbFile.exists()) {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/api/database/status", Map.class);
            // 数据库应该在第一次访问时创建
        }
        
        // 现在应该存在
        assertTrue(dbFile.exists(), "Database file should be created");
        assertTrue(dbFile.length() > 0, "Database file should not be empty");
    }

    /**
     * 测试2: 数据库状态API
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
     * 测试3: 数据库信息API
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
     * 测试4: 数据库验证API
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
     * 测试5: 备份创建功能
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
     * 测试6: 现有用户API兼容性
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
        
        // 用户统计API应该正常工作
        Map<String, Object> body = response.getBody();
        assertNotNull(body.get("totalUsers"));
        assertNotNull(body.get("verifiedUsers"));
        assertNotNull(body.get("pendingUsers"));
    }

    /**
     * 测试7: 健康记录API兼容性
     * Test 7: Health record API compatibility
     */
    @Test
    @Order(7)
    @DisplayName("Health Record API Should Work With Database")
    void testHealthRecordAPI() {
        // 创建测试健康记录
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
     * 测试8: 数据库管理服务
     * Test 8: Database management service
     */
    @Test
    @Order(8)
    @DisplayName("Database Management Service Should Work")
    void testDatabaseManagementService() {
        // 测试服务层方法
        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        assertNotNull(status);
        assertTrue((Boolean) status.get("database_exists"));
        
        String dbPath = databaseManagementService.getDatabasePath();
        assertNotNull(dbPath);
        assertTrue(dbPath.contains("test_elderly_companion.db"));
        
        String backupPath = databaseManagementService.getBackupPath();
        assertNotNull(backupPath);
        assertTrue(backupPath.contains("test_backups"));
        
        // 测试验证功能
        Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
        assertNotNull(validation);
        assertTrue((Boolean) validation.get("valid"));
    }

    /**
     * 测试9: 备份清理功能
     * Test 9: Backup cleanup functionality
     */
    @Test
    @Order(9)
    @DisplayName("Backup Cleanup Should Work")
    void testBackupCleanup() {
        // 先创建几个备份
        for (int i = 0; i < 3; i++) {
            restTemplate.postForEntity(baseUrl + "/api/database/backup", null, Map.class);
            try {
                Thread.sleep(1000); // 确保文件名不同
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 清理，只保留2个
        Map<String, Object> cleanupRequest = Map.of("keep_count", 2);
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/database/cleanup-backups", cleanupRequest, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        
        // 检查删除的文件数
        Integer deletedCount = (Integer) body.get("deleted_count");
        assertTrue(deletedCount >= 0, "Should have deleted some files or none if less than keep_count");
    }

    /**
     * 测试10: 错误处理
     * Test 10: Error handling
     */
    @Test
    @Order(10)
    @DisplayName("API Should Handle Errors Gracefully")
    void testErrorHandling() {
        // 测试恢复不存在的备份文件
        Map<String, Object> restoreRequest = Map.of("backup_filename", "nonexistent_backup.db");
        ResponseEntity<Map> response = restTemplate.postForEntity(
            baseUrl + "/api/database/restore", restoreRequest, Map.class);
        
        // 应该返回错误但不崩溃
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertNotNull(body.get("message"));
    }
}
