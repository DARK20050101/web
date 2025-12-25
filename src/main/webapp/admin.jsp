<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.model.User" %>
<%@ page import="com.messageboard.util.SecurityUtil" %>
<%
    User user = (User) session.getAttribute("user");
    
    // Check if user is admin
    if (user == null || !user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
        return;
    }
    
    String username = user.getUsername();
    
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
    <meta name="context-path" content="<%= request.getContextPath() %>">
    <meta http-equiv="Content-Security-Policy" content="
        default-src 'self';
        script-src 'self' 'unsafe-inline';
        style-src 'self' 'unsafe-inline';
        img-src 'self' data: blob:;
        font-src 'self';
        connect-src 'self';
        frame-ancestors 'none';
        base-uri 'self';
        form-action 'self';
    ">
    <title>管理面板 - 在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>🛠️ 管理面板</h1>
            <div class="user-info">
                <span>Administrator: <strong><%= SecurityUtil.escapeHtml(username) %></strong></span>
                <div>
                    <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-secondary">返回首页</a>
                    <button onclick="logout()" class="btn btn-secondary">退出登录</button>
                </div>
            </div>
        </div>

        <!-- Statistics -->
        <div class="message-form">
            <h2>📊 系统统计</h2>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px;">
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                    <h3 style="margin: 0; color: #667eea;">总留言数</h3>
                    <p style="font-size: 2em; margin: 10px 0; font-weight: bold;" id="totalMessages">-</p>
                </div>
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                    <h3 style="margin: 0; color: #667eea;">在线用户</h3>
                    <p style="font-size: 2em; margin: 10px 0; font-weight: bold;">1</p>
                </div>
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                    <h3 style="margin: 0; color: #667eea;">系统状态</h3>
                    <p style="font-size: 1.5em; margin: 10px 0; color: #10b981; font-weight: bold;">运行中</p>
                </div>
            </div>
        </div>

        <!-- All Messages with Management -->
        <div class="message-list">
            <h2>📬 所有留言管理</h2>
            <p style="color: #666; margin-bottom: 20px;">在此页面，您可以查看和删除所有留言。</p>
            <div id="messageList">
                <p style="text-align: center; color: #999;">Loading messages...</p>
            </div>
        </div>
    </div>

    <script>
        window.currentUser = {"username":"<%= SecurityUtil.escapeHtml(username) %>","isAdmin":true};
        csrfToken = '<%= csrfToken %>';
        contextPath = '<%= request.getContextPath() %>';
        
        // Load messages and update statistics
        document.addEventListener('DOMContentLoaded', function() {
            fetch(contextPath + '/api/messages')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('totalMessages').textContent = data.messages.length;
                    }
                });
        });
    </script>
    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
