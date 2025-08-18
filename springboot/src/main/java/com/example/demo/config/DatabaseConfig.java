package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.io.File;

/**
 * Database Configuration for SQLite
 * SQLite数据库配置类
 * 
 * 负责初始化数据库文件和表结构
 * Responsible for initializing database file and table structure
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Configuration
public class DatabaseConfig {

    @Value("${app.database.path:data/elderly_companion.db}")
    private String databasePath;

    @Value("${app.database.backup.path:data/backups/}")
    private String backupPath;

    @Value("${app.database.init-schema:true}")
    private boolean initSchema;

    /**
     * 确保数据库目录存在
     * Ensure database directory exists
     */
    @Bean
    public String ensureDatabaseDirectoryExists() {
        // 创建数据库文件目录
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Created database directory: " + dataDir.getAbsolutePath());
            }
        }

        // 创建备份目录
        File backupDir = new File(backupPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (created) {
                System.out.println("Created backup directory: " + backupDir.getAbsolutePath());
            }
        }

        System.out.println("Database will be created at: " + new File(databasePath).getAbsolutePath());
        return "Database directories initialized";
    }

    /**
     * 数据库初始化器
     * Database initializer
     * 
     * 当数据库文件不存在时，自动创建表结构
     * Automatically creates table structure when database file doesn't exist
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        if (initSchema) {
            // 始终执行 schema.sql（包含 CREATE TABLE IF NOT EXISTS，幂等安全），确保新增表被创建
            File dbFile = new File(databasePath);
            System.out.println((dbFile.exists() ? "Database file exists at: " : "Database file not found. Initializing new database at: ") + dbFile.getAbsolutePath());

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            // schema.sql（表结构）
            try {
                ClassPathResource schemaResource = new ClassPathResource("schema.sql");
                if (schemaResource.exists()) {
                    populator.addScript(schemaResource);
                    System.out.println("Applying schema.sql for database structure (idempotent)");
                } else {
                    System.out.println("schema.sql not found, will rely on Hibernate DDL for JPA entities only");
                }
            } catch (Exception e) {
                System.out.println("Failed to load schema.sql: " + e.getMessage());
            }
            // data.sql（初始数据，建议使用 INSERT OR IGNORE）
            try {
                ClassPathResource dataResource = new ClassPathResource("data.sql");
                if (dataResource.exists()) {
                    populator.addScript(dataResource);
                    System.out.println("Applying data.sql for seed data (should be idempotent)");
                }
            } catch (Exception e) {
                System.out.println("No data.sql found or failed to load: " + e.getMessage());
            }

            populator.setSeparator(";");
            populator.setCommentPrefix("--");
            initializer.setDatabasePopulator(populator);
        }

        return initializer;
    }
}
