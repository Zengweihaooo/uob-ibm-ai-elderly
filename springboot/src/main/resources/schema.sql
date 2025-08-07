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
    record_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_abnormal BOOLEAN DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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