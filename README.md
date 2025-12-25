# 在线留言板系统 (Web Message Board)

一个功能完整的在线留言板系统，支持图片上传、表情包、用户登录、CSRF保护等安全特性。

## 主要特性

- ✅ 用户登录与会话管理（Session + Cookie 持久登录）
- ✅ 图片上传功能（支持 PNG/JPEG/GIF，最大2MB）
- ✅ 表情包快速插入（28种常用emoji）
- ✅ 图片预览与缩略图展示
- ✅ 安全防护：SQL注入防御、XSS防御、CSRF保护
- ✅ 管理员后台管理
- ✅ 响应式界面设计
- ✅ 匿名发布（需验证码）
- ✅ 图片安全访问控制

## 技术栈

- **后端**: Java Servlet 4.0 + JDBC
- **前端**: JSP + JavaScript + CSS3
- **数据库**: MySQL 8.0+
- **构建工具**: Maven
- **容器**: Tomcat 9.0+ / 其他支持 Servlet 4.0 的容器

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Tomcat 9.0+ 或其他 Servlet 4.0 容器

### 2. 数据库配置

```bash
# 创建数据库并导入schema
mysql -u root -p < sql/schema.sql
```

配置数据库连接（通过环境变量）：
```bash
export DB_URL="jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4"
export DB_USER="root"
export DB_PASSWORD="your_password"
```

### 3. 构建项目

```bash
mvn clean package
```

### 4. 部署

将生成的 `target/messageboard.war` 部署到 Tomcat 的 `webapps` 目录。

### 5. 配置上传目录

编辑 `src/main/webapp/WEB-INF/web.xml`，修改 `uploadDir` 参数：

```xml
<context-param>
    <param-name>uploadDir</param-name>
    <param-value>/var/tmp/board_uploads</param-value>
</context-param>
```

或者使用默认的系统临时目录。

### 6. 访问应用

访问: `http://localhost:8080/messageboard/`

默认管理员账号:
- 用户名: `admin`
- 密码: `admin123`

## 项目结构

```
.
├── sql/                           # 数据库脚本
│   ├── schema.sql                # 初始化schema
│   └── alter_001_add_image_path.sql  # 迁移脚本
├── src/main/
│   ├── java/com/messageboard/
│   │   ├── dao/                  # 数据访问层
│   │   │   ├── MessageDao.java
│   │   │   └── UserDao.java
│   │   ├── filter/               # 过滤器
│   │   │   ├── AuthFilter.java
│   │   │   └── CsrfFilter.java
│   │   ├── model/                # 数据模型
│   │   │   ├── Message.java
│   │   │   └── User.java
│   │   ├── util/                 # 工具类
│   │   │   ├── DBUtil.java
│   │   │   └── SecurityUtil.java
│   │   └── web/                  # Servlet
│   │       ├── CaptchaServlet.java
│   │       ├── ImageServlet.java
│   │       ├── LoginServlet.java
│   │       └── MessageServlet.java
│   └── webapp/
│       ├── WEB-INF/
│       │   └── web.xml           # Web应用配置
│       ├── css/
│       │   └── styles.css        # 样式文件
│       ├── js/
│       │   └── app.js            # 前端JavaScript
│       ├── admin.jsp             # 管理员页面
│       ├── error.jsp             # 错误页面
│       ├── index.jsp             # 主页
│       └── login.jsp             # 登录页面
└── pom.xml                       # Maven配置
```

## 安全特性

### SQL注入防御
- 所有数据库查询使用 PreparedStatement
- 参数化查询，不拼接SQL

### XSS防御
- 输出时使用 HTML转义
- Content-Security-Policy (CSP) 头部限制
- 用户输入内容严格转义

### CSRF防护
- 基于Token的CSRF保护
- 所有状态变更操作需要验证CSRF token

### 图片上传安全
- 文件类型白名单验证（仅允许 PNG/JPEG/GIF）
- 文件大小限制（2MB）
- ImageIO二次验证确保文件为真实图片
- UUID随机文件名，防止路径穿越
- 通过专用 Servlet 输出图片，设置 nosniff 头

### 密码安全
- SHA-256 哈希存储
- 会话管理与Cookie安全配置

## 功能说明

### 发表留言
- 登录用户可直接发表
- 匿名用户需提供昵称和完成验证码
- 支持上传图片附件
- 支持插入emoji表情

### 图片上传
- 支持 PNG、JPEG、GIF 格式
- 最大文件大小 2MB
- 实时预览功能
- 安全的文件名生成
- 图片通过 `/image/<filename>` 访问

### 管理功能
- 管理员可删除任何留言
- 查看系统统计信息
- 管理所有留言

## 数据库迁移

如果是已有系统需要添加图片支持：

```bash
mysql -u root -p messageboard < sql/alter_001_add_image_path.sql
```

## 开发与调试

### 本地开发

```bash
# 编译
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package

# 使用Maven Tomcat插件运行
mvn tomcat7:run
```

### 查看日志

Tomcat日志位置: `$TOMCAT_HOME/logs/catalina.out`

## 常见问题

### 1. 图片上传失败
检查上传目录权限，确保Tomcat有写入权限：
```bash
mkdir -p /var/tmp/board_uploads
chmod 755 /var/tmp/board_uploads
chown tomcat:tomcat /var/tmp/board_uploads
```

### 2. 数据库连接失败
检查环境变量是否正确设置，或修改 `DBUtil.java` 中的默认配置。

### 3. CSRF token 错误
清除浏览器 Cookie 和 Session，重新登录。

## License

MIT License

## 作者

Message Board System Development Team
