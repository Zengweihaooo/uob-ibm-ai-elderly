-- Database initialization script for IBM AI Elderly Project
-- This script creates the necessary tables for the application

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    verification_code VARCHAR(10),
    status VARCHAR(20) DEFAULT 'UNREGISTERED',
    role VARCHAR(20) DEFAULT 'ELDERLY',
    phone_number VARCHAR(20),
    is_verified BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    code_expires_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for users
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_verified ON users(is_verified);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- Family Contacts Table
CREATE TABLE IF NOT EXISTS family_contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    notification_preference VARCHAR(20) DEFAULT 'ALL',
    is_emergency_contact BOOLEAN DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_family_contacts_user_id ON family_contacts(user_id);
CREATE INDEX IF NOT EXISTS idx_family_contacts_emergency ON family_contacts(user_id, is_emergency_contact);
CREATE INDEX IF NOT EXISTS idx_family_contacts_active ON family_contacts(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_family_contacts_notification ON family_contacts(user_id, notification_preference);

-- Health Records Table (if not exists)
CREATE TABLE IF NOT EXISTS health_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    unit VARCHAR(50),
    record_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    shared BOOLEAN DEFAULT 0,
    shared_with_user_id BIGINT,
    shared_with_role VARCHAR(50),
    shared_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Schedules Table (if not exists)
CREATE TABLE IF NOT EXISTS schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    priority VARCHAR(20) DEFAULT 'medium',
    is_completed BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chat Messages Table (if not exists)
CREATE TABLE IF NOT EXISTS chat_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'text',
    sender_type VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Important Dates Table (if not exists)
CREATE TABLE IF NOT EXISTS important_dates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    type VARCHAR(50),
    repeat_cycle VARCHAR(20) DEFAULT 'yearly',
    enabled BOOLEAN DEFAULT 1,
    week_reminder_sent TIMESTAMP,
    day_reminder_sent TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_important_dates_user ON important_dates(user_id);
CREATE INDEX IF NOT EXISTS idx_important_dates_date ON important_dates(date);
CREATE INDEX IF NOT EXISTS idx_important_dates_enabled ON important_dates(enabled);

-- Emotion Companion (one record per user for current emotional state)
CREATE TABLE IF NOT EXISTS emotion_companion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT,
    personality TEXT,
    avatar TEXT,
    emotion TEXT,
    happiness INTEGER,
    energy INTEGER,
    responsiveness INTEGER,
    last_interaction TEXT,
    last_chat TEXT,
    last_command TEXT,
    interaction_count INTEGER,
    chat_count INTEGER,
    current_location TEXT,
    is_active INTEGER,
    activity_mode TEXT,
    current_sound TEXT,
    visual_expression TEXT,
    is_making_sound INTEGER,
    is_expressing_emotion INTEGER,
    led_color TEXT,
    last_attention_time TEXT,
    neglect_level INTEGER,
    needs_attention INTEGER,
    is_lonely INTEGER,
    current_task TEXT,
    is_learning INTEGER,
    helpfulness INTEGER,
    created_at TEXT,
    updated_at TEXT
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS ux_emotion_companion_user ON emotion_companion(user_id);

-- Pet Mood Table
CREATE TABLE IF NOT EXISTS pet_mood (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    mood_score INTEGER DEFAULT 0, -- Mood score (-100 to 100)
    happiness INTEGER DEFAULT 85, -- Happiness level (0-100)
    health INTEGER DEFAULT 92,    -- Health level (0-100)
    energy INTEGER DEFAULT 78,    -- Energy level (0-100)
    mood_emoji TEXT DEFAULT '😊', -- Mood emoji
    status TEXT DEFAULT 'Happy & Healthy', -- Status description
    level INTEGER DEFAULT 1,      -- Pet level
    experience INTEGER DEFAULT 0, -- Experience points
    last_interaction TEXT,        -- Last interaction time
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS ux_pet_mood_user ON pet_mood(user_id);
CREATE INDEX IF NOT EXISTS idx_pet_mood_score ON pet_mood(mood_score);
CREATE INDEX IF NOT EXISTS idx_pet_mood_last_interaction ON pet_mood(last_interaction);

-- Pet Conversation History Table
CREATE TABLE IF NOT EXISTS pet_conversation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    sender_type TEXT NOT NULL, -- 'user' or 'pet'
    message TEXT NOT NULL,
    message_type TEXT DEFAULT 'text', -- 'text', 'voice', 'emergency'
    timestamp TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_pet_conversation_user ON pet_conversation(user_id);
CREATE INDEX IF NOT EXISTS idx_pet_conversation_timestamp ON pet_conversation(timestamp);
CREATE INDEX IF NOT EXISTS idx_pet_conversation_sender ON pet_conversation(sender_type);

-- Insert sample data for testing
INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, notification_preference, is_emergency_contact) 
VALUES (1, 'Zhang Xiaoming', 'Son', '+86 138 0013 8000', 'xiaoming@example.com', 'ALL', 1);

INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, notification_preference, is_emergency_contact) 
VALUES (1, 'Li Xiaohong', 'Daughter', '+86 139 0013 9000', 'xiaohong@example.com', 'ALL', 1);

INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, notification_preference, is_emergency_contact) 
VALUES (1, 'Dr. Wang', 'Doctor', '+86 137 0013 7000', 'doctor.wang@hospital.com', 'HEALTH_ALERT', 0);

-- Insert sample users
INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('admin', 'admin@example.com', 'hashed_password', 'Administrator', 'VERIFIED', 'ADMIN', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('elderly1', 'elderly1@example.com', 'hashed_password', 'Grandpa Zhang', 'VERIFIED', 'ELDERLY', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('doctor1', 'doctor1@hospital.com', 'hashed_password', 'Dr. Wang', 'VERIFIED', 'DOCTOR', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('family1', 'family1@example.com', 'hashed_password', 'Zhang Xiaoming', 'VERIFIED', 'FAMILY', 1);

-- UK Medical Terms Mapping Table
CREATE TABLE IF NOT EXISTS uk_medical_terms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    colloquial_term TEXT NOT NULL, -- Colloquial expression
    standard_term TEXT NOT NULL, -- Standard medical term
    category TEXT, -- symptom, body_part, medication, location
    urgency_level TEXT, -- low, medium, high, emergency
    region_specific BOOLEAN DEFAULT 0, -- Whether region-specific expression
    confidence_score REAL DEFAULT 1.0 -- Match confidence score
);

-- Create index for UK medical terms
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_colloquial ON uk_medical_terms(colloquial_term);
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_category ON uk_medical_terms(category);
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_urgency ON uk_medical_terms(urgency_level);

-- Create emails table for email composition system
CREATE TABLE IF NOT EXISTS emails (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    from_email TEXT NOT NULL,
    to_email TEXT NOT NULL,
    subject TEXT,
    content TEXT,
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK(status IN ('DRAFT', 'SENT', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    error_message TEXT
);

-- Create contacts table for contact management
CREATE TABLE IF NOT EXISTS contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    phone TEXT,
    relationship TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, email)
);

-- Schema Version Table
CREATE TABLE IF NOT EXISTS schema_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(20) NOT NULL,
    description TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 

-- ===================== Memoir Module Tables (AI Memoir Module) =====================
-- Memoir project table: stores basic information of memoir projects
CREATE TABLE IF NOT EXISTS memoir_project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    owner VARCHAR(100),
    locale VARCHAR(20) DEFAULT 'en-US',
    pin_hash TEXT,                         -- Optional project access PIN hash
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Memoir segment table: each segment corresponds to one answer/organized text for a theme
CREATE TABLE IF NOT EXISTS memoir_segment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    chapter TEXT NOT NULL,
    theme TEXT,
    prompt_id INTEGER,
    order_index INTEGER DEFAULT 0,
    text TEXT,                             -- Transcribed or polished text
    audio_url TEXT,                        -- Storage address of original recording (optional)
    tags TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(project_id) REFERENCES memoir_project(id)
);

CREATE INDEX IF NOT EXISTS idx_memoir_segment_project ON memoir_segment(project_id);
CREATE INDEX IF NOT EXISTS idx_memoir_segment_chapter ON memoir_segment(chapter);

-- Media table: project-related images/audio etc.
CREATE TABLE IF NOT EXISTS memoir_media (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    type TEXT,                             -- image/audio
    url TEXT NOT NULL,
    caption TEXT,
    source TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(project_id) REFERENCES memoir_project(id)
);

-- Export record table: records export history and file addresses
CREATE TABLE IF NOT EXISTS memoir_export (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    format TEXT NOT NULL,                  -- markdown/pdf
    file_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(project_id) REFERENCES memoir_project(id)
);

-- Question bank table: default life stages + themes + question text
CREATE TABLE IF NOT EXISTS memoir_prompt_catalog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chapter TEXT NOT NULL,
    theme TEXT NOT NULL,
    text TEXT NOT NULL,
    locale TEXT DEFAULT 'en-US',
    therapy_type TEXT,                     -- integrative/instrumental
    difficulty INTEGER DEFAULT 1
);

-- Share token table: one-time or time-limited sharing
CREATE TABLE IF NOT EXISTS memoir_share_token (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP,
    scope TEXT DEFAULT 'view',             -- view/export
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(project_id) REFERENCES memoir_project(id)
);

-- Share guard table: extends sharing token security and quota (avoids directly modifying existing tables)
CREATE TABLE IF NOT EXISTS memoir_share_guard (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    share_id INTEGER UNIQUE NOT NULL,      -- Corresponds to memoir_share_token.id
    pin_hash TEXT,                         -- Optional PIN hash (SHA-256/hex)
    max_downloads INTEGER,                 -- Maximum download count (null=unlimited)
    download_count INTEGER DEFAULT 0,      -- Downloaded count
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(share_id) REFERENCES memoir_share_token(id)
);
