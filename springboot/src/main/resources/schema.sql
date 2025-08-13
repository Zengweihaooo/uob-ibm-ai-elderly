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
    is_emergency_contact BOOLEAN DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_family_contacts_user_id ON family_contacts(user_id);
CREATE INDEX IF NOT EXISTS idx_family_contacts_emergency ON family_contacts(user_id, is_emergency_contact);
CREATE INDEX IF NOT EXISTS idx_family_contacts_active ON family_contacts(user_id, is_active);

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

-- Emotion Companion (一人一条当前情绪状态)
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

-- 索引
CREATE UNIQUE INDEX IF NOT EXISTS ux_emotion_companion_user ON emotion_companion(user_id);

-- Insert sample data for testing
INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, is_emergency_contact) 
VALUES (1, '张小明', '儿子', '+86 138 0013 8000', 'xiaoming@example.com', 1);

INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, is_emergency_contact) 
VALUES (1, '李小红', '女儿', '+86 139 0013 9000', 'xiaohong@example.com', 1);

INSERT OR IGNORE INTO family_contacts (user_id, name, relationship, phone, email, is_emergency_contact) 
VALUES (1, '王医生', '医生', '+86 137 0013 7000', 'doctor.wang@hospital.com', 0);

-- Insert sample users
INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('admin', 'admin@example.com', 'hashed_password', '管理员', 'VERIFIED', 'ADMIN', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('elderly1', 'elderly1@example.com', 'hashed_password', '张爷爷', 'VERIFIED', 'ELDERLY', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('doctor1', 'doctor1@hospital.com', 'hashed_password', '王医生', 'VERIFIED', 'DOCTOR', 1);

INSERT OR IGNORE INTO users (username, email, password_hash, name, status, role, is_verified) 
VALUES ('family1', 'family1@example.com', 'hashed_password', '张小明', 'VERIFIED', 'FAMILY', 1);

-- UK Medical Terms Mapping Table (英国医疗术语映射表)
CREATE TABLE IF NOT EXISTS uk_medical_terms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    colloquial_term TEXT NOT NULL, -- 口语化表达
    standard_term TEXT NOT NULL, -- 标准医疗术语
    category TEXT, -- symptom, body_part, medication, location
    urgency_level TEXT, -- low, medium, high, emergency
    region_specific BOOLEAN DEFAULT 0, -- 是否为地区特定表达
    confidence_score REAL DEFAULT 1.0 -- 匹配置信度
);

-- Create index for UK medical terms
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_colloquial ON uk_medical_terms(colloquial_term);
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_category ON uk_medical_terms(category);
CREATE INDEX IF NOT EXISTS idx_uk_medical_terms_urgency ON uk_medical_terms(urgency_level);

-- Schema Version Table (数据库版本表)
CREATE TABLE IF NOT EXISTS schema_version (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    version VARCHAR(20) NOT NULL,
    description TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
); 