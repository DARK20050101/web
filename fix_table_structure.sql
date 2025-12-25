-- 表结构修复脚本
-- 此脚本用于修复已存在的 messages 表，确保其结构与代码匹配

USE messageboard;

-- 显示当前表结构
SELECT '=== 当前 messages 表结构 ===' AS Info;
DESCRIBE messages;

-- 检查 nickname 列是否存在
SET @nickname_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'messages'
      AND column_name = 'nickname'
);

-- 如果 nickname 列不存在，添加它
SET @sql_add_nickname := IF(
    @nickname_exists = 0,
    'ALTER TABLE messages ADD COLUMN nickname VARCHAR(50) NOT NULL DEFAULT ''匿名用户'' AFTER user_id',
    'SELECT ''nickname 列已存在，跳过'' AS Info'
);

PREPARE stmt FROM @sql_add_nickname;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 is_anonymous 列是否存在
SET @anonymous_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'messages'
      AND column_name = 'is_anonymous'
);

-- 如果 is_anonymous 列不存在，添加它
SET @sql_add_anonymous := IF(
    @anonymous_exists = 0,
    'ALTER TABLE messages ADD COLUMN is_anonymous BOOLEAN DEFAULT FALSE AFTER image_path',
    'SELECT ''is_anonymous 列已存在，跳过'' AS Info'
);

PREPARE stmt FROM @sql_add_anonymous;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查是否存在旧的 author 列
SET @author_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'messages'
      AND column_name = 'author'
);

-- 如果存在 author 列，将数据迁移到 nickname 列，然后删除 author 列
SET SQL_SAFE_UPDATES = 0;

SET @sql_migrate_author := IF(
    @author_exists = 1,
    'UPDATE messages SET nickname = COALESCE(author, nickname, ''匿名用户'') WHERE nickname IS NULL OR nickname = ''''',
    'SELECT ''不存在 author 列，跳过迁移'' AS Info'
);

PREPARE stmt FROM @sql_migrate_author;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 删除 author 列（如果存在）
SET @sql_drop_author := IF(
    @author_exists = 1,
    'ALTER TABLE messages DROP COLUMN author',
    'SELECT ''不存在 author 列，跳过删除'' AS Info'
);

PREPARE stmt FROM @sql_drop_author;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET SQL_SAFE_UPDATES = 1;

-- 确保 nickname 列不为空
UPDATE messages 
SET nickname = '匿名用户' 
WHERE nickname IS NULL OR nickname = '';

-- 显示修复后的表结构
SELECT '=== 修复后的 messages 表结构 ===' AS Info;
DESCRIBE messages;

-- 显示数据样例
SELECT '=== 前5条留言数据检查 ===' AS Info;
SELECT id, user_id, nickname, LEFT(content, 30) AS content_preview, is_anonymous, created_at
FROM messages
ORDER BY created_at DESC
LIMIT 5;

SELECT '=== 修复完成 ===' AS Status;
SELECT CONCAT('✅ 留言总数: ', COUNT(*)) AS Result FROM messages;
