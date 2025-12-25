# API 接口文档

## 基础信息

- **Base URL**: `http://localhost:8080/messageboard`
- **编码**: UTF-8
- **数据格式**: JSON / Form Data
- **认证方式**: Session + Cookie

## 认证相关 API

### 1. 用户登录

**接口地址**: `/login`  
**请求方式**: `POST`  
**内容类型**: `application/x-www-form-urlencoded`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码（明文，服务端加密） |
| captcha | String | 是 | 验证码 |
| rememberMe | String | 否 | 记住我（值为"on"） |

#### 请求示例

```http
POST /messageboard/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=admin&password=admin123&captcha=AB12&rememberMe=on
```

#### 响应说明

- **成功**: 重定向到主页或管理后台
- **失败**: 返回登录页面并显示错误信息

#### 错误信息

- `验证码错误`
- `用户名或密码错误`

---

### 2. 用户注册

**接口地址**: `/register`  
**请求方式**: `POST`  
**内容类型**: `application/x-www-form-urlencoded`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（3-20字符） |
| password | String | 是 | 密码（最少6字符） |
| confirmPassword | String | 是 | 确认密码 |
| email | String | 是 | 邮箱地址 |

#### 请求示例

```http
POST /messageboard/register HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=testuser&password=123456&confirmPassword=123456&email=test@example.com
```

#### 响应说明

- **成功**: 重定向到登录页面并显示成功消息
- **失败**: 返回注册页面并显示错误信息

#### 错误信息

- `所有字段都必须填写`
- `两次输入的密码不一致`
- `密码长度至少为6个字符`
- `用户名已存在`

---

### 3. 用户登出

**接口地址**: `/logout`  
**请求方式**: `GET` / `POST`

#### 响应说明

- 清除Session
- 重定向到登录页面

---

### 4. 获取验证码

**接口地址**: `/captcha`  
**请求方式**: `GET`  
**响应类型**: `image/jpeg`

#### 说明

- 生成4位随机验证码
- 验证码存储在Session中
- 返回验证码图片
- 可以点击刷新获取新验证码

---

## 留言相关 API

### 1. 获取留言列表

**接口地址**: `/message?action=list`  
**请求方式**: `GET`  
**响应类型**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `list` |
| page | Integer | 否 | 页码，默认1 |

#### 请求示例

```http
GET /messageboard/message?action=list&page=1 HTTP/1.1
```

#### 响应示例

```json
{
    "success": true,
    "messages": [
        {
            "id": 1,
            "userId": 2,
            "nickname": "张三",
            "content": "这是一条留言内容",
            "imagePath": "uploads/abc123.jpg",
            "isAnonymous": false,
            "createdAt": "2024-01-01 10:00:00",
            "updatedAt": "2024-01-01 10:00:00"
        }
    ],
    "currentPage": 1,
    "totalPages": 5,
    "totalCount": 50
}
```

---

### 2. 获取单条留言

**接口地址**: `/message?action=get`  
**请求方式**: `GET`  
**响应类型**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `get` |
| id | Integer | 是 | 留言ID |

#### 响应示例

```json
{
    "success": true,
    "message": {
        "id": 1,
        "userId": 2,
        "nickname": "张三",
        "content": "这是一条留言内容",
        "imagePath": "uploads/abc123.jpg",
        "isAnonymous": false,
        "createdAt": "2024-01-01 10:00:00",
        "updatedAt": "2024-01-01 10:00:00"
    }
}
```

---

### 3. 发布留言

**接口地址**: `/message?action=create`  
**请求方式**: `POST`  
**内容类型**: `multipart/form-data`  
**响应类型**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `create` |
| nickname | String | 条件 | 昵称（匿名用户必填） |
| content | String | 是 | 留言内容 |
| captcha | String | 条件 | 验证码（匿名用户必填） |
| image | File | 否 | 图片文件 |
| csrfToken | String | 是 | CSRF令牌 |

#### 请求示例

```http
POST /messageboard/message?action=create HTTP/1.1
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="nickname"

张三
------WebKitFormBoundary
Content-Disposition: form-data; name="content"

这是我的留言内容
------WebKitFormBoundary
Content-Disposition: form-data; name="captcha"

AB12
------WebKitFormBoundary
Content-Disposition: form-data; name="csrfToken"

abc123...
------WebKitFormBoundary--
```

#### 响应示例

```json
{
    "success": true,
    "message": "留言发布成功"
}
```

#### 错误响应

```json
{
    "success": false,
    "message": "验证码错误"
}
```

---

### 4. 编辑留言

**接口地址**: `/message?action=update`  
**请求方式**: `POST`  
**内容类型**: `multipart/form-data`  
**响应类型**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `update` |
| id | Integer | 是 | 留言ID |
| content | String | 是 | 新的留言内容 |
| image | File | 否 | 新的图片文件 |
| csrfToken | String | 是 | CSRF令牌 |

#### 权限要求

- 必须登录
- 只能编辑自己的留言或管理员可编辑所有留言

#### 响应示例

```json
{
    "success": true,
    "message": "留言更新成功"
}
```

---

### 5. 删除留言

**接口地址**: `/message?action=delete`  
**请求方式**: `POST`  
**内容类型**: `application/x-www-form-urlencoded`  
**响应类型**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `delete` |
| id | Integer | 是 | 留言ID |
| csrfToken | String | 是 | CSRF令牌 |

#### 权限要求

- 必须登录
- 只能删除自己的留言或管理员可删除所有留言

#### 响应示例

```json
{
    "success": true,
    "message": "留言删除成功"
}
```

---

## 管理员 API

### 1. 获取用户列表

**接口地址**: `/admin/api?action=list&type=users`  
**请求方式**: `GET`  
**响应类型**: `application/json`  
**权限**: 仅管理员

#### 响应示例

```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "username": "admin",
            "email": "admin@example.com",
            "admin": true,
            "createdAt": "2024-01-01 00:00:00"
        }
    ]
}
```

---

### 2. 获取留言列表（管理）

**接口地址**: `/admin/api?action=list&type=messages`  
**请求方式**: `GET`  
**响应类型**: `application/json`  
**权限**: 仅管理员

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `list` |
| type | String | 是 | 固定值: `messages` |
| page | Integer | 否 | 页码，默认1 |

#### 响应示例

```json
{
    "success": true,
    "data": [...],
    "totalPages": 10
}
```

---

### 3. 删除用户

**接口地址**: `/admin/api?action=delete&type=user`  
**请求方式**: `POST`  
**响应类型**: `application/json`  
**权限**: 仅管理员

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `delete` |
| type | String | 是 | 固定值: `user` |
| id | Integer | 是 | 用户ID |
| csrfToken | String | 是 | CSRF令牌 |

#### 响应示例

```json
{
    "success": true,
    "message": "删除成功"
}
```

---

### 4. 编辑用户

**接口地址**: `/admin/api?action=update&type=user`  
**请求方式**: `POST`  
**响应类型**: `application/json`  
**权限**: 仅管理员

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | String | 是 | 固定值: `update` |
| type | String | 是 | 固定值: `user` |
| id | Integer | 是 | 用户ID |
| username | String | 是 | 用户名 |
| email | String | 是 | 邮箱 |
| isAdmin | String | 否 | 是否管理员（"true"） |
| csrfToken | String | 是 | CSRF令牌 |

---

## 错误码说明

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 安全说明

### CSRF防护

所有POST/PUT/DELETE请求都需要包含CSRF Token：

```javascript
// 从页面获取CSRF Token
const csrfToken = document.querySelector('input[name="csrfToken"]').value;

// 在请求中包含Token
formData.append('csrfToken', csrfToken);
```

### Session管理

- Session超时时间：30分钟
- 登录后Session自动创建
- 退出登录Session自动销毁

### 文件上传限制

- 单个文件大小：10MB
- 请求总大小：50MB
- 允许的文件类型：图片文件（image/*）

## JavaScript调用示例

### 使用Fetch API

```javascript
// 获取留言列表
fetch('/messageboard/message?action=list&page=1')
    .then(response => response.json())
    .then(data => {
        console.log(data.messages);
    });

// 发布留言（含文件上传）
const formData = new FormData();
formData.append('action', 'create');
formData.append('content', '留言内容');
formData.append('csrfToken', csrfToken);

fetch('/messageboard/message', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => {
    console.log(data);
});
```

### 使用jQuery

```javascript
// 获取留言列表
$.ajax({
    url: '/messageboard/message',
    type: 'GET',
    data: {
        action: 'list',
        page: 1
    },
    success: function(data) {
        console.log(data.messages);
    }
});

// 删除留言
$.ajax({
    url: '/messageboard/message',
    type: 'POST',
    data: {
        action: 'delete',
        id: messageId,
        csrfToken: csrfToken
    },
    success: function(data) {
        console.log(data);
    }
});
```

## 注意事项

1. 所有请求必须使用UTF-8编码
2. 文件上传必须使用`multipart/form-data`
3. CSRF Token在每次Session创建时生成
4. 管理员API需要先登录且具有管理员权限
5. 匿名用户发布留言需要验证码
6. 图片上传是可选功能
7. 所有返回的HTML内容都已转义，防止XSS攻击

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现所有基础功能
- 完善安全防护机制
