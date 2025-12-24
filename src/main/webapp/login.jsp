<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String csrfToken = (String) session.getAttribute("csrfToken");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="<%= csrfToken %>">
    <meta http-equiv="Content-Security-Policy" 
          content="default-src 'self'; 
                   script-src 'self'; 
                   style-src 'self' 'unsafe-inline'; 
                   img-src 'self'; 
                   font-src 'self'; 
                   connect-src 'self';">
    <title>登录 - 在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="login-container">
        <div class="card">
            <h1 style="text-align: center; color: #667eea; margin-bottom: 30px;">🔐 用户登录</h1>
            
            <form id="loginForm">
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input type="text" id="username" name="username" class="form-control" required autofocus>
                </div>

                <div class="form-group">
                    <label for="password">密码</label>
                    <input type="password" id="password" name="password" class="form-control" required>
                </div>

                <div class="form-group checkbox-group">
                    <input type="checkbox" id="remember" name="remember" value="true">
                    <label for="remember">记住我（7天）</label>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%; margin-bottom: 15px;">登录</button>
                
                <div style="text-align: center;">
                    <a href="<%= request.getContextPath() %>/" class="btn btn-link">返回首页</a>
                </div>
            </form>
            
            <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #999; font-size: 13px; text-align: center;">
                <p>默认管理员账号：admin / admin123</p>
            </div>
        </div>
    </div>

    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
