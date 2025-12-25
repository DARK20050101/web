# 数据库设置详细指南

> **⚠️ 留言列表无法显示？** 请查看 [TROUBLESHOOTING_MESSAGES.md](TROUBLESHOOTING_MESSAGES.md) 获取完整的问题排查指南！

## 📊 数据库概述

本项目使用MySQL数据库，共需要**2个表**：
1. **users** - 用户表
2. **messages** - 留言表

## 🗂️ 表结构详解

### 1. users（用户表）

存储所有注册用户的信息。

| 字段名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| `id` | INT | 用户唯一标识 | 主键、自增 |
| `username` | VARCHAR(50) | 用户名 | 唯一、非空 |
| `password` | VARCHAR(255) | 密码（SHA-256加密） | 非空 |
| `email` | VARCHAR(100) | 邮箱地址 | 可选 |
| `is_admin` | BOOLEAN | 是否为管理员 | 默认FALSE |
| `created_at` | TIMESTAMP | 注册时间 | 默认当前时间 |

**索引：**
- `idx_username` - 在username字段上创建索引，加快查询速度

**初始数据：**
- 默认管理员账号：`admin`
- 密码：`admin123`（加密后存储）
- 邮箱：`admin@messageboard.com`

**SQL定义：**
```sql
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. messages（留言表）

存储所有用户的留言内容。

| 字段名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| `id` | INT | 留言唯一标识 | 主键、自增 |
| `user_id` | INT | 发布者用户ID | 外键、可为NULL（匿名） |
| `nickname` | VARCHAR(50) | 昵称 | 非空 |
| `content` | TEXT | 留言内容 | 非空 |
| `image_path` | VARCHAR(255) | 上传图片路径 | 可选 |
| `is_anonymous` | BOOLEAN | 是否匿名留言 | 默认FALSE |
| `created_at` | TIMESTAMP | 发布时间 | 默认当前时间 |
| `updated_at` | TIMESTAMP | 最后更新时间 | 自动更新 |

**外键关系：**
- `user_id` 关联到 `users.id`
- 删除用户时，留言的 `user_id` 设置为 NULL（保留留言）

**索引：**
- `idx_created_at` - 在created_at字段上创建降序索引（最新留言优先）
- `idx_user_id` - 在user_id字段上创建索引，加快用户留言查询

**初始数据：**
- 5条欢迎留言，介绍系统使用规范

**SQL定义：**
```sql
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    nickname VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    image_path VARCHAR(255),
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_created_at (created_at DESC),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 🔧 Windows系统数据库配置步骤

### 方式一：使用命令行（推荐用于脚本自动化）

```cmd
# 1. 打开命令提示符（以管理员身份运行）
# 2. 切换到项目目录
cd C:\path\to\your\project

# 3. 登录MySQL
mysql -u root -p
# 输入密码：!aBc123456

# 4. 在MySQL中执行
source database_schema.sql;
# 或者退出MySQL后执行：
exit;
mysql -u root -p < database_schema.sql
```

### 方式二：使用MySQL Workbench（推荐初学者）

1. **打开MySQL Workbench**
   - 在开始菜单找到并打开

2. **连接到MySQL服务器**
   - 双击 "Local instance MySQL" 连接
   - 输入密码：`!aBc123456`

3. **导入SQL脚本**
   - 点击菜单：`File` → `Open SQL Script`
   - 找到项目中的 `database_schema.sql` 文件
   - 点击打开

4. **执行SQL脚本**
   - 点击工具栏的闪电图标⚡（Execute）
   - 或按快捷键 `Ctrl + Shift + Enter`

5. **验证创建成功**
   - 左侧 SCHEMAS 面板点击刷新按钮🔄
   - 应该能看到 `messageboard` 数据库
   - 展开可以看到 `users` 和 `messages` 两个表

### 方式三：使用phpMyAdmin（如果安装了XAMPP）

1. 打开浏览器访问：`http://localhost/phpmyadmin`
2. 点击"导入"标签
3. 点击"选择文件"，选择 `database_schema.sql`
4. 点击"执行"按钮

## ✅ 验证数据库配置

### 1. 检查数据库是否创建

```sql
-- 查看所有数据库
SHOW DATABASES;

-- 应该能看到 messageboard
```

### 2. 检查表是否创建

```sql
-- 切换到数据库
USE messageboard;

-- 查看所有表
SHOW TABLES;

-- 应该显示：
-- +------------------------+
-- | Tables_in_messageboard |
-- +------------------------+
-- | messages               |
-- | users                  |
-- +------------------------+
```

### 3. 查看表结构

```sql
-- 查看users表结构
DESCRIBE users;

-- 输出：
-- +------------+--------------+------+-----+-------------------+
-- | Field      | Type         | Null | Key | Default           |
-- +------------+--------------+------+-----+-------------------+
-- | id         | int          | NO   | PRI | NULL              |
-- | username   | varchar(50)  | NO   | UNI | NULL              |
-- | password   | varchar(255) | NO   |     | NULL              |
-- | email      | varchar(100) | YES  |     | NULL              |
-- | is_admin   | tinyint(1)   | YES  |     | 0                 |
-- | created_at | timestamp    | YES  |     | CURRENT_TIMESTAMP |
-- +------------+--------------+------+-----+-------------------+

-- 查看messages表结构
DESCRIBE messages;

-- 输出：
-- +--------------+--------------+------+-----+-------------------+
-- | Field        | Type         | Null | Key | Default           |
-- +--------------+--------------+------+-----+-------------------+
-- | id           | int          | NO   | PRI | NULL              |
-- | user_id      | int          | YES  | MUL | NULL              |
-- | nickname     | varchar(50)  | NO   |     | NULL              |
-- | content      | text         | NO   |     | NULL              |
-- | image_path   | varchar(255) | YES  |     | NULL              |
-- | is_anonymous | tinyint(1)   | YES  |     | 0                 |
-- | created_at   | timestamp    | YES  | MUL | CURRENT_TIMESTAMP |
-- | updated_at   | timestamp    | YES  |     | CURRENT_TIMESTAMP |
-- +--------------+--------------+------+-----+-------------------+
```

### 4. 查看初始数据

```sql
-- 查看用户数据
SELECT * FROM users;

-- 应该有1条管理员记录：
-- +----+----------+------------------------------------------------------------------+--------------------------+----------+---------------------+
-- | id | username | password                                                         | email                    | is_admin | created_at          |
-- +----+----------+------------------------------------------------------------------+--------------------------+----------+---------------------+
-- |  1 | admin    | 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9 | admin@messageboard.com   |        1 | 2025-12-25 18:00:00 |
-- +----+----------+------------------------------------------------------------------+--------------------------+----------+---------------------+

-- 查看留言数据
SELECT id, nickname, LEFT(content, 30) as content_preview, is_anonymous FROM messages;

-- 应该有5条欢迎留言：
-- +----+----------+--------------------------------+--------------+
-- | id | nickname | content_preview                | is_anonymous |
-- +----+----------+--------------------------------+--------------+
-- |  1 | admin    | 🎉 欢迎使用在线留言板系统！      |            0 |
-- |  2 | admin    | 📋 使用规范：                   |            0 |
-- |  3 | admin    | ✨ 功能说明：                   |            0 |
-- |  4 | admin    | 🔒 安全提示：                   |            0 |
-- |  5 | admin    | 💡 温馨提示：                   |            0 |
-- +----+----------+--------------------------------+--------------+
```

## 🚨 常见错误及解决方法

### 错误1: "Access denied for user 'root'@'localhost'"

**原因：** 密码不正确

**解决：**
```sql
-- 使用正确的密码：!aBc123456
mysql -u root -p!aBc123456

-- 如果密码确实不对，需要重置MySQL密码
```

### 错误2: "Unknown database 'messageboard'"

**原因：** 数据库未创建

**解决：**
```sql
-- 手动创建数据库
CREATE DATABASE messageboard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 然后重新导入脚本
USE messageboard;
source database_schema.sql;
```

### 错误3: "Table 'messageboard.messages' doesn't exist"

**原因：** 表未创建

**解决：**
```sql
-- 重新执行database_schema.sql脚本
USE messageboard;
source C:\path\to\project\database_schema.sql;
```

### 错误4: "Can't connect to MySQL server on 'localhost'"

**原因：** MySQL服务未启动

**解决：**
```cmd
# Windows - 启动MySQL服务
net start mysql

# 或在服务管理器中启动
# 1. Win+R 输入 services.msc
# 2. 找到 MySQL 服务
# 3. 右键选择"启动"
```

### 错误5: 中文显示乱码

**原因：** 字符集不正确

**解决：**
```sql
-- 检查数据库字符集
SHOW CREATE DATABASE messageboard;

-- 如果不是utf8mb4，需要修改
ALTER DATABASE messageboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 检查表字符集
SHOW CREATE TABLE messages;

-- 修改表字符集
ALTER TABLE messages CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 🔗 配置应用连接数据库

编辑 `src/main/resources/db.properties`：

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=!aBc123456
```

**重要提示：**
- 确保密码与MySQL的root密码一致
- 如果MySQL端口不是3306，需要修改URL中的端口号
- 对于MySQL 8.0+，需要添加 `allowPublicKeyRetrieval=true` 参数

## 📝 数据库备份

### 备份整个数据库

```cmd
mysqldump -u root -p messageboard > backup_20251225.sql
```

### 只备份表结构

```cmd
mysqldump -u root -p --no-data messageboard > structure_only.sql
```

### 恢复数据库

```cmd
mysql -u root -p messageboard < backup_20251225.sql
```

## 🎯 总结

完成数据库配置后，系统应该具有：

✅ 1个数据库：`messageboard`  
✅ 2个表：`users`, `messages`  
✅ 1个管理员账号：`admin/admin123`  
✅ 5条欢迎留言  

如果一切正常，启动应用后应该能够：
1. 看到5条欢迎留言显示在留言板上
2. 使用 `admin/admin123` 登录管理后台
3. 正常发表和管理留言

如仍有问题，请检查：
- MySQL服务是否正常运行
- 数据库配置文件密码是否正确
- 应用日志中的错误信息
