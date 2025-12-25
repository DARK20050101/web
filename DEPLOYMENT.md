# 部署指南

## 开发环境部署

### 1. 准备工作

#### 安装JDK
```bash
# 检查Java版本
java -version

# 如未安装，下载并安装JDK 1.8+
# Windows: 从Oracle官网下载安装包
# Linux: sudo apt-get install openjdk-8-jdk
# macOS: brew install openjdk@8
```

#### 安装MySQL
```bash
# Windows: 从MySQL官网下载安装包
# Linux: sudo apt-get install mysql-server
# macOS: brew install mysql

# 启动MySQL服务
# Linux: sudo service mysql start
# macOS: brew services start mysql
```

#### 安装Maven
```bash
# 检查Maven版本
mvn -version

# 如未安装
# Windows: 从Maven官网下载并配置环境变量
# Linux: sudo apt-get install maven
# macOS: brew install maven
```

#### 安装Tomcat
```bash
# 从Apache Tomcat官网下载
# 解压到合适的目录
# 配置环境变量 CATALINA_HOME
```

### 2. 数据库初始化

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库并导入表结构
source /path/to/database_schema.sql

# 或者直接执行
mysql -u root -p < database_schema.sql

# 验证表创建成功
mysql -u root -p
USE messageboard;
SHOW TABLES;
```

### 3. 配置数据库连接

编辑 `src/main/resources/db.properties`：

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=your_mysql_password
```

### 4. 编译项目

```bash
# 进入项目根目录
cd /path/to/web

# 清理并编译
mvn clean package

# 编译成功后，在 target 目录下会生成 messageboard.war
```

### 5. 部署到Tomcat

#### 方式一：手动部署
```bash
# 复制WAR文件到Tomcat的webapps目录
cp target/messageboard.war $CATALINA_HOME/webapps/

# 启动Tomcat
cd $CATALINA_HOME/bin
./startup.sh  # Linux/macOS
# 或
startup.bat   # Windows
```

#### 方式二：使用Maven插件
```bash
# 直接运行
mvn tomcat7:run

# 应用将运行在 http://localhost:8080/messageboard
```

### 6. 访问应用

打开浏览器访问：
- 主页：http://localhost:8080/messageboard/
- 登录页：http://localhost:8080/messageboard/login.jsp
- 管理后台：http://localhost:8080/messageboard/admin/dashboard.jsp

默认管理员账号：
- 用户名：admin
- 密码：admin123

## 生产环境部署

### 1. 服务器要求

- 操作系统：Linux (推荐 Ubuntu 20.04 LTS 或 CentOS 7+)
- 内存：2GB+
- 硬盘：10GB+
- CPU：2核+

### 2. 安装必要软件

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装JDK
sudo apt install openjdk-8-jdk -y

# 安装MySQL
sudo apt install mysql-server -y

# 安装Tomcat
sudo apt install tomcat9 -y
```

### 3. 配置MySQL

```bash
# 安全配置
sudo mysql_secure_installation

# 创建数据库用户
sudo mysql -u root -p
CREATE DATABASE messageboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'msgboard'@'localhost' IDENTIFIED BY 'strong_password_here';
GRANT ALL PRIVILEGES ON messageboard.* TO 'msgboard'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 导入数据库结构
mysql -u msgboard -p messageboard < database_schema.sql
```

### 4. 配置Tomcat

编辑 `/etc/tomcat9/server.xml`：

```xml
<!-- 设置UTF-8编码 -->
<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"
           URIEncoding="UTF-8" />
```

### 5. 部署应用

```bash
# 上传WAR文件到服务器
scp target/messageboard.war user@server:/tmp/

# 在服务器上
sudo cp /tmp/messageboard.war /var/lib/tomcat9/webapps/

# 重启Tomcat
sudo systemctl restart tomcat9

# 检查日志
sudo tail -f /var/log/tomcat9/catalina.out
```

### 6. 配置防火墙

```bash
# 开放8080端口
sudo ufw allow 8080/tcp

# 或者配置Nginx反向代理
sudo apt install nginx -y
```

### 7. 配置Nginx反向代理（可选）

创建 `/etc/nginx/sites-available/messageboard`：

```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:8080/messageboard/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 上传文件大小限制
    client_max_body_size 10M;
}
```

启用站点：

```bash
sudo ln -s /etc/nginx/sites-available/messageboard /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 8. 配置SSL（可选）

```bash
# 使用Let's Encrypt
sudo apt install certbot python3-certbot-nginx -y

# 获取SSL证书
sudo certbot --nginx -d yourdomain.com

# 自动续期
sudo certbot renew --dry-run
```

## Docker部署（可选）

### 1. 创建Dockerfile

```dockerfile
FROM tomcat:9-jdk8

# 复制WAR文件
COPY target/messageboard.war /usr/local/tomcat/webapps/

# 暴露端口
EXPOSE 8080

# 启动Tomcat
CMD ["catalina.sh", "run"]
```

### 2. 创建docker-compose.yml

```yaml
version: '3'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: messageboard
      MYSQL_USER: msgboard
      MYSQL_PASSWORD: msgboardpass
    volumes:
      - mysql-data:/var/lib/mysql
      - ./database_schema.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "3306:3306"

  tomcat:
    build: .
    depends_on:
      - mysql
    ports:
      - "8080:8080"
    environment:
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: messageboard
      DB_USER: msgboard
      DB_PASS: msgboardpass

volumes:
  mysql-data:
```

### 3. 启动容器

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

## 性能优化

### 1. 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_user_id ON messages(user_id);

-- 定期清理旧数据
DELETE FROM messages WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- 优化表
OPTIMIZE TABLE messages;
OPTIMIZE TABLE users;
```

### 2. Tomcat优化

编辑 `$CATALINA_HOME/bin/setenv.sh`：

```bash
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m"
```

### 3. 连接池配置

在 `context.xml` 中配置数据库连接池：

```xml
<Resource name="jdbc/MessageBoardDB"
          auth="Container"
          type="javax.sql.DataSource"
          maxTotal="100"
          maxIdle="30"
          maxWaitMillis="10000"
          username="msgboard"
          password="password"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/messageboard"/>
```

## 监控和维护

### 1. 日志监控

```bash
# Tomcat日志
tail -f $CATALINA_HOME/logs/catalina.out

# 应用日志
tail -f $CATALINA_HOME/logs/localhost.*.log

# MySQL日志
sudo tail -f /var/log/mysql/error.log
```

### 2. 性能监控

```bash
# 使用JConsole监控JVM
jconsole

# 使用top查看资源使用
top
htop

# 查看Tomcat进程
ps aux | grep tomcat
```

### 3. 备份策略

```bash
# 数据库备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/messageboard"
mkdir -p $BACKUP_DIR

mysqldump -u msgboard -p messageboard > $BACKUP_DIR/messageboard_$DATE.sql
gzip $BACKUP_DIR/messageboard_$DATE.sql

# 保留最近7天的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
```

设置定时任务：

```bash
# 编辑crontab
crontab -e

# 每天凌晨2点备份
0 2 * * * /path/to/backup_script.sh
```

## 故障排查

### 常见问题

1. **应用无法启动**
   - 检查Tomcat日志
   - 验证数据库连接
   - 确认端口未被占用

2. **数据库连接失败**
   - 检查MySQL服务状态
   - 验证数据库配置
   - 检查防火墙设置

3. **文件上传失败**
   - 检查uploads目录权限
   - 验证文件大小限制
   - 查看Tomcat日志错误

4. **性能问题**
   - 优化数据库查询
   - 增加Tomcat内存
   - 使用连接池
   - 启用缓存

## 安全加固

### 1. 修改默认管理员密码

```sql
-- 登录MySQL后执行
UPDATE users SET password = SHA2('new_strong_password', 256) WHERE username = 'admin';
```

### 2. 配置HTTPS

参考上面的SSL配置章节

### 3. 限制文件上传类型

在代码中添加文件类型验证

### 4. 定期更新依赖

```bash
# 检查依赖更新
mvn versions:display-dependency-updates

# 更新依赖
mvn versions:use-latest-versions
```

## 技术支持

如遇到问题，请：
1. 查看应用日志
2. 检查数据库状态
3. 参考本文档
4. 提交Issue到GitHub
