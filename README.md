# 在线留言板系统

一个基于 Java Servlet + JDBC 的在线留言板系统，支持图片上传、表情包和用户认证。

## 功能特性

### 核心功能
- ✅ 用户留言发布（支持文本和图片）
- ✅ 图片上传与预览（PNG/JPG/GIF，最大2MB）
- ✅ Unicode表情符号选择器
- ✅ 用户登录/注销
- ✅ 管理员面板
- ✅ 响应式设计

### 安全特性
- ✅ SQL注入防护（PreparedStatement）
- ✅ XSS防护（输出转义）
- ✅ CSRF保护（Token验证）
- ✅ 图片上传安全校验（MIME类型 + ImageIO内容验证）
- ✅ 安全的文件命名（UUID）
- ✅ 路径穿越防护
- ✅ CSP（内容安全策略）
- ✅ HTTP-only Cookie

## 技术栈

- **后端**: Java Servlet 4.0, JDBC
- **数据库**: MySQL 8.0
- **前端**: JSP, JavaScript (ES6+), CSS3
- **构建工具**: Maven 3.x
- **服务器**: Tomcat 9.x 或 Jetty

## 快速开始

### 前置要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Tomcat 9.0+ 或其他Servlet容器

### 数据库设置

1. 创建数据库并执行初始化脚本：

```bash
mysql -u root -p < sql/schema.sql
```

2. 执行迁移脚本添加图片支持：

```bash
mysql -u root -p < sql/alter_001_add_image_path.sql
```

### 配置

修改 `src/main/java/com/messageboard/util/DBUtil.java` 中的数据库连接信息：

```java
private static final String URL = "jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

可选：在 `src/main/webapp/WEB-INF/web.xml` 中修改上传目录：

```xml
<context-param>
    <param-name>uploadDir</param-name>
    <param-value>/var/tmp/board_uploads</param-value>
</context-param>
```

### 构建和部署

1. 编译项目：

```bash
mvn clean package
```

2. 部署WAR文件到Tomcat：

```bash
cp target/message-board.war $TOMCAT_HOME/webapps/
```

3. 启动Tomcat：

```bash
$TOMCAT_HOME/bin/catalina.sh run
```

4. 访问应用：

```
http://localhost:8080/message-board/
```

### 默认账号

- **管理员账号**: admin
- **密码**: admin123

## 项目结构

```
.
├── sql/                          # 数据库脚本
│   ├── schema.sql               # 初始化脚本
│   └── alter_001_add_image_path.sql  # 迁移脚本
├── src/main/
│   ├── java/com/messageboard/
│   │   ├── dao/                 # 数据访问层
│   │   │   ├── MessageDao.java
│   │   │   └── UserDao.java
│   │   ├── filter/              # 过滤器
│   │   │   ├── AuthFilter.java
│   │   │   └── CsrfFilter.java
│   │   ├── model/               # 数据模型
│   │   │   ├── Message.java
│   │   │   └── User.java
│   │   ├── util/                # 工具类
│   │   │   └── DBUtil.java
│   │   └── web/                 # Servlet控制器
│   │       ├── AdminServlet.java
│   │       ├── ImageServlet.java
│   │       ├── LoginServlet.java
│   │       ├── LogoutServlet.java
│   │       └── MessageServlet.java
│   └── webapp/
│       ├── WEB-INF/
│       │   └── web.xml          # Web配置
│       ├── css/
│       │   └── styles.css       # 样式表
│       ├── js/
│       │   └── app.js           # 前端脚本
│       ├── admin.jsp            # 管理页面
│       ├── error.jsp            # 错误页面
│       ├── index.jsp            # 首页
│       └── login.jsp            # 登录页面
└── pom.xml                      # Maven配置
```

## API接口

### 留言相关

- `GET /api/messages` - 获取所有留言
- `POST /api/messages` - 创建新留言（支持multipart/form-data）
- `DELETE /api/messages/{id}` - 删除留言（仅管理员）

### 用户相关

- `POST /api/login` - 用户登录
- `POST /api/logout` - 用户注销

### 图片访问

- `GET /image/{filename}` - 访问上传的图片

## 安全说明

### 图片上传安全

1. **文件类型验证**：仅允许 PNG、JPG、GIF
2. **大小限制**：最大2MB
3. **内容校验**：使用 ImageIO 二次验证文件确实是图片
4. **安全命名**：使用UUID生成文件名，防止路径穿越
5. **访问控制**：图片仅通过 ImageServlet 访问，设置 nosniff 头

### CSRF防护

所有非GET请求都需要携带CSRF Token：
- 表单提交：通过隐藏字段或header传递
- AJAX请求：通过 X-CSRF-Token header传递

### XSS防护

- 所有用户输入在显示前都经过HTML转义
- CSP限制脚本和样式来源
- 图片仅允许同源、data、blob协议

## 开发

### 本地开发

```bash
# 使用Maven Jetty插件快速启动
mvn jetty:run

# 访问
http://localhost:8080/
```

### 调试

在IDE中配置Tomcat服务器，设置断点即可调试。

## 许可证

MIT License

## 作者

DARK20050101
