package com.example.demo.database;

import com.example.demo.service.DatabaseManagementService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Management Service Unit Tests
 * 数据库管理服务单元测试
 * 
 * 测试数据库管理服务的各种功能，使用模拟对象进行隔离测试
 * Tests various functionalities of the database management service using mocks for isolated testing
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseManagementServiceTest {

    @InjectMocks
    private DatabaseManagementService databaseManagementService;

    private static final String TEST_DB_PATH = "test_data/test_unit_elderly_companion.db";
    private static final String TEST_BACKUP_PATH = "test_data/test_unit_backups/";

    @BeforeEach
    void setUp() {
        // 使用反射设置私有字段值
        ReflectionTestUtils.setField(databaseManagementService, "databasePath", TEST_DB_PATH);
        ReflectionTestUtils.setField(databaseManagementService, "backupPath", TEST_BACKUP_PATH);
        
        // 创建测试目录
        new File("test_data").mkdirs();
        new File(TEST_BACKUP_PATH).mkdirs();
    }

    @AfterEach
    void cleanUp() {
        // 清理测试文件
        try {
            Files.deleteIfExists(Paths.get(TEST_DB_PATH));
            // 清理备份目录中的文件
            File backupDir = new File(TEST_BACKUP_PATH);
            if (backupDir.exists()) {
                File[] files = backupDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                backupDir.delete();
            }
            Files.deleteIfExists(Paths.get("test_data"));
        } catch (IOException e) {
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    /**
     * 测试1: 获取数据库路径
     * Test 1: Get database path
     */
    @Test
    @Order(1)
    @DisplayName("Should Return Correct Database Path")
    void testGetDatabasePath() {
        String path = databaseManagementService.getDatabasePath();
        assertEquals(TEST_DB_PATH, path);
    }

    /**
     * 测试2: 获取备份路径
     * Test 2: Get backup path
     */
    @Test
    @Order(2)
    @DisplayName("Should Return Correct Backup Path")
    void testGetBackupPath() {
        String path = databaseManagementService.getBackupPath();
        assertEquals(TEST_BACKUP_PATH, path);
    }

    /**
     * 测试3: 数据库不存在时的状态
     * Test 3: Database status when database doesn't exist
     */
    @Test
    @Order(3)
    @DisplayName("Should Return Correct Status When Database Doesn't Exist")
    void testGetDatabaseStatusWhenNotExists() {
        // 确保数据库文件不存在
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        
        assertNotNull(status);
        assertEquals(false, status.get("database_exists"));
        assertEquals(TEST_DB_PATH, status.get("database_path"));
        assertEquals(TEST_BACKUP_PATH, status.get("backup_path"));
        assertNotNull(status.get("check_time"));
    }

    /**
     * 测试4: 数据库存在时的状态
     * Test 4: Database status when database exists
     */
    @Test
    @Order(4)
    @DisplayName("Should Return Correct Status When Database Exists")
    void testGetDatabaseStatusWhenExists() throws IOException {
        // 创建一个测试数据库文件
        File dbFile = new File(TEST_DB_PATH);
        dbFile.getParentFile().mkdirs();
        Files.write(dbFile.toPath(), "test database content".getBytes());

        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        
        assertNotNull(status);
        assertEquals(true, status.get("database_exists"));
        assertEquals(TEST_DB_PATH, status.get("database_path"));
        assertTrue((Long) status.get("database_size") > 0);
        assertNotNull(status.get("last_modified"));
    }

    /**
     * 测试5: 数据库验证 - 文件不存在
     * Test 5: Database validation when file doesn't exist
     */
    @Test
    @Order(5)
    @DisplayName("Should Return Invalid When Database File Doesn't Exist")
    void testValidateDatabaseWhenNotExists() {
        // 确保数据库文件不存在
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
        
        assertNotNull(validation);
        assertEquals(false, validation.get("valid"));
        assertNotNull(validation.get("error"));
        assertTrue(validation.get("error").toString().contains("not exist"));
    }

    /**
     * 测试6: 数据库验证 - 文件存在
     * Test 6: Database validation when file exists
     */
    @Test
    @Order(6)
    @DisplayName("Should Return Valid When Database File Exists")
    void testValidateDatabaseWhenExists() throws IOException {
        // 创建一个测试数据库文件
        File dbFile = new File(TEST_DB_PATH);
        dbFile.getParentFile().mkdirs();
        Files.write(dbFile.toPath(), "test database content".getBytes());

        Map<String, Object> validation = databaseManagementService.validateDatabaseIntegrity();
        
        assertNotNull(validation);
        assertEquals(true, validation.get("valid"));
        assertNotNull(validation.get("file_size"));
        assertTrue((Long) validation.get("file_size") > 0);
    }

    /**
     * 测试7: 创建备份
     * Test 7: Create backup
     */
    @Test
    @Order(7)
    @DisplayName("Should Create Backup Successfully")
    void testCreateBackup() throws IOException {
        // 创建一个测试数据库文件
        File dbFile = new File(TEST_DB_PATH);
        dbFile.getParentFile().mkdirs();
        String testContent = "test database content for backup";
        Files.write(dbFile.toPath(), testContent.getBytes());

        String backupPath = databaseManagementService.createBackup();
        
        assertNotNull(backupPath);
        assertTrue(backupPath.startsWith(TEST_BACKUP_PATH));
        assertTrue(backupPath.endsWith(".db"));
        
        // 验证备份文件存在
        File backupFile = new File(backupPath);
        assertTrue(backupFile.exists());
        
        // 验证备份内容
        String backupContent = new String(Files.readAllBytes(backupFile.toPath()));
        assertEquals(testContent, backupContent);
    }

    /**
     * 测试8: 创建备份 - 数据库不存在
     * Test 8: Create backup when database doesn't exist
     */
    @Test
    @Order(8)
    @DisplayName("Should Throw Exception When Creating Backup Of Non-Existent Database")
    void testCreateBackupWhenDatabaseNotExists() {
        // 确保数据库文件不存在
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }

        Exception exception = assertThrows(RuntimeException.class, () -> {
            databaseManagementService.createBackup();
        });
        
        assertTrue(exception.getMessage().contains("Database file does not exist"));
    }

    /**
     * 测试9: 恢复备份
     * Test 9: Restore backup
     */
    @Test
    @Order(9)
    @DisplayName("Should Restore Backup Successfully")
    void testRestoreBackup() throws IOException {
        // 创建一个备份文件
        String testContent = "backup database content";
        String backupFileName = "test_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
        Path backupFilePath = Paths.get(TEST_BACKUP_PATH, backupFileName);
        
        Files.createDirectories(backupFilePath.getParent());
        Files.write(backupFilePath, testContent.getBytes());

        boolean result = databaseManagementService.restoreFromBackup(backupFileName);
        
        assertTrue(result);
        
        // 验证数据库文件被创建
        File dbFile = new File(TEST_DB_PATH);
        assertTrue(dbFile.exists());
        
        // 验证恢复的内容
        String restoredContent = new String(Files.readAllBytes(dbFile.toPath()));
        assertEquals(testContent, restoredContent);
    }

    /**
     * 测试10: 恢复备份 - 备份文件不存在
     * Test 10: Restore backup when backup file doesn't exist
     */
    @Test
    @Order(10)
    @DisplayName("Should Handle Exception When Restoring Non-Existent Backup")
    void testRestoreBackupWhenFileNotExists() {
        Exception exception = assertThrows(IOException.class, () -> {
            databaseManagementService.restoreFromBackup("nonexistent_backup.db");
        });
        assertTrue(exception.getMessage().contains("Backup file does not exist"));
    }

    /**
     * 测试11: 清理旧备份
     * Test 11: Cleanup old backups
     */
    @Test
    @Order(11)
    @DisplayName("Should Cleanup Old Backups Correctly")
    void testCleanupOldBackups() throws IOException, InterruptedException {
        // 创建多个备份文件
        String[] backupNames = {
            "backup_20240101_120000.db",
            "backup_20240102_120000.db", 
            "backup_20240103_120000.db",
            "backup_20240104_120000.db",
            "backup_20240105_120000.db"
        };
        
        for (String name : backupNames) {
            Path backupPath = Paths.get(TEST_BACKUP_PATH, name);
            Files.write(backupPath, ("content of " + name).getBytes());
            // 稍微延迟以确保文件时间戳不同
            Thread.sleep(10);
        }

        int deletedCount = databaseManagementService.cleanupOldBackups(3);
        
        // 应该删除2个最老的文件
        assertEquals(2, deletedCount);
        
        // 验证剩余的文件
        File backupDir = new File(TEST_BACKUP_PATH);
        File[] remainingFiles = backupDir.listFiles();
        assertNotNull(remainingFiles);
        assertEquals(3, remainingFiles.length);
    }

    /**
     * 测试12: 数据库状态包含备份信息
     * Test 12: Database status includes backup information
     */
    @Test
    @Order(12)
    @DisplayName("Should Include Backup Information In Database Status")
    void testDatabaseStatusIncludesBackupInfo() throws IOException {
        // 创建几个备份文件
        String[] backupNames = {
            "backup_20240101_120000.db",
            "backup_20240102_120000.db",
            "notabackup.txt" // 这个不应该被计算
        };
        
        new File(TEST_BACKUP_PATH).mkdirs();
        for (String name : backupNames) {
            Path backupPath = Paths.get(TEST_BACKUP_PATH, name);
            Files.write(backupPath, ("content of " + name).getBytes());
        }

        Map<String, Object> status = databaseManagementService.getDatabaseStatus();
        
        assertNotNull(status);
        assertTrue(status.containsKey("backup_count"));
        assertTrue(status.containsKey("backup_directory_exists"));
        
        // 应该只计算.db文件
        assertEquals(2, status.get("backup_count"));
        assertEquals(true, status.get("backup_directory_exists"));
    }
}
