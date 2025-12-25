<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.util.SecurityUtil" %>
<%
    // Generate CSRF token if not exists
    String csrfToken = (String) session.getAttribute("csrfToken");
    if (csrfToken == null) {
        csrfToken = SecurityUtil.generateToken();
        session.setAttribute("csrfToken", csrfToken);
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="<%= csrfToken %>">
    <meta http-equiv="Content-Security-Policy" content="
        default-src 'self';
        script-src 'self' 'unsafe-inline';
        style-src 'self' 'unsafe-inline';
        img-src 'self' data:;
        font-src 'self';
        connect-src 'self';
        frame-ancestors 'none';
        base-uri 'self';
        form-action 'self';
    ">
    <title>登录 - 在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <h1>🔐 用户登录</h1>
            
            <form id="loginForm" onsubmit="login(event)">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username" class="form-control" 
                           placeholder="请输入用户名" required maxlength="50">
                </div>
                
                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="请输入密码" required>
                </div>
                
                <div class="checkbox-wrapper">
                    <input type="checkbox" id="rememberMe" name="rememberMe" value="true">
                    <label for="rememberMe">记住我（30天）</label>
                </div>
                
                <button type="submit" class="btn btn-primary" style="width: 100%;">登录</button>
            </form>
            
            <div style="margin-top: 20px; text-align: center;">
                <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-secondary">返回首页</a>
            </div>
            
            <div style="margin-top: 30px; padding: 15px; background: #f0f0f0; border-radius: 8px; font-size: 12px;">
                <p style="margin: 0; color: #666;"><strong>测试账号：</strong></p>
                <p style="margin: 5px 0; color: #666;">用户名: admin</p>
                <p style="margin: 0; color: #666;">密码: admin123</p>
            </div>
        </div>
    </div>

    <script>
        csrfToken = '<%= csrfToken %>';
    </script>
    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
