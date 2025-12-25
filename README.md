# 在线留言板系统

一个功能完善的在线留言板系统，支持用户管理、身份验证、匿名留言、图片上传等功能。

## 功能特性

### 1. 用户管理与身份验证
- ✅ 用户注册与登录
- ✅ 密码加密存储（SHA-256）
- ✅ 验证码验证（防止恶意刷留言）
- ✅ 记住我功能（Cookie持久化）
- ✅ Session管理
- ✅ 管理员权限控制

### 2. 留言功能
- ✅ 匿名留言（需输入昵称和验证码）
- ✅ 注册用户留言
- ✅ 留言内容编辑（仅限留言者本人或管理员）
- ✅ 留言删除（仅限留言者本人或管理员）
- ✅ 图片/表情包上传
- ✅ 留言列表分页显示
- ✅ 按时间倒序展示（最新留言优先）

### 3. 管理员后台
- ✅ 用户管理（增删查改）
- ✅ 留言管理（查看、删除）
- ✅ 权限控制（仅管理员可访问）
- ✅ 数据统计展示

### 4. 安全防护
- ✅ SQL注入防护（PreparedStatement）
- ✅ XSS攻击防护（输入过滤和HTML转义）
- ✅ CSRF攻击防护（Token验证）
- ✅ Session验证
- ✅ 文件上传安全控制

### 5. 前后端交互
- ✅ 同步表单提交
- ✅ 异步AJAX请求（JSON格式）
- ✅ RESTful API设计
- ✅ 友好的错误提示

## 技术栈

### 后端技术
- **Java Servlet** - Web应用框架
- **JDBC** - 数据库连接
- **MySQL** - 数据库
- **JSP** - 页面模板
- **Maven** - 项目管理和构建

### 前端技术
- **HTML5** - 页面结构
- **CSS3** - 样式设计
- **JavaScript (原生)** - 交互逻辑
- **AJAX** - 异步数据交互

### 安全技术
- **SHA-256** - 密码加密
- **验证码** - 防止机器人攻击
- **CSRF Token** - 跨站请求伪造防护
- **XSS过滤** - 跨站脚本攻击防护
- **PreparedStatement** - SQL注入防护

## 项目结构

```
web/
├── database_schema.sql                 # 数据库建表脚本
├── pom.xml                             # Maven配置文件
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── messageboard/
│       │           ├── dao/            # 数据访问层
│       │           │   ├── UserDAO.java
│       │           │   └── MessageDAO.java
│       │           ├── model/          # 数据模型
│       │           │   ├── User.java
│       │           │   └── Message.java
│       │           ├── service/        # 业务逻辑层
│       │           │   ├── UserService.java
│       │           │   └── MessageService.java
│       │           ├── servlet/        # Servlet控制层
│       │           │   ├── LoginServlet.java
│       │           │   ├── LogoutServlet.java
│       │           │   ├── RegisterServlet.java
│       │           │   ├── CaptchaServlet.java
│       │           │   ├── MessageServlet.java
│       │           │   └── AdminServlet.java
│       │           ├── filter/         # 过滤器
│       │           │   ├── AuthFilter.java
│       │           │   └── CSRFFilter.java
│       │           └── util/           # 工具类
│       │               ├── DBUtil.java
│       │               ├── SecurityUtil.java
│       │               └── CaptchaUtil.java
│       ├── resources/
│       │   └── db.properties           # 数据库配置
│       └── webapp/
│           ├── WEB-INF/
│           │   └── web.xml             # Web应用配置
│           ├── admin/
│           │   └── dashboard.jsp       # 管理后台
│           ├── error/
│           │   ├── 404.jsp
│           │   └── 500.jsp
│           ├── css/
│           │   ├── style.css
│           │   └── admin.css
│           ├── js/
│           │   ├── messageboard.js
│           │   └── admin.js
│           ├── uploads/                # 上传文件目录
│           ├── index.jsp               # 留言板主页
│           ├── login.jsp               # 登录页面
│           └── register.jsp            # 注册页面
└── README.md
```

## 安装与部署

### 1. 环境要求
- JDK 1.8+
- MySQL 5.7+
- Apache Tomcat 8.5+
- Maven 3.6+

### 2. 数据库配置

创建数据库并导入表结构：

```bash
mysql -u root -p < database_schema.sql
```

修改数据库配置文件 `src/main/resources/db.properties`：

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
```

### 3. 编译项目

```bash
mvn clean package
```

### 4. 部署到Tomcat

方式一：将生成的 `target/messageboard.war` 复制到Tomcat的 `webapps` 目录

方式二：使用Maven Tomcat插件运行：

```bash
mvn tomcat7:run
```

### 5. 访问应用

浏览器访问：`http://localhost:8080/messageboard`

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

## 系统架构

### 分层架构设计

```
┌─────────────────────────────────────┐
│         表现层 (Presentation)        │
│     JSP + Servlet + JavaScript      │
├─────────────────────────────────────┤
│         业务逻辑层 (Service)         │
│   UserService + MessageService      │
├─────────────────────────────────────┤
│        数据访问层 (DAO)              │
│      UserDAO + MessageDAO            │
├─────────────────────────────────────┤
│         数据库层 (Database)          │
│            MySQL                     │
└─────────────────────────────────────┘
```

### 安全防护机制

#### 1. SQL注入防护
使用PreparedStatement预编译SQL语句：

```java
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);
```

#### 2. XSS攻击防护
对用户输入进行HTML转义：

```java
public static String sanitizeHtml(String input) {
    return input.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
}
```

#### 3. CSRF攻击防护
使用CSRF Token验证：

```java
// 生成Token
String csrfToken = SecurityUtil.generateCSRFToken();
session.setAttribute("csrfToken", csrfToken);

// 验证Token
String sessionToken = (String) session.getAttribute("csrfToken");
String requestToken = request.getParameter("csrfToken");
if (!sessionToken.equals(requestToken)) {
    // 拒绝请求
}
```

#### 4. 密码加密
使用SHA-256哈希算法：

```java
public static String hashPassword(String password) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
    // 转换为十六进制字符串
}
```

## API接口文档

### 用户相关

#### 登录
- **URL**: `/login`
- **Method**: `POST`
- **参数**: 
  - `username`: 用户名
  - `password`: 密码
  - `captcha`: 验证码
  - `rememberMe`: 记住我（可选）

#### 注册
- **URL**: `/register`
- **Method**: `POST`
- **参数**: 
  - `username`: 用户名
  - `password`: 密码
  - `confirmPassword`: 确认密码
  - `email`: 邮箱

#### 登出
- **URL**: `/logout`
- **Method**: `GET/POST`

### 留言相关

#### 获取留言列表
- **URL**: `/message?action=list&page={page}`
- **Method**: `GET`
- **返回**: JSON格式的留言列表

#### 发布留言
- **URL**: `/message?action=create`
- **Method**: `POST`
- **参数**: 
  - `nickname`: 昵称（匿名用户）
  - `content`: 留言内容
  - `captcha`: 验证码（匿名用户）
  - `image`: 图片文件（可选）
  - `csrfToken`: CSRF令牌

#### 编辑留言
- **URL**: `/message?action=update`
- **Method**: `POST`
- **参数**: 
  - `id`: 留言ID
  - `content`: 新内容
  - `image`: 新图片（可选）
  - `csrfToken`: CSRF令牌

#### 删除留言
- **URL**: `/message?action=delete`
- **Method**: `POST`
- **参数**: 
  - `id`: 留言ID
  - `csrfToken`: CSRF令牌

### 管理员相关

#### 用户列表
- **URL**: `/admin/api?action=list&type=users`
- **Method**: `GET`
- **权限**: 仅管理员

#### 留言列表
- **URL**: `/admin/api?action=list&type=messages&page={page}`
- **Method**: `GET`
- **权限**: 仅管理员

#### 删除用户
- **URL**: `/admin/api?action=delete&type=user`
- **Method**: `POST`
- **权限**: 仅管理员

## 功能演示

### 1. 用户注册流程
1. 访问注册页面
2. 填写用户名、邮箱、密码
3. 提交注册表单
4. 系统验证并创建账号
5. 跳转到登录页面

### 2. 匿名留言流程
1. 访问主页（无需登录）
2. 填写昵称和留言内容
3. 输入验证码
4. 可选上传图片
5. 提交留言

### 3. 管理员操作流程
1. 使用管理员账号登录
2. 访问管理后台
3. 查看用户列表和留言列表
4. 对用户和留言进行管理操作

## 代码规范

### Java代码规范
- 遵循Java命名约定
- 使用有意义的变量名和方法名
- 每个类职责单一
- 添加必要的注释

### 前端代码规范
- 语义化HTML标签
- CSS类名采用小写加连字符
- JavaScript使用驼峰命名
- 代码格式化和缩进统一

## 常见问题

### Q: 无法连接数据库
A: 检查 `db.properties` 配置是否正确，确保MySQL服务已启动

### Q: 验证码不显示
A: 确保Servlet映射正确，检查 `web.xml` 配置

### Q: 文件上传失败
A: 检查 `uploads` 目录权限，确保应用有写入权限

### Q: 登录后无法访问管理后台
A: 检查用户的 `is_admin` 字段是否为 `true`

## 后续优化方向

1. 添加邮箱验证功能
2. 实现留言点赞和评论功能
3. 添加富文本编辑器
4. 实现站内消息通知
5. 优化数据库查询性能
6. 添加缓存机制（Redis）
7. 实现WebSocket实时通信
8. 添加单元测试和集成测试

## 许可证

MIT License

## 作者

DARK20050101

## 联系方式

如有问题或建议，请提交Issue或Pull Request。
