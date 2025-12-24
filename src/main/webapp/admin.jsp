<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.model.User" %>
<%@ page import="java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    String csrfToken = (String) session.getAttribute("csrfToken");
    List<User> users = (List<User>) request.getAttribute("users");
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
    <title>管理面板 - 在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🛠️ 管理面板</h1>
            <div class="user-info">
                <span>管理员: <strong><%= currentUser.getUsername().replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&#x27;") %></strong></span>
                <a href="<%= request.getContextPath() %>/" class="btn btn-secondary">返回首页</a>
                <button onclick="logout()" class="btn btn-secondary">退出</button>
            </div>
        </div>

        <div class="card">
            <h2>用户列表</h2>
            
            <% if (users != null && !users.isEmpty()) { %>
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>用户名</th>
                        <th>邮箱</th>
                        <th>角色</th>
                        <th>创建时间</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (User user : users) { %>
                    <tr>
                        <td><%= user.getId() %></td>
                        <td><%= user.getUsername().replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&#x27;") %></td>
                        <td><%= user.getEmail() != null ? user.getEmail().replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;").replace("\"", "&quot;").replace("'", "&#x27;") : "N/A" %></td>
                        <td>
                            <% if (user.isAdmin()) { %>
                                <span style="color: #dc3545; font-weight: bold;">管理员</span>
                            <% } else { %>
                                普通用户
                            <% } %>
                        </td>
                        <td><%= user.getCreatedAt() %></td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
            <% } else { %>
            <p>暂无用户数据</p>
            <% } %>
        </div>

        <div class="card">
            <h2>系统信息</h2>
            <p><strong>上传目录:</strong> <%= application.getInitParameter("uploadDir") %></p>
            <p><strong>会话超时:</strong> <%= session.getMaxInactiveInterval() / 60 %> 分钟</p>
        </div>
    </div>

    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
