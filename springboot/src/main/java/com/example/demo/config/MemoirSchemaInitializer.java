package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Memoir table schema initializer.
 * Ensures that all memoir-related tables exist when the database file already exists.
 */
@Configuration
public class MemoirSchemaInitializer {
    private final JdbcTemplate jdbc;

    public MemoirSchemaInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void ensureTables() {
        // Use IF NOT EXISTS to avoid duplicate creation
        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_project (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "owner VARCHAR(100), " +
                "locale VARCHAR(20) DEFAULT 'en-US', " +
                "pin_hash TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_segment (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "chapter TEXT NOT NULL, " +
                "theme TEXT, " +
                "prompt_id INTEGER, " +
                "order_index INTEGER DEFAULT 0, " +
                "text TEXT, " +
                "audio_url TEXT, " +
                "tags TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES memoir_project(id))");

        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_memoir_segment_project ON memoir_segment(project_id)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_memoir_segment_chapter ON memoir_segment(chapter)");

        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_media (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "type TEXT, " +
                "url TEXT NOT NULL, " +
                "caption TEXT, " +
                "source TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES memoir_project(id))");

        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_export (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "format TEXT NOT NULL, " +
                "file_url TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES memoir_project(id))");

        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_prompt_catalog (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chapter TEXT NOT NULL, " +
                "theme TEXT NOT NULL, " +
                "text TEXT NOT NULL, " +
                "locale TEXT DEFAULT 'en-US', " +
                "therapy_type TEXT, " +
                "difficulty INTEGER DEFAULT 1)");

        jdbc.execute("CREATE TABLE IF NOT EXISTS memoir_share_token (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "project_id INTEGER NOT NULL, " +
                "token TEXT NOT NULL, " +
                "expires_at TIMESTAMP, " +
                "scope TEXT DEFAULT 'view', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(project_id) REFERENCES memoir_project(id))");
    }
}
