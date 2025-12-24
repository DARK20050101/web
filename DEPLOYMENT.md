# 部署指南 (Deployment Guide)

## 前置要求

### 软件要求
- JDK 11 或更高版本
- Maven 3.6+
- MySQL 8.0+
- Apache Tomcat 9.0+ 或其他兼容的 Servlet 容器

### 硬件要求（最低）
- CPU: 1 核
- RAM: 2GB
- 磁盘空间: 1GB（包括上传文件存储）

## 部署步骤

### 1. 数据库设置

#### 1.1 创建数据库

```bash
mysql -u root -p
```

在 MySQL 中执行：

```sql
CREATE DATABASE messageboard DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.2 执行初始化脚本

```bash
mysql -u root -p messageboard < sql/schema.sql
```

#### 1.3 执行迁移脚本

```bash
mysql -u root -p messageboard < sql/alter_001_add_image_path.sql
```

#### 1.4 验证表结构

```sql
USE messageboard;
SHOW TABLES;
DESCRIBE messages;
DESCRIBE users;
```

应该看到 `messages` 表中有 `image_path` 字段。

### 2. 配置应用

#### 2.1 数据库连接配置

编辑 `src/main/java/com/messageboard/util/DBUtil.java`：

```java
private static final String URL = "jdbc:mysql://localhost:3306/messageboard?useSSL=true&serverTimezone=UTC";
private static final String USER = "your_db_user";
private static final String PASSWORD = "your_strong_password";
```

**生产环境建议：** 使用环境变量或外部配置文件管理数据库凭据。

#### 2.2 上传目录配置

编辑 `src/main/webapp/WEB-INF/web.xml`：

```xml
<context-param>
    <param-name>uploadDir</param-name>
    <param-value>/var/www/uploads</param-value>
</context-param>
```

确保该目录存在且应用有写入权限：

```bash
sudo mkdir -p /var/www/uploads
sudo chown tomcat:tomcat /var/www/uploads
sudo chmod 750 /var/www/uploads
```

### 3. 构建项目

```bash
mvn clean package
```

构建成功后，在 `target/` 目录下会生成 `message-board.war` 文件。

### 4. 部署到 Tomcat

#### 4.1 复制 WAR 文件

```bash
sudo cp target/message-board.war $TOMCAT_HOME/webapps/
```

#### 4.2 启动 Tomcat

```bash
$TOMCAT_HOME/bin/catalina.sh start
```

#### 4.3 查看日志

```bash
tail -f $TOMCAT_HOME/logs/catalina.out
```

### 5. 访问应用

打开浏览器访问：

```
http://localhost:8080/message-board/
```

或者使用服务器 IP：

```
http://your-server-ip:8080/message-board/
```

### 6. 登录测试

使用默认管理员账号登录：

- **用户名**: admin
- **密码**: admin123

**重要：** 登录后立即修改默认密码！

## 生产环境配置

### 1. 安全加固

#### 1.1 修改默认密码

登录数据库并执行：

```sql
UPDATE users SET password = 'new_hashed_password' WHERE username = 'admin';
```

**建议：** 使用 BCrypt 或 Argon2 对密码进行哈希。

#### 1.2 启用 HTTPS

在 Tomcat 的 `server.xml` 中配置 SSL：

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

#### 1.3 数据库 SSL

修改连接字符串启用 SSL：

```java
private static final String URL = "jdbc:mysql://localhost:3306/messageboard?useSSL=true&requireSSL=true&serverTimezone=UTC";
```

#### 1.4 Cookie 安全

在 `web.xml` 中设置：

```xml
<session-config>
    <cookie-config>
        <http-only>true</http-only>
        <secure>true</secure>
    </cookie-config>
</session-config>
```

### 2. 性能优化

#### 2.1 数据库索引

确保以下索引存在：

```sql
CREATE INDEX idx_created_at ON messages(created_at DESC);
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_image_path ON messages(image_path);
```

#### 2.2 连接池配置

考虑使用 HikariCP 或 Apache DBCP 连接池。

#### 2.3 静态资源缓存

在 Tomcat 中配置静态资源缓存：

```xml
<Context>
    <Resources cachingAllowed="true" cacheMaxSize="100000" />
</Context>
```

### 3. 监控和日志

#### 3.1 配置日志框架

添加 Log4j2 或 SLF4J 依赖并配置日志输出。

#### 3.2 应用监控

考虑使用：
- JMX 监控
- Prometheus + Grafana
- Application Performance Monitoring (APM) 工具

### 4. 备份策略

#### 4.1 数据库备份

设置定期备份：

```bash
#!/bin/bash
# backup-db.sh
mysqldump -u root -p messageboard > /backup/messageboard_$(date +%Y%m%d).sql
```

添加到 crontab：

```
0 2 * * * /path/to/backup-db.sh
```

#### 4.2 上传文件备份

```bash
#!/bin/bash
# backup-uploads.sh
tar -czf /backup/uploads_$(date +%Y%m%d).tar.gz /var/www/uploads/
```

## 故障排除

### 问题 1: 数据库连接失败

**症状**: 应用启动时报错 "Cannot connect to database"

**解决方案**:
1. 检查数据库是否运行：`systemctl status mysql`
2. 验证连接信息是否正确
3. 检查防火墙设置
4. 确认 MySQL 用户权限

### 问题 2: 图片上传失败

**症状**: 上传图片时返回错误

**解决方案**:
1. 检查上传目录权限：`ls -la /var/www/uploads`
2. 确认目录存在
3. 查看应用日志中的详细错误信息
4. 验证文件大小限制配置

### 问题 3: 404 错误

**症状**: 访问页面返回 404

**解决方案**:
1. 确认 WAR 文件已正确部署
2. 检查 Tomcat 日志
3. 验证 URL 路径是否正确
4. 确认 servlet 映射配置

### 问题 4: CSRF Token 错误

**症状**: 提交表单时报 "Invalid CSRF token"

**解决方案**:
1. 确认 session 正常工作
2. 检查浏览器 Cookie 设置
3. 验证 CSRF Filter 配置
4. 清除浏览器缓存和 Cookie

## 维护建议

### 定期任务
- [ ] 每周检查应用日志
- [ ] 每月更新依赖包
- [ ] 每季度进行安全审计
- [ ] 定期测试备份恢复流程

### 更新流程
1. 在测试环境验证新版本
2. 备份数据库和上传文件
3. 停止 Tomcat 服务
4. 部署新 WAR 文件
5. 执行必要的数据库迁移
6. 启动 Tomcat 并验证功能
7. 监控应用运行状态

## 支持和帮助

如遇到问题，请查阅：
- 项目 README.md
- Tomcat 官方文档
- MySQL 官方文档
- GitHub Issues

祝您部署顺利！
