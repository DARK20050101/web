# 部署指南 (Deployment Guide)

## 部署前准备

### 1. 系统要求
- Java JDK 1.8 或更高版本
- MySQL 8.0 或更高版本
- Tomcat 9.0 或其他支持 Servlet 4.0 的应用服务器
- 至少 512MB 可用内存
- 100MB 磁盘空间（用于图片上传）

### 2. 数据库设置

#### 步骤 1: 创建数据库
```bash
mysql -u root -p
```

在 MySQL 命令行中执行：
```sql
CREATE DATABASE messageboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 步骤 2: 导入初始化脚本
```bash
mysql -u root -p messageboard < sql/schema.sql
```

这将创建以下表：
- `users` - 用户表
- `messages` - 留言表（包含 image_path 字段）
- `sessions` - 会话表（用于"记住我"功能）

#### 步骤 3: 验证数据
默认会创建一个管理员账户：
- 用户名: `admin`
- 密码: `admin123`

### 3. 配置数据库连接

#### 方法 1: 使用环境变量（推荐）

**Linux/macOS:**
```bash
export DB_URL="jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

将这些行添加到 Tomcat 的 `setenv.sh`:
```bash
# 在 $TOMCAT_HOME/bin/setenv.sh 中添加
export DB_URL="jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

**Windows:**
在 `$TOMCAT_HOME/bin/setenv.bat` 中添加：
```batch
set DB_URL=jdbc:mysql://localhost:3306/messageboard?useSSL=false^&serverTimezone=UTC^&characterEncoding=utf8mb4
set DB_USER=root
set DB_PASSWORD=your_password
```

#### 方法 2: 修改源代码
编辑 `src/main/java/com/messageboard/util/DBUtil.java`，修改默认值。

### 4. 配置上传目录

编辑 `src/main/webapp/WEB-INF/web.xml`：

```xml
<context-param>
    <param-name>uploadDir</param-name>
    <param-value>/var/tmp/board_uploads</param-value>
</context-param>
```

**Linux/macOS 推荐路径:**
- `/var/tmp/board_uploads`
- `/opt/messageboard/uploads`

**Windows 推荐路径:**
- `C:\temp\board_uploads`
- `C:\messageboard\uploads`

确保 Tomcat 用户有读写权限：
```bash
sudo mkdir -p /var/tmp/board_uploads
sudo chown tomcat:tomcat /var/tmp/board_uploads
sudo chmod 755 /var/tmp/board_uploads
```

## 构建应用

### 使用 Maven 构建

```bash
# 清理并编译
mvn clean compile

# 打包成 WAR 文件
mvn package

# 跳过测试的快速构建
mvn package -DskipTests
```

构建成功后，WAR 文件位于：`target/messageboard.war`

## 部署到 Tomcat

### 方法 1: 自动部署（热部署）

1. 复制 WAR 文件到 Tomcat webapps 目录：
```bash
cp target/messageboard.war $TOMCAT_HOME/webapps/
```

2. Tomcat 会自动解压并部署应用

3. 访问应用：
```
http://localhost:8080/messageboard/
```

### 方法 2: 使用 Tomcat Manager

1. 访问 Tomcat Manager：
```
http://localhost:8080/manager/html
```

2. 在 "WAR file to deploy" 部分选择 `messageboard.war` 上传

3. 点击 "Deploy" 按钮

### 方法 3: 手动部署

1. 停止 Tomcat：
```bash
$TOMCAT_HOME/bin/shutdown.sh
```

2. 删除旧的部署（如果存在）：
```bash
rm -rf $TOMCAT_HOME/webapps/messageboard
rm -f $TOMCAT_HOME/webapps/messageboard.war
```

3. 复制新的 WAR 文件：
```bash
cp target/messageboard.war $TOMCAT_HOME/webapps/
```

4. 启动 Tomcat：
```bash
$TOMCAT_HOME/bin/startup.sh
```

## 验证部署

### 1. 检查 Tomcat 日志
```bash
tail -f $TOMCAT_HOME/logs/catalina.out
```

查找类似以下的成功消息：
```
INFO: Deployment of web application archive [.../messageboard.war] has finished in [xxx] ms
```

### 2. 访问应用
打开浏览器访问：
```
http://localhost:8080/messageboard/
```

### 3. 测试功能

#### 基本功能测试：
- [ ] 访问首页，查看留言板界面
- [ ] 以匿名用户发表留言（需要昵称和验证码）
- [ ] 使用 admin/admin123 登录
- [ ] 登录后发表留言
- [ ] 上传图片附件
- [ ] 插入表情包
- [ ] 查看留言列表

#### 管理功能测试：
- [ ] 以管理员身份访问 `/admin.jsp`
- [ ] 查看系统统计
- [ ] 删除留言

#### 安全功能测试：
- [ ] 尝试上传非图片文件（应该被拒绝）
- [ ] 尝试上传超过 2MB 的图片（应该被拒绝）
- [ ] 验证 XSS 防护（留言内容中的 HTML 标签应该被转义）

## 生产环境配置建议

### 1. 数据库优化

```sql
-- 创建索引以提高查询性能
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_user_id ON messages(user_id);
CREATE INDEX idx_sessions_expires_at ON sessions(expires_at);

-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

### 2. Tomcat 调优

编辑 `$TOMCAT_HOME/conf/server.xml`：

```xml
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           maxThreads="200"
           minSpareThreads="10"
           maxConnections="10000"
           acceptCount="100"
           URIEncoding="UTF-8"
           compression="on"
           compressionMinSize="2048"
           compressibleMimeType="text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json"/>
```

### 3. JVM 内存设置

在 `$TOMCAT_HOME/bin/setenv.sh` 中添加：

```bash
export CATALINA_OPTS="$CATALINA_OPTS -Xms512m -Xmx1024m"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
export CATALINA_OPTS="$CATALINA_OPTS -Dfile.encoding=UTF-8"
```

### 4. 启用 HTTPS

生产环境中应该使用 HTTPS：

1. 生成 SSL 证书（使用 Let's Encrypt 或购买证书）

2. 配置 Tomcat SSL：

编辑 `$TOMCAT_HOME/conf/server.xml`：

```xml
<Connector port="8443" protocol="org.apache.coyote.http11.Http11NioProtocol"
           maxThreads="150" SSLEnabled="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/keystore.jks"
                     certificateKeystorePassword="your_password"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

3. 更新 `web.xml` 中的 Cookie 配置：

```xml
<session-config>
    <cookie-config>
        <http-only>true</http-only>
        <secure>true</secure>  <!-- 启用 HTTPS -->
    </cookie-config>
</session-config>
```

### 5. 配置反向代理（可选）

使用 Nginx 作为反向代理：

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 图片缓存
    location /messageboard/image/ {
        proxy_pass http://localhost:8080;
        proxy_cache_valid 200 365d;
        add_header Cache-Control "public, immutable";
    }
}
```

### 6. 日志配置

配置 `$TOMCAT_HOME/conf/logging.properties`：

```properties
# 设置日志级别
.handlers = 1catalina.org.apache.juli.FileHandler, java.util.logging.ConsoleHandler
.level = INFO

# 应用日志
com.messageboard.level = INFO
```

### 7. 定期维护任务

创建定时任务清理过期会话：

```sql
-- 清理 30 天前过期的会话
DELETE FROM sessions WHERE expires_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

配置 cron 任务（Linux）：
```bash
# 每天凌晨 2 点清理过期会话
0 2 * * * mysql -u root -p'password' messageboard -e "DELETE FROM sessions WHERE expires_at < NOW();"
```

## 故障排除

### 问题 1: 数据库连接失败

**错误信息：**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**解决方案：**
1. 检查 MySQL 是否运行：`systemctl status mysql`
2. 验证数据库连接信息（host, port, username, password）
3. 检查防火墙规则
4. 确认 MySQL 允许远程连接（如果需要）

### 问题 2: 图片上传失败

**错误信息：**
```
Failed to create upload directory
```

**解决方案：**
1. 检查上传目录是否存在
2. 检查 Tomcat 用户是否有写入权限
3. 确认磁盘空间充足

```bash
sudo mkdir -p /var/tmp/board_uploads
sudo chown tomcat:tomcat /var/tmp/board_uploads
sudo chmod 755 /var/tmp/board_uploads
```

### 问题 3: 应用无法启动

**解决方案：**
1. 查看 Tomcat 日志：`tail -f $TOMCAT_HOME/logs/catalina.out`
2. 检查 web.xml 配置是否正确
3. 验证所有依赖 JAR 包是否存在
4. 确认 Java 版本兼容性

### 问题 4: CSRF Token 错误

**解决方案：**
1. 清除浏览器 Cookie
2. 清除服务器端会话（重启 Tomcat）
3. 确认 CSRF Filter 配置正确

## 监控和日志

### 应用日志位置
- Tomcat 访问日志：`$TOMCAT_HOME/logs/localhost_access_log.txt`
- 应用日志：`$TOMCAT_HOME/logs/catalina.out`
- 错误日志：`$TOMCAT_HOME/logs/catalina.out`

### 监控指标
- 数据库连接池状态
- 应用响应时间
- 内存使用情况
- 磁盘空间（特别是上传目录）

## 升级指南

### 从旧版本升级

1. 备份数据库：
```bash
mysqldump -u root -p messageboard > backup_$(date +%Y%m%d).sql
```

2. 运行迁移脚本：
```bash
mysql -u root -p messageboard < sql/alter_001_add_image_path.sql
```

3. 部署新的 WAR 文件

4. 验证功能正常

## 回滚步骤

如果部署出现问题：

1. 停止 Tomcat
2. 删除新部署的应用
3. 恢复旧版本 WAR 文件
4. 恢复数据库备份（如果需要）
5. 重启 Tomcat

## 联系支持

如有问题，请参考：
- 项目 README: `README.md`
- GitHub Issues: 在项目仓库中创建 issue
- 邮件支持: [联系开发团队]
