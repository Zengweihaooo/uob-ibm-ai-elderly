-- Simplified Schema for Spring Boot Auto-initialization

-- Only include essential initial data

-- Insert sample contacts for testing
INSERT OR IGNORE INTO contacts (user_id, name, email, phone, relationship) VALUES
(1, 'Dr. Smith', 'dr.smith@hospital.com', '+44 20 1234 5678', 'doctor'),
(1, 'Sarah Johnson', 'sarah.johnson@gmail.com', '+44 20 2345 6789', 'family'),
(1, 'Mary Wilson', 'mary.wilson@carehome.com', '+44 20 3456 7890', 'caregiver'),
(1, 'John Brown', 'john.brown@yahoo.com', '+44 20 4567 8901', 'friend'),
(1, 'Emergency Services', 'emergency@nhs.uk', '999', 'emergency');

-- Insert sample draft email for testing
INSERT OR IGNORE INTO emails (from_email, to_email, subject, content, status) VALUES
('elderly1@example.com', 'dr.smith@hospital.com', 'Health Check Inquiry', 'Dear Dr. Smith, I would like to schedule a health check appointment. Please let me know your available times.', 'DRAFT');

-- Insert UK medical terms mapping data
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
INSERT OR IGNORE INTO users (email, name, status, role, created_at) VALUES
('admin@elderly-care.uk', 'System Administrator', 'VERIFIED', 'ADMIN', datetime('now'));

-- Insert database version info
INSERT OR IGNORE INTO schema_version (version, description) VALUES 
('1.0.0', 'Initial database schema with all core tables and CrewAI extensions');
