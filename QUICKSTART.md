# 快速开始指南

这是一个5分钟快速启动指南，帮助您快速运行本留言板系统。

## 前置条件

确保已安装：
- JDK 1.8+
- MySQL 5.7+
- Maven 3.6+

## 快速启动步骤

### 1. 克隆项目

```bash
git clone https://github.com/DARK20050101/web.git
cd web
```

### 2. 配置数据库

#### 启动MySQL服务

```bash
# Windows
net start mysql

# Linux/macOS
sudo service mysql start
# 或
brew services start mysql
```

#### 创建数据库

```bash
# 登录MySQL
mysql -u root -p

# 导入数据库
mysql -u root -p < database_schema.sql
```

#### 修改数据库配置

编辑 `src/main/resources/db.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/messageboard?useSSL=false&serverTimezone=UTC
db.username=root
db.password=你的MySQL密码
```

### 3. 编译项目

```bash
mvn clean package
```

### 4. 运行项目

#### 方式一：使用Maven插件（推荐）

```bash
mvn tomcat7:run
```

#### 方式二：部署到Tomcat

```bash
# 复制WAR到Tomcat
cp target/messageboard.war $CATALINA_HOME/webapps/

# 启动Tomcat
cd $CATALINA_HOME/bin
./startup.sh  # Linux/macOS
# 或
startup.bat   # Windows
```

### 5. 访问系统

打开浏览器访问：

- **主页**: http://localhost:8080/messageboard/
- **登录**: http://localhost:8080/messageboard/login.jsp
- **注册**: http://localhost:8080/messageboard/register.jsp

### 6. 登录系统

#### 管理员账号（默认）
- 用户名：`admin`
- 密码：`admin123`

#### 普通用户
先注册一个新账号，然后登录

## 功能演示

### 匿名用户
1. 访问主页
2. 填写昵称和留言内容
3. 输入验证码（点击可刷新）
4. 可选上传图片
5. 点击"发表留言"

### 注册用户
1. 点击"注册"
2. 填写用户名、邮箱、密码
3. 提交注册
4. 使用新账号登录
5. 发表留言（无需验证码）
6. 可以编辑和删除自己的留言

### 管理员
1. 使用管理员账号登录
2. 点击"管理后台"
3. 管理用户和留言
4. 可以删除任何留言

## 常见问题

### Q1: Maven编译失败
```bash
# 清理Maven缓存
mvn clean

# 强制更新依赖
mvn clean install -U
```

### Q2: 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库名称为 `messageboard`
- 验证用户名和密码是否正确
- 检查 `db.properties` 配置

### Q3: 端口8080被占用
```bash
# 查看占用端口的进程
# Windows
netstat -ano | findstr :8080

# Linux/macOS
lsof -i :8080

# 修改端口（在pom.xml中）
<configuration>
    <port>8081</port>  <!-- 改为其他端口 -->
</configuration>
```

### Q4: 页面显示乱码
- 确保所有文件使用UTF-8编码
- 检查浏览器编码设置
- 验证web.xml中的编码配置

### Q5: 验证码不显示
- 确保JDK包含图形库支持
- 检查浏览器控制台错误信息
- 验证CaptchaServlet映射正确

## 测试数据

系统已包含一个默认管理员账号：
- 用户名：admin
- 密码：admin123

您可以：
1. 注册几个测试账号
2. 发表一些测试留言
3. 测试编辑和删除功能
4. 测试管理员后台功能

## 目录说明

```
web/
├── database_schema.sql      # 数据库初始化脚本
├── pom.xml                  # Maven配置文件
├── README.md                # 详细文档
├── DEPLOYMENT.md            # 部署指南
├── PROJECT_SUMMARY.md       # 项目总结
├── QUICKSTART.md           # 本文件
└── src/
    └── main/
        ├── java/            # Java源代码
        ├── resources/       # 配置文件
        └── webapp/          # Web资源文件
```

## 下一步

- 📖 查看 [README.md](README.md) 了解详细功能
- 🚀 查看 [DEPLOYMENT.md](DEPLOYMENT.md) 了解生产环境部署
- 📝 查看 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) 了解项目实现

## 停止服务

### Maven方式
按 `Ctrl + C` 停止

### Tomcat方式
```bash
cd $CATALINA_HOME/bin
./shutdown.sh  # Linux/macOS
# 或
shutdown.bat   # Windows
```

## 获取帮助

如遇到问题：
1. 查看Tomcat日志：`$CATALINA_HOME/logs/catalina.out`
2. 查看浏览器控制台错误信息
3. 检查数据库连接状态
4. 提交Issue到GitHub

## 安全提示

⚠️ **生产环境部署前请务必：**
1. 修改默认管理员密码
2. 配置HTTPS
3. 更新数据库密码
4. 检查文件上传安全设置
5. 启用防火墙规则

祝您使用愉快！ 🎉
