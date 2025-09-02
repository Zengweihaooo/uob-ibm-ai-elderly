package com.example.demo.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Memo database initializer
 * Ensures memos table is created when application starts
 */
@Component
public class MemoSchemaInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Initializing memo schema...");
            
            // Create table
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS memos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title VARCHAR(255) NOT NULL,
                    content TEXT NOT NULL,
                    type VARCHAR(50) NOT NULL DEFAULT 'general',
                    is_important BOOLEAN NOT NULL DEFAULT 0,
                    pin_code VARCHAR(10),
                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_deleted BOOLEAN NOT NULL DEFAULT 0
                )
            """);
            
            // Create indexes
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_id ON memos (user_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_deleted ON memos (user_id, is_deleted)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_update_time ON memos (update_time)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_type ON memos (type)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_important ON memos (is_important)");
            
            // Insert test data
            List<String> insertStatements = Arrays.asList(
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, 'Important Reminder', 'Tomorrow go to hospital for physical examination, remember to bring ID card and medical insurance card', 'important', 1, '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, 'Shopping List', 'Milk, bread, eggs, vegetables', 'todo', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, 'Daily Record', 'Today weather is good, suitable for walking', 'general', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, 'Important Matter', 'Bank password: 123456, please keep safe', 'important', 1, '5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
            );
            
            for (String insertSql : insertStatements) {
                jdbcTemplate.execute(insertSql);
            }
            
            System.out.println("Memo schema initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Failed to initialize memo schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 