# 安全文档 (Security Documentation)

## 安全特性概述

本系统实现了多层次的安全防护措施，确保数据和用户的安全。

## 已实施的安全措施

### 1. 输入验证和输出编码

#### SQL 注入防护
- ✅ 所有数据库查询使用 `PreparedStatement`
- ✅ 参数化查询，避免直接拼接 SQL
- ✅ 在 DAO 层统一处理数据库操作

**示例代码**:
```java
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);
```

#### XSS（跨站脚本）防护
- ✅ 使用 `HtmlUtil.escapeHtml()` 对所有用户输入进行 HTML 转义
- ✅ JSP 页面输出时统一使用转义函数
- ✅ Content Security Policy (CSP) 头限制脚本来源

**转义的字符**:
- `<` → `&lt;`
- `>` → `&gt;`
- `&` → `&amp;`
- `"` → `&quot;`
- `'` → `&#x27;`
- `/` → `&#x2F;`

**CSP 策略**:
```
default-src 'self';
script-src 'self';
style-src 'self' 'unsafe-inline';
img-src 'self' data: blob:;
connect-src 'self';
```

### 2. CSRF（跨站请求伪造）防护

#### Token 机制
- ✅ `CsrfFilter` 为每个 session 生成唯一的 token
- ✅ 所有非 GET 请求必须携带有效的 CSRF token
- ✅ Token 使用 `SecureRandom` 生成，保证随机性

**Token 验证**:
- Header: `X-CSRF-Token`
- Form 参数: `csrfToken`

**示例代码**:
```javascript
fetch('/api/messages', {
    method: 'POST',
    headers: {
        'X-CSRF-Token': getCsrfToken()
    },
    body: formData
});
```

### 3. 身份认证和授权

#### Session 管理
- ✅ HTTP-only Cookie 防止 JavaScript 访问
- ✅ 可配置的 session 超时时间（默认 30 分钟）
- ✅ "记住我" 功能延长 session 至 7 天
- ✅ 退出时 session 立即失效

#### 访问控制
- ✅ `AuthFilter` 保护需要登录的页面
- ✅ 管理员权限检查
- ✅ 删除操作仅限管理员

**受保护的路径**:
- `/admin.jsp` - 管理员面板
- `/admin` - 管理员操作

### 4. 文件上传安全

#### 多重验证机制

**第一层：MIME 类型验证**
```java
Set<String> ALLOWED_MIME_TYPES = {
    "image/png",
    "image/jpeg", 
    "image/gif"
};
```

**第二层：文件大小限制**
- 最大单个文件：2MB
- 最大请求大小：3MB

**第三层：内容验证**
```java
BufferedImage image = ImageIO.read(inputStream);
if (image == null) {
    // 不是有效的图片文件
    return null;
}
```

**第四层：安全文件命名**
```java
String filename = UUID.randomUUID().toString() + "." + extension;
```

#### 文件访问控制
- ✅ 图片仅通过 `ImageServlet` 访问
- ✅ 路径穿越防护
- ✅ 文件名必须匹配 UUID 模式
- ✅ 设置 `X-Content-Type-Options: nosniff` 头

**路径验证**:
```java
String canonicalUploadDir = new File(uploadDir).getCanonicalPath();
String canonicalFilePath = file.getCanonicalPath();
if (!canonicalFilePath.startsWith(canonicalUploadDir)) {
    // 路径穿越攻击
    return 403 Forbidden;
}
```

### 5. 密码安全

#### 当前实现（开发/演示）
⚠️ **警告**: 当前版本使用明文密码比较，仅用于开发和演示。

#### 生产环境建议

**使用 BCrypt**:
```java
// 添加依赖
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

// 密码哈希
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

// 密码验证
boolean isValid = BCrypt.checkpw(password, hashedPassword);
```

**或使用 PasswordUtil**:

项目中已包含 `PasswordUtil` 类，提供基于 SHA-256 的密码哈希（建议生产环境使用 BCrypt）。

### 6. 数据库安全

#### 连接安全
⚠️ **开发环境配置**:
```java
useSSL=false  // 仅用于开发
```

✅ **生产环境配置**:
```java
useSSL=true
requireSSL=true
verifyServerCertificate=true
```

#### 最小权限原则
建议为应用创建专用数据库用户：

```sql
CREATE USER 'msgboard_app'@'localhost' IDENTIFIED BY 'strong_password';
GRANT SELECT, INSERT, UPDATE, DELETE ON messageboard.* TO 'msgboard_app'@'localhost';
FLUSH PRIVILEGES;
```

### 7. HTTP 头安全

#### 已实施的安全头

**Content Security Policy (CSP)**
```
Content-Security-Policy: default-src 'self'; script-src 'self'; ...
```

**X-Content-Type-Options**
```
X-Content-Type-Options: nosniff
```

**建议添加的头**:

```java
// 在 Filter 中添加
response.setHeader("X-Frame-Options", "DENY");
response.setHeader("X-XSS-Protection", "1; mode=block");
response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
```

## 安全配置清单

### 部署前必须完成

- [ ] 修改默认管理员密码
- [ ] 配置强密码策略
- [ ] 启用数据库 SSL 连接
- [ ] 配置 HTTPS（生产环境）
- [ ] 设置 secure flag on cookies（HTTPS 环境）
- [ ] 将数据库凭据移至环境变量
- [ ] 配置合适的上传目录权限
- [ ] 实施密码哈希（使用 BCrypt）
- [ ] 启用应用日志记录
- [ ] 配置防火墙规则

### 定期安全任务

- [ ] 审查访问日志
- [ ] 更新依赖包（检查安全漏洞）
- [ ] 进行渗透测试
- [ ] 备份数据（测试恢复）
- [ ] 审查用户权限
- [ ] 检查上传文件

## 已知限制和风险

### 当前版本的限制

1. **密码存储**: 默认使用明文存储（需要升级到 BCrypt）
2. **速率限制**: 未实施登录尝试限制（建议添加）
3. **日志记录**: 基础的日志功能（建议使用日志框架）
4. **验证码**: 未实施验证码功能（建议添加防止自动化攻击）

### 建议的改进

#### 1. 实施速率限制
```java
// 防止暴力破解
public class RateLimitFilter implements Filter {
    // 限制每 IP 每分钟 5 次登录尝试
}
```

#### 2. 添加验证码
```java
// 使用 Google reCAPTCHA 或类似服务
```

#### 3. 增强日志记录
```java
// 记录安全事件
- 登录尝试（成功/失败）
- 文件上传
- 权限检查失败
- CSRF token 验证失败
```

#### 4. 实施 API 速率限制
```java
// 防止 API 滥用
```

## 安全事件响应

### 如果发现安全问题

1. **立即行动**:
   - 确认问题范围
   - 隔离受影响系统
   - 收集日志和证据

2. **修复漏洞**:
   - 应用安全补丁
   - 更新密码和密钥
   - 审查访问权限

3. **事后分析**:
   - 调查根本原因
   - 更新安全措施
   - 记录经验教训

## 安全联系方式

如发现安全漏洞，请通过以下方式报告：
- GitHub Security Advisory（推荐）
- 项目 Issues（非紧急问题）

## 参考资源

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP Java Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Java_Security_Cheat_Sheet.html)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)

## 版本历史

### v1.0.0 (2025-12-24)
- 初始安全实现
- SQL 注入防护
- XSS 防护
- CSRF 防护
- 文件上传安全
- 基础认证和授权

---

最后更新：2025-12-24
