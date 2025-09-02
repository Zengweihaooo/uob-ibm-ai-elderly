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
 * 
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
        // Set private field values via reflection
        ReflectionTestUtils.setField(databaseManagementService, "databasePath", TEST_DB_PATH);
        ReflectionTestUtils.setField(databaseManagementService, "backupPath", TEST_BACKUP_PATH);
        
        // Create test directories
        new File("test_data").mkdirs();
        new File(TEST_BACKUP_PATH).mkdirs();
    }

    @AfterEach
    void cleanUp() {
        // Cleanup test files
        try {
            Files.deleteIfExists(Paths.get(TEST_DB_PATH));
            // Cleanup files in backup directory
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
     * Test 3: Database status when database doesn't exist
     */
    @Test
    @Order(3)
    @DisplayName("Should Return Correct Status When Database Doesn't Exist")
    void testGetDatabaseStatusWhenNotExists() {
        // Ensure database file does not exist
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
     * Test 4: Database status when database exists
     */
    @Test
    @Order(4)
    @DisplayName("Should Return Correct Status When Database Exists")
    void testGetDatabaseStatusWhenExists() throws IOException {
        // Create a test database file
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
     * Test 5: Database validation when file doesn't exist
     */
    @Test
    @Order(5)
    @DisplayName("Should Return Invalid When Database File Doesn't Exist")
    void testValidateDatabaseWhenNotExists() {
        // Ensure database file does not exist
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
     * Test 6: Database validation when file exists
     */
    @Test
    @Order(6)
    @DisplayName("Should Return Valid When Database File Exists")
    void testValidateDatabaseWhenExists() throws IOException {
        // Create a test database file
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
     * Test 7: Create backup
     */
    @Test
    @Order(7)
    @DisplayName("Should Create Backup Successfully")
    void testCreateBackup() throws IOException {
        // Create a test database file
        File dbFile = new File(TEST_DB_PATH);
        dbFile.getParentFile().mkdirs();
        String testContent = "test database content for backup";
        Files.write(dbFile.toPath(), testContent.getBytes());

        String backupPath = databaseManagementService.createBackup();
        
        assertNotNull(backupPath);
        assertTrue(backupPath.startsWith(TEST_BACKUP_PATH));
        assertTrue(backupPath.endsWith(".db"));
        
        // Verify backup file exists
        File backupFile = new File(backupPath);
        assertTrue(backupFile.exists());
        
        // Verify backup file content
        String backupContent = new String(Files.readAllBytes(backupFile.toPath()));
        assertEquals(testContent, backupContent);
    }

    /**
     * Test 8: Create backup when database doesn't exist
     */
    @Test
    @Order(8)
    @DisplayName("Should Throw Exception When Creating Backup Of Non-Existent Database")
    void testCreateBackupWhenDatabaseNotExists() {
        // Ensure database file does not exist
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
     * Test 9: Restore backup
     */
    @Test
    @Order(9)
    @DisplayName("Should Restore Backup Successfully")
    void testRestoreBackup() throws IOException {
        // Create a backup file
        String testContent = "backup database content";
        String backupFileName = "test_backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".db";
        Path backupFilePath = Paths.get(TEST_BACKUP_PATH, backupFileName);
        
        Files.createDirectories(backupFilePath.getParent());
        Files.write(backupFilePath, testContent.getBytes());

        boolean result = databaseManagementService.restoreFromBackup(backupFileName);
        
        assertTrue(result);
        
        // Verify that database file is created
        File dbFile = new File(TEST_DB_PATH);
        assertTrue(dbFile.exists());
        
        // Verify restored content
        String restoredContent = new String(Files.readAllBytes(dbFile.toPath()));
        assertEquals(testContent, restoredContent);
    }

    /**
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
     * Test 11: Cleanup old backups
     */
    @Test
    @Order(11)
    @DisplayName("Should Cleanup Old Backups Correctly")
    void testCleanupOldBackups() throws IOException, InterruptedException {
        // Create multiple backup files
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
            // Small delay to ensure different timestamps
            Thread.sleep(10);
        }

        int deletedCount = databaseManagementService.cleanupOldBackups(3);
        
        // Should delete 2 oldest files
        assertEquals(2, deletedCount);
        
        // Verify remaining files
        File backupDir = new File(TEST_BACKUP_PATH);
        File[] remainingFiles = backupDir.listFiles();
        assertNotNull(remainingFiles);
        assertEquals(3, remainingFiles.length);
    }

    /**
     * Test 12: Database status includes backup information
     */
    @Test
    @Order(12)
    @DisplayName("Should Include Backup Information In Database Status")
    void testDatabaseStatusIncludesBackupInfo() throws IOException {
        // Create a few backup files
        String[] backupNames = {
            "backup_20240101_120000.db",
            "backup_20240102_120000.db",
            "notabackup.txt" // This should not be counted
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
        
        // Should only count .db files
        assertEquals(2, status.get("backup_count"));
        assertEquals(true, status.get("backup_directory_exists"));
    }
}
