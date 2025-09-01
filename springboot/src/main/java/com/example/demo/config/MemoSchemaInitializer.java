package com.example.demo.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 备忘录数据库初始化器
 * 确保memos表在应用启动时被创建
 */
@Component
public class MemoSchemaInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Initializing memo schema...");
            
            // 创建表
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
            
            // 创建索引
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_id ON memos (user_id)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_user_deleted ON memos (user_id, is_deleted)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_update_time ON memos (update_time)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_type ON memos (type)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_important ON memos (is_important)");
            
            // 插入测试数据
            List<String> insertStatements = Arrays.asList(
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, '重要提醒', '明天要去医院做体检，记得带身份证和医保卡', 'important', 1, '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, '购物清单', '牛奶、面包、鸡蛋、蔬菜', 'todo', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, '日常记录', '今天天气很好，适合出去散步', 'general', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES (1, '重要事项', '银行密码：123456，请妥善保管', 'important', 1, '5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
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