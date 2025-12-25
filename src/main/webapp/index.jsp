<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.model.User" %>
<%@ page import="com.messageboard.util.SecurityUtil" %>
<%
    User user = (User) session.getAttribute("user");
    String username = user != null ? user.getUsername() : null;
    boolean isLoggedIn = user != null;
    boolean isAdmin = user != null && user.isAdmin();
    
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
        img-src 'self' data: blob:;
        font-src 'self';
        connect-src 'self';
        frame-ancestors 'none';
        base-uri 'self';
        form-action 'self';
    ">
    <title>在线留言板 - Message Board</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="container">
        <!-- Header -->
        <div class="header">
            <h1>📝 在线留言板</h1>
            <div class="user-info">
                <% if (isLoggedIn) { %>
                    <span>Welcome, <strong><%= SecurityUtil.escapeHtml(username) %></strong>
                        <% if (isAdmin) { %>
                            <span class="admin-badge">ADMIN</span>
                        <% } %>
                    </span>
                    <div>
                        <% if (isAdmin) { %>
                            <a href="<%= request.getContextPath() %>/admin.jsp" class="btn btn-secondary">管理面板</a>
                        <% } %>
                        <button onclick="logout()" class="btn btn-secondary">退出登录</button>
                    </div>
                <% } else { %>
                    <span>欢迎访问！</span>
                    <a href="<%= request.getContextPath() %>/login.jsp" class="btn btn-primary">登录</a>
                <% } %>
            </div>
        </div>

        <!-- Message Form -->
        <div class="message-form">
            <h2>💬 发表留言</h2>
            
            <form id="messageForm" onsubmit="submitMessage(event)" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                
                <% if (!isLoggedIn) { %>
                <div class="form-group">
                    <label for="author">昵称 *</label>
                    <input type="text" id="author" name="author" class="form-control" 
                           placeholder="请输入您的昵称" required maxlength="100">
                </div>
                <% } %>
                
                <!-- Emoji Picker -->
                <div class="emoji-picker">
                    <h4>😊 快速插入表情</h4>
                    <div class="emoji-grid">
                        <button type="button" class="emoji-btn">😀</button>
                        <button type="button" class="emoji-btn">😃</button>
                        <button type="button" class="emoji-btn">😄</button>
                        <button type="button" class="emoji-btn">😁</button>
                        <button type="button" class="emoji-btn">😊</button>
                        <button type="button" class="emoji-btn">😍</button>
                        <button type="button" class="emoji-btn">🥰</button>
                        <button type="button" class="emoji-btn">😘</button>
                        <button type="button" class="emoji-btn">😎</button>
                        <button type="button" class="emoji-btn">🤔</button>
                        <button type="button" class="emoji-btn">🤗</button>
                        <button type="button" class="emoji-btn">😢</button>
                        <button type="button" class="emoji-btn">😭</button>
                        <button type="button" class="emoji-btn">😂</button>
                        <button type="button" class="emoji-btn">🤣</button>
                        <button type="button" class="emoji-btn">👍</button>
                        <button type="button" class="emoji-btn">👎</button>
                        <button type="button" class="emoji-btn">👏</button>
                        <button type="button" class="emoji-btn">🙏</button>
                        <button type="button" class="emoji-btn">❤️</button>
                        <button type="button" class="emoji-btn">💕</button>
                        <button type="button" class="emoji-btn">🎉</button>
                        <button type="button" class="emoji-btn">🎊</button>
                        <button type="button" class="emoji-btn">🔥</button>
                        <button type="button" class="emoji-btn">✨</button>
                        <button type="button" class="emoji-btn">⭐</button>
                        <button type="button" class="emoji-btn">🌟</button>
                        <button type="button" class="emoji-btn">💯</button>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="content">留言内容 *</label>
                    <textarea id="content" name="content" class="form-control" 
                              placeholder="请输入留言内容..." required maxlength="1000"></textarea>
                </div>
                
                <div class="form-group">
                    <label for="image">📷 上传图片 (可选，最大2MB，支持 PNG/JPEG/GIF)</label>
                    <div class="image-upload">
                        <div class="file-input-wrapper">
                            <input type="file" id="image" name="image" class="form-control" 
                                   accept="image/png,image/jpeg,image/jpg,image/gif">
                        </div>
                        <img id="imagePreview" class="image-preview" alt="Preview">
                    </div>
                </div>
                
                <% if (!isLoggedIn) { %>
                <div id="captchaContainer" class="form-group">
                    <label for="captcha">验证码 *</label>
                    <p id="captchaQuestion" style="font-weight: bold; color: #667eea;"></p>
                    <input type="text" id="captcha" name="captcha" class="form-control" 
                           placeholder="请输入答案" required>
                </div>
                <% } %>
                
                <button type="submit" class="btn btn-primary">📤 发表留言</button>
            </form>
        </div>

        <!-- Message List -->
        <div class="message-list">
            <h2>📬 留言列表</h2>
            <div id="messageList">
                <p style="text-align: center; color: #999;">Loading messages...</p>
            </div>
        </div>
    </div>

    <script>
        // Set current user for JavaScript
        window.currentUser = <%= isLoggedIn ? "{\"username\":\"" + SecurityUtil.escapeHtml(username) + "\",\"isAdmin\":" + isAdmin + "}" : "null" %>;
        // Set CSRF token
        csrfToken = '<%= csrfToken %>';
    </script>
    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
