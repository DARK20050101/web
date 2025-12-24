<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.model.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
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
                   img-src 'self' data: blob:; 
                   font-src 'self'; 
                   connect-src 'self';">
    <title>在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📝 在线留言板</h1>
            <div class="user-info">
                <% if (currentUser != null) { %>
                    <span>欢迎, <strong><%= currentUser.getUsername() %></strong></span>
                    <% if (currentUser.isAdmin()) { %>
                        <a href="<%= request.getContextPath() %>/admin" class="btn btn-secondary">管理面板</a>
                    <% } %>
                    <button onclick="logout()" class="btn btn-secondary">退出</button>
                <% } else { %>
                    <a href="<%= request.getContextPath() %>/login.jsp" class="btn btn-primary">登录</a>
                <% } %>
            </div>
        </div>

        <div class="card">
            <h2>发表留言</h2>
            <form id="messageForm" enctype="multipart/form-data">
                <% if (currentUser == null) { %>
                <div class="form-group">
                    <label for="nickname">昵称 *</label>
                    <input type="text" id="nickname" name="nickname" class="form-control" required>
                </div>
                <% } %>
                
                <div class="form-group">
                    <label for="content">留言内容 *</label>
                    
                    <!-- Emoji Panel -->
                    <div class="emoji-panel">
                        <button type="button" onclick="insertEmoji('😀')">😀</button>
                        <button type="button" onclick="insertEmoji('😊')">😊</button>
                        <button type="button" onclick="insertEmoji('😂')">😂</button>
                        <button type="button" onclick="insertEmoji('😍')">😍</button>
                        <button type="button" onclick="insertEmoji('🤔')">🤔</button>
                        <button type="button" onclick="insertEmoji('👍')">👍</button>
                        <button type="button" onclick="insertEmoji('👏')">👏</button>
                        <button type="button" onclick="insertEmoji('❤️')">❤️</button>
                        <button type="button" onclick="insertEmoji('🎉')">🎉</button>
                        <button type="button" onclick="insertEmoji('🔥')">🔥</button>
                    </div>
                    
                    <textarea id="content" name="content" class="form-control" rows="4" required></textarea>
                </div>

                <div class="form-group">
                    <label>图片附件（可选，支持 PNG/JPG/GIF，最大 2MB）</label>
                    <div class="file-input-wrapper">
                        <input type="file" id="imageInput" name="image" accept="image/png,image/jpeg,image/gif">
                        <label for="imageInput" class="file-input-label">
                            📎 选择图片
                        </label>
                    </div>
                    <div id="imagePreview" class="image-preview"></div>
                </div>

                <button type="submit" class="btn btn-primary">发表留言</button>
            </form>
        </div>

        <div class="message-list">
            <h2>留言列表</h2>
            <div id="messageList">
                <div class="card">
                    <p>加载中...</p>
                </div>
            </div>
        </div>
    </div>

    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
