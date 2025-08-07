-- ==================================================================================
-- SQLite Database Design for IBM AI Elderly Care System
-- 面向英国老年人的AI陪伴系统数据库设计
-- ==================================================================================

-- ==================== 核心用户管理表 / Core User Management ====================

-- 用户表 / Users Table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    name TEXT,
    phone_number TEXT,
    verification_code TEXT,
    status TEXT DEFAULT 'UNREGISTERED', -- UNREGISTERED, PENDING, VERIFIED
    role TEXT DEFAULT 'ELDERLY', -- ELDERLY, FAMILY, DOCTOR, ADMIN
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    verified_at DATETIME,
    code_expires_at DATETIME
);

-- 英国用户配置表 / UK User Profiles (为CrewAI扩展预留)
CREATE TABLE uk_user_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    -- UK Healthcare Info / 英国医疗信息
    nhs_number TEXT, -- 加密存储的NHS号码
    gp_surgery_name TEXT, -- GP诊所名称
    preferred_gp_name TEXT, -- 偏好的医生姓名
    
    -- Regional Info / 地区信息  
    region TEXT, -- Scotland, Northern_England, London, Wales, etc.
    dialect_preference TEXT, -- 方言偏好识别
    
    -- Communication Preferences / 沟通偏好
    formality_level TEXT DEFAULT 'polite', -- polite, casual, formal
    preferred_greeting TEXT DEFAULT 'standard', -- 问候语偏好
    
    -- Emergency Preferences / 紧急情况偏好
    emergency_service_preference TEXT DEFAULT '999', -- 999, 111, GP
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 日程管理表 / Schedule Management ====================

-- 日程表 / Schedules Table
CREATE TABLE schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    schedule_date DATE NOT NULL,
    activity_time TIME,
    title TEXT NOT NULL,
    description TEXT,
    category TEXT, -- morning, afternoon, evening, medication
    priority TEXT DEFAULT 'medium', -- high, medium, low
    completed BOOLEAN DEFAULT 0,
    
    -- Enhanced Features / 高级功能
    emergency_contact TEXT,
    emergency_contact_name TEXT,
    repeat_cycle TEXT DEFAULT 'none', -- none, daily, weekly, monthly, yearly
    notification_time TEXT DEFAULT '15min', -- 5min, 15min, 30min, 1hour, 1day
    
    -- Location-based Reminders / 基于位置的提醒
    location_reminder BOOLEAN DEFAULT 0,
    location_name TEXT,
    latitude REAL,
    longitude REAL,
    location_radius INTEGER, -- 半径（米）
    
    -- System Fields / 系统字段
    notes TEXT,
    is_all_day BOOLEAN DEFAULT 0,
    reminder_sent DATETIME,
    emergency_notification_sent DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 健康记录表 / Health Records ====================

-- 健康记录表 / Health Records Table
CREATE TABLE health_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type TEXT NOT NULL, -- bloodPressure, bloodSugar, steps, weight, heartRate, symptoms
    value TEXT NOT NULL,
    record_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    
    -- Sharing Features / 共享功能
    shared BOOLEAN DEFAULT 0,
    shared_with_user_id INTEGER,
    shared_with_role TEXT, -- family, doctor
    shared_at DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (shared_with_user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- ==================== 家庭联系人表 / Family Contacts ====================

-- 家庭联系人表 / Family Contacts Table
CREATE TABLE family_contacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    phone_number TEXT,
    email TEXT,
    relationship TEXT, -- SPOUSE, CHILD, GRANDCHILD, SIBLING, PARENT, FRIEND, NEIGHBOR, CAREGIVER, DOCTOR, OTHER
    notification_preference TEXT DEFAULT 'BOTH', -- SMS, EMAIL, BOTH, NONE
    is_emergency_contact BOOLEAN DEFAULT 0,
    is_active BOOLEAN DEFAULT 1,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_contacted_at DATETIME,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 情感陪伴AI表 / Emotion Companion ====================

-- 情感陪伴表 / Emotion Companions Table
CREATE TABLE emotion_companions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE NOT NULL,
    name TEXT DEFAULT 'Whiskers',
    personality TEXT DEFAULT 'friendly', -- friendly, professional, casual, caring
    avatar TEXT DEFAULT 'robot', -- robot, assistant, companion, helper
    
    -- Emotional and Behavioral States / 情感和行为状态
    emotion TEXT DEFAULT 'happy', -- happy, sad, excited, calm, anxious, helpful
    happiness INTEGER DEFAULT 50, -- 0-100
    energy INTEGER DEFAULT 50, -- 0-100
    responsiveness INTEGER DEFAULT 80, -- 0-100
    
    -- Activity and Interaction Tracking / 活动和交互追踪
    last_interaction DATETIME,
    last_chat DATETIME,
    last_command DATETIME,
    interaction_count INTEGER DEFAULT 0,
    chat_count INTEGER DEFAULT 0,
    
    -- Location and Presence / 位置和状态
    current_location TEXT DEFAULT 'home_screen', -- home_screen, chat_mode, assistant_mode, sleep_mode
    is_active BOOLEAN DEFAULT 1,
    activity_mode TEXT DEFAULT 'idle', -- listening, thinking, responding, idle, sleeping
    
    -- Sound and Visual Expressions / 声音和视觉表达
    current_sound TEXT DEFAULT 'silent', -- beep, chime, voice, notification, silent
    visual_expression TEXT DEFAULT 'idle_screen', -- happy_led, sad_led, thinking_animation, idle_screen
    is_making_sound BOOLEAN DEFAULT 0,
    is_expressing_emotion BOOLEAN DEFAULT 0,
    led_color TEXT DEFAULT 'blue', -- green, blue, red, yellow, purple, white
    
    -- Neglect Tracking / 忽视追踪
    last_attention_time DATETIME,
    neglect_level INTEGER DEFAULT 0, -- 0-100
    needs_attention BOOLEAN DEFAULT 0,
    is_lonely BOOLEAN DEFAULT 0,
    
    -- AI-specific Features / AI特定功能
    current_task TEXT,
    is_learning BOOLEAN DEFAULT 1,
    helpfulness INTEGER DEFAULT 50, -- 0-100
    
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 重要日期表 / Important Dates ====================

-- 重要日期表 / Important Dates Table
CREATE TABLE important_dates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    type TEXT, -- birthday, anniversary, holiday, custom
    repeat_cycle TEXT DEFAULT 'yearly', -- none, yearly
    enabled BOOLEAN DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 聊天记录表 / Chat Messages ====================

-- 聊天消息表 / Chat Messages Table (从ChatController推断)
CREATE TABLE chat_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    message_type TEXT NOT NULL, -- user, assistant, system
    content TEXT,
    attachments TEXT, -- JSON格式存储附件信息
    conversation_id TEXT, -- 对话会话ID
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- ==================== 播客缓存表 / Podcast Cache ====================

-- 播客表 / Podcasts Table
CREATE TABLE podcasts (
    id TEXT PRIMARY KEY, -- Listen Notes API的ID
    title TEXT NOT NULL,
    description TEXT,
    publisher TEXT,
    image TEXT,
    thumbnail TEXT,
    listennotes_url TEXT,
    rss TEXT,
    language TEXT,
    country TEXT,
    website TEXT,
    is_claimed BOOLEAN DEFAULT 0,
    type TEXT,
    total_episodes INTEGER DEFAULT 0,
    genres TEXT, -- JSON格式存储分类标签
    extra TEXT, -- JSON格式存储额外信息
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 播客剧集表 / Podcast Episodes Table
CREATE TABLE podcast_episodes (
    id TEXT PRIMARY KEY,
    podcast_id TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    audio TEXT,
    image TEXT,
    thumbnail TEXT,
    listennotes_url TEXT,
    audio_length TEXT,
    published_date DATETIME,
    language TEXT,
    country TEXT,
    website TEXT,
    is_claimed BOOLEAN DEFAULT 0,
    type TEXT,
    extra TEXT, -- JSON格式
    
    FOREIGN KEY (podcast_id) REFERENCES podcasts (id) ON DELETE CASCADE
);

-- ==================== CrewAI扩展表 / CrewAI Extensions (未来使用) ====================

-- 对话上下文表 / Conversation Contexts (为CrewAI准备)
CREATE TABLE conversation_contexts (
    id TEXT PRIMARY KEY,
    user_id INTEGER NOT NULL,
    status TEXT DEFAULT 'active', -- active, waiting_info, completed, failed
    operation_type TEXT, -- schedule_creation, health_record, family_contact, emergency
    missing_fields TEXT, -- JSON格式存储缺失字段
    extracted_info TEXT, -- JSON格式存储已提取信息
    conversation_history TEXT, -- JSON格式存储对话历史
    language_preference TEXT DEFAULT 'british_english', -- 语言偏好
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME, -- 对话过期时间
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- LLM请求日志表 / LLM Request Logs (为CrewAI准备)
CREATE TABLE llm_request_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    conversation_id TEXT,
    request_type TEXT, -- analyze_instruction, supplement_info, execute_task
    input_text TEXT,
    response_data TEXT, -- JSON格式存储响应
    processing_time INTEGER, -- 处理时间(毫秒)
    success BOOLEAN,
    error_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 英国医疗术语映射表 / UK Medical Terms Mapping (为CrewAI准备)
CREATE TABLE uk_medical_terms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    colloquial_term TEXT NOT NULL, -- 口语化表达
    standard_term TEXT NOT NULL, -- 标准医疗术语
    category TEXT, -- symptom, body_part, medication, location
    urgency_level TEXT, -- low, medium, high, emergency
    region_specific BOOLEAN DEFAULT 0, -- 是否为地区特定表达
    confidence_score REAL DEFAULT 1.0 -- 匹配置信度
);

-- ==================== 性能优化索引 / Performance Indexes ====================

-- 用户相关索引
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_role ON users(role);

-- 日程相关索引
CREATE INDEX idx_schedules_user_date ON schedules(user_id, schedule_date);
CREATE INDEX idx_schedules_reminder ON schedules(reminder_sent, notification_time);
CREATE INDEX idx_schedules_location ON schedules(location_reminder, latitude, longitude);
CREATE INDEX idx_schedules_category ON schedules(category);
CREATE INDEX idx_schedules_priority ON schedules(priority);

-- 健康记录索引
CREATE INDEX idx_health_records_user_time ON health_records(user_id, record_time);
CREATE INDEX idx_health_records_type ON health_records(type);
CREATE INDEX idx_health_records_shared ON health_records(shared, shared_with_user_id);

-- 聊天记录索引
CREATE INDEX idx_chat_messages_user_time ON chat_messages(user_id, timestamp);
CREATE INDEX idx_chat_messages_conversation ON chat_messages(conversation_id);
CREATE INDEX idx_chat_messages_type ON chat_messages(message_type);

-- 家庭联系人索引
CREATE INDEX idx_family_contacts_user ON family_contacts(user_id);
CREATE INDEX idx_family_contacts_emergency ON family_contacts(is_emergency_contact);
CREATE INDEX idx_family_contacts_active ON family_contacts(is_active);

-- 重要日期索引
CREATE INDEX idx_important_dates_user_date ON important_dates(user_id, date);
CREATE INDEX idx_important_dates_type ON important_dates(type);

-- 情感陪伴索引
CREATE INDEX idx_emotion_companions_user ON emotion_companions(user_id);
CREATE INDEX idx_emotion_companions_active ON emotion_companions(is_active);

-- 播客索引
CREATE INDEX idx_podcasts_language ON podcasts(language);
CREATE INDEX idx_podcast_episodes_podcast ON podcast_episodes(podcast_id);

-- CrewAI相关索引
CREATE INDEX idx_conversation_contexts_user ON conversation_contexts(user_id);
CREATE INDEX idx_conversation_contexts_status ON conversation_contexts(status);
CREATE INDEX idx_llm_logs_user_time ON llm_request_logs(user_id, created_at);
CREATE INDEX idx_uk_medical_terms_colloquial ON uk_medical_terms(colloquial_term);

-- ==================== 初始数据插入 / Initial Data ====================

-- 插入英国医疗术语映射数据
INSERT INTO uk_medical_terms (colloquial_term, standard_term, category, urgency_level, region_specific) VALUES
('poorly', 'unwell', 'symptom', 'medium', 1),
('under the weather', 'feeling unwell', 'symptom', 'low', 0),
('playing up', 'acting abnormally', 'symptom', 'medium', 1),
('rough', 'unwell', 'symptom', 'medium', 1),
('GP', 'General Practitioner', 'healthcare_provider', 'low', 0),
('surgery', 'medical practice', 'location', 'low', 0),
('A&E', 'Accident and Emergency', 'location', 'high', 0),
('poorly chest', 'chest discomfort', 'symptom', 'high', 1),
('dodgy heart', 'heart problems', 'symptom', 'high', 1),
('feeling dickey', 'feeling unwell', 'symptom', 'medium', 1);

-- 插入示例管理员用户
INSERT INTO users (email, name, status, role, created_at) VALUES
('admin@elderly-care.uk', 'System Administrator', 'VERIFIED', 'ADMIN', CURRENT_TIMESTAMP);

-- ==================== 视图定义 / View Definitions ====================

-- 用户健康概览视图
CREATE VIEW user_health_overview AS
SELECT 
    u.id as user_id,
    u.name,
    u.email,
    COUNT(hr.id) as total_health_records,
    MAX(hr.record_time) as last_health_record,
    COUNT(CASE WHEN hr.record_time >= date('now', '-7 days') THEN 1 END) as records_this_week
FROM users u
LEFT JOIN health_records hr ON u.id = hr.user_id
WHERE u.role = 'ELDERLY'
GROUP BY u.id, u.name, u.email;

-- 今日日程视图
CREATE VIEW today_schedule AS
SELECT 
    s.*,
    u.name as user_name,
    u.email as user_email
FROM schedules s
JOIN users u ON s.user_id = u.id
WHERE s.schedule_date = date('now')
ORDER BY s.activity_time;

-- 紧急联系人视图
CREATE VIEW emergency_contacts AS
SELECT 
    fc.*,
    u.name as elderly_user_name,
    u.email as elderly_user_email
FROM family_contacts fc
JOIN users u ON fc.user_id = u.id
WHERE fc.is_emergency_contact = 1 AND fc.is_active = 1;

-- ==================== 触发器定义 / Trigger Definitions ====================

-- 自动更新时间戳触发器
CREATE TRIGGER update_schedules_timestamp 
    AFTER UPDATE ON schedules
    FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at OR NEW.updated_at IS NULL
BEGIN
    UPDATE schedules SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER update_health_records_timestamp 
    AFTER UPDATE ON health_records
    FOR EACH ROW
    WHEN OLD.record_time = NEW.record_time OR NEW.record_time IS NULL
BEGIN
    UPDATE health_records SET record_time = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

CREATE TRIGGER update_emotion_companions_timestamp 
    AFTER UPDATE ON emotion_companions
    FOR EACH ROW
    WHEN OLD.updated_at = NEW.updated_at OR NEW.updated_at IS NULL
BEGIN
    UPDATE emotion_companions SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id;
END;

-- ==================== 数据完整性约束 / Data Integrity Constraints ====================

-- 确保健康记录值的有效性 (简单验证)
CREATE TRIGGER validate_health_record_value
    BEFORE INSERT ON health_records
    FOR EACH ROW
    WHEN NEW.type = 'bloodPressure' AND (
        LENGTH(NEW.value) < 5 OR 
        NEW.value NOT LIKE '%/%'
    )
BEGIN
    SELECT RAISE(ABORT, 'Invalid blood pressure format. Expected format: "120/80"');
END;

-- 确保情感陪伴状态值在有效范围内
CREATE TRIGGER validate_emotion_companion_values
    BEFORE INSERT ON emotion_companions
    FOR EACH ROW
    WHEN NEW.happiness < 0 OR NEW.happiness > 100 OR
         NEW.energy < 0 OR NEW.energy > 100 OR
         NEW.responsiveness < 0 OR NEW.responsiveness > 100
BEGIN
    SELECT RAISE(ABORT, 'Emotion companion values must be between 0 and 100');
END;

-- ==================== 数据库版本信息 / Database Version ====================

CREATE TABLE schema_version (
    version TEXT PRIMARY KEY,
    description TEXT,
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO schema_version (version, description) VALUES 
('1.0.0', 'Initial database schema with all core tables and CrewAI extensions');

-- ==================== 说明文档 / Documentation ====================

/*
数据库设计说明 / Database Design Notes:

1. 核心设计原则 / Core Design Principles:
   - 保持与现有POJO类的完全兼容性
   - 为CrewAI集成预留扩展空间  
   - 支持英国特定的医疗和文化背景
   - 确保数据完整性和性能优化

2. 主要特性 / Key Features:
   - 完整的用户管理和角色系统
   - 高级日程管理(地理位置、重复、优先级)
   - 健康记录共享和追踪
   - 情感陪伴AI状态管理
   - 英国医疗术语支持
   - CrewAI对话上下文管理

3. 扩展点 / Extension Points:
   - uk_user_profiles: 英国用户特定配置
   - conversation_contexts: CrewAI对话管理
   - uk_medical_terms: 英国医疗术语映射
   - llm_request_logs: LLM请求追踪

4. 性能优化 / Performance Optimizations:
   - 针对查询模式优化的索引
   - 视图简化常用查询
   - 触发器保证数据一致性
   - 分区友好的时间戳字段

5. 安全考虑 / Security Considerations:
   - 敏感数据(如NHS号码)加密存储
   - 外键约束保证引用完整性
   - 角色基础的访问控制支持
   - 数据共享权限管理
*/
