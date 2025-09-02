-- Memo table
CREATE TABLE IF NOT EXISTS memos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'general',
    is_important BOOLEAN NOT NULL DEFAULT 0,
    pin_code VARCHAR(10),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT 0,
    
    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_user_deleted (user_id, is_deleted),
    INDEX idx_update_time (update_time),
    INDEX idx_type (type),
    INDEX idx_important (is_important)
);

-- Insert some test data
INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES
(1, 'Important Reminder', 'Tomorrow go to hospital for physical examination, remember to bring ID card and medical insurance card', 'important', 1, '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Shopping List', 'Milk, bread, eggs, vegetables', 'todo', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Daily Record', 'Today weather is good, suitable for walking', 'general', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Important Matter', 'Bank password: 123456, please keep safe', 'important', 1, '5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 
 
 