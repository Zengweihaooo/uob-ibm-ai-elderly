package com.example.demo.config;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

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
    public void ensureDatabaseDirectoryExists() {
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
            // 检查数据库文件是否存在
            File dbFile = new File(databasePath);
            if (!dbFile.exists()) {
                System.out.println("Database file not found. Initializing new database...");
                
                // 设置初始化脚本
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                
                // 如果有schema.sql文件，使用它来初始化
                try {
                    ClassPathResource schemaResource = new ClassPathResource("schema.sql");
                    if (schemaResource.exists()) {
                        populator.addScript(schemaResource);
                        System.out.println("Found schema.sql, using it for initialization");
                    }
                } catch (Exception e) {
                    System.out.println("No schema.sql found, will rely on Hibernate DDL");
                }

                // 如果有data.sql文件，使用它来插入初始数据
                try {
                    ClassPathResource dataResource = new ClassPathResource("data.sql");
                    if (dataResource.exists()) {
                        populator.addScript(dataResource);
                        System.out.println("Found data.sql, using it for initial data");
                    }
                } catch (Exception e) {
                    System.out.println("No data.sql found, skipping initial data insertion");
                }

                populator.setSeparator(";");
                populator.setCommentPrefix("--");
                initializer.setDatabasePopulator(populator);
            } else {
                System.out.println("Database file exists at: " + dbFile.getAbsolutePath());
            }
        }

        return initializer;
    }
}
