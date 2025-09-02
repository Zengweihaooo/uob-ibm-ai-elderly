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
 * Database Configuration for SQLite.
 * Responsible for initializing the database file and table structure.
 * 
 * Author: Weihao Zeng
 * Version: 1.0
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
     * Ensure database (and backup) directories exist.
     */
    @Bean
    public String ensureDatabaseDirectoryExists() {
    // Create database directory
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Created database directory: " + dataDir.getAbsolutePath());
            }
        }

    // Create backup directory
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
     * Database initializer.
     * Automatically applies schema and seed scripts (idempotent) if enabled.
     */
    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        if (initSchema) {
            // Always execute schema.sql (contains CREATE TABLE IF NOT EXISTS; idempotent) to ensure new tables are created.
            File dbFile = new File(databasePath);
            System.out.println((dbFile.exists() ? "Database file exists at: " : "Database file not found. Initializing new database at: ") + dbFile.getAbsolutePath());

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            // schema.sql (table structure)
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
            // data.sql (seed data; should use INSERT OR IGNORE / idempotent inserts)
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
