-- Simplified Schema for Spring Boot Auto-initialization
-- 简化的数据库初始化脚本

-- Only include essential initial data
-- 只包含必要的初始数据

-- Insert UK medical terms mapping data
-- 插入英国医疗术语映射数据
INSERT OR IGNORE INTO uk_medical_terms (colloquial_term, standard_term, category, urgency_level, region_specific) VALUES
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

-- Insert example admin user
-- 插入示例管理员用户
INSERT OR IGNORE INTO users (email, name, status, role, created_at) VALUES
('admin@elderly-care.uk', 'System Administrator', 'VERIFIED', 'ADMIN', datetime('now'));

-- Insert database version info
-- 插入数据库版本信息
INSERT OR IGNORE INTO schema_version (version, description) VALUES 
('1.0.0', 'Initial database schema with all core tables and CrewAI extensions');
