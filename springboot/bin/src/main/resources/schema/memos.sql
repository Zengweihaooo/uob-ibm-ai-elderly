-- 备忘录表
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
    
    -- 索引
    INDEX idx_user_id (user_id),
    INDEX idx_user_deleted (user_id, is_deleted),
    INDEX idx_update_time (update_time),
    INDEX idx_type (type),
    INDEX idx_important (is_important)
);

-- 插入一些测试数据
INSERT OR IGNORE INTO memos (user_id, title, content, type, is_important, pin_code, create_time, update_time) VALUES
(1, '重要提醒', '明天要去医院做体检，记得带身份证和医保卡', 'important', 1, '1234', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '购物清单', '牛奶、面包、鸡蛋、蔬菜', 'todo', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '日常记录', '今天天气很好，适合出去散步', 'general', 0, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '重要事项', '银行密码：123456，请妥善保管', 'important', 1, '5678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 
 
 