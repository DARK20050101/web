-- Message Board Database Schema
-- This schema supports MySQL/MariaDB
-- Enhanced version with compatibility fixes and data validation

-- Create database
CREATE DATABASE IF NOT EXISTS messageboard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE messageboard;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    nickname VARCHAR(50) NOT NULL DEFAULT '匿名用户',
    content TEXT NOT NULL,
    image_path VARCHAR(255),
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_created_at (created_at DESC),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123) with INSERT IGNORE to avoid duplicates
INSERT IGNORE INTO users (username, password, email, is_admin) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin@messageboard.com', TRUE);

-- Check if welcome messages already exist
SET @has_welcome_messages = 0;
SELECT COUNT(*) INTO @has_welcome_messages FROM messages 
WHERE content LIKE '🎉 欢迎使用在线留言板系统！%' 
   OR content LIKE '📋 使用规范：%'
   OR content LIKE '✨ 功能说明：%'
   OR content LIKE '🔒 安全提示：%'
   OR content LIKE '💡 温馨提示：%';

-- Insert initial welcome messages only if they don't exist
INSERT INTO messages (user_id, nickname, content, is_anonymous, created_at)
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '🎉 欢迎使用在线留言板系统！\n\n本留言板旨在为大家提供一个自由、友好的交流平台。请遵守以下使用规范，共同营造良好的交流环境。',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 5 MINUTE)
WHERE @has_welcome_messages = 0
UNION ALL
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '📋 使用规范：\n\n1️⃣ 文明发言：请使用文明礼貌的语言，禁止发布侮辱、诽谤、歧视等不当内容\n2️⃣ 尊重他人：尊重他人的观点和隐私，不得恶意攻击或骚扰其他用户\n3️⃣ 内容合法：禁止发布违法违规信息，包括但不限于色情、暴力、赌博等内容',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 4 MINUTE)
WHERE @has_welcome_messages = 0
UNION ALL
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '✨ 功能说明：\n\n• 匿名留言：未登录用户可以匿名发表留言，需要输入昵称和验证码\n• 注册用户：注册登录后可以直接发表留言，无需验证码\n• 编辑删除：注册用户可以编辑和删除自己发表的留言\n• 图片上传：支持上传图片和表情包，让交流更生动有趣',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 3 MINUTE)
WHERE @has_welcome_messages = 0
UNION ALL
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '🔒 安全提示：\n\n• 请妥善保管您的账号密码，不要将密码告诉他人\n• 系统已启用多重安全防护，包括SQL注入防护、XSS攻击防护等\n• 如发现任何安全问题，请及时联系管理员\n• 定期修改密码可以提高账号安全性',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 2 MINUTE)
WHERE @has_welcome_messages = 0
UNION ALL
SELECT 
    (SELECT id FROM users WHERE username = 'admin'),
    'admin',
    '💡 温馨提示：\n\n• 留言按照时间倒序显示，最新的留言会出现在最前面\n• 支持分页浏览，每页显示10条留言\n• 管理员会定期清理违规内容，请自觉遵守规范\n• 如有任何建议或问题，欢迎在留言板中反馈\n\n祝大家使用愉快！😊',
    FALSE,
    DATE_SUB(NOW(), INTERVAL 1 MINUTE)
WHERE @has_welcome_messages = 0;

-- Display database status
SELECT '=== 数据库初始化完成 ===' AS Status;
SELECT CONCAT('✅ 用户数: ', COUNT(*)) AS UserCount FROM users;
SELECT CONCAT('✅ 留言数: ', COUNT(*)) AS MessageCount FROM messages;
SELECT '=== 管理员账号 ===' AS AdminInfo;
SELECT username AS '用户名', email AS '邮箱', is_admin AS '管理员权限', created_at AS '创建时间' FROM users WHERE is_admin = TRUE;
