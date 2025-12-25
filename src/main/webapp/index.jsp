<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String username = (String) session.getAttribute("username");
    Integer userId = (Integer) session.getAttribute("userId");
    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
    String csrfToken = (String) session.getAttribute("csrfToken");
    if (csrfToken == null) {
        csrfToken = com.messageboard.util.SecurityUtil.generateCSRFToken();
        session.setAttribute("csrfToken", csrfToken);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>留言板系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script>
        // Pass session data to JavaScript
        const currentUserId = <%= userId != null ? userId : "null" %>;
        const currentIsAdmin = <%= isAdmin != null && isAdmin ? "true" : "false" %>;
    </script>
</head>
<body>
    <div class="header">
        <div class="container">
            <h1>在线留言板系统</h1>
            <div class="user-info">
                <% if (username != null) { %>
                    欢迎, <strong><%= username %></strong>
                    <% if (isAdmin != null && isAdmin) { %>
                        <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="btn btn-small">管理后台</a>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-small">退出</a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-small">登录</a>
                    <a href="${pageContext.request.contextPath}/register.jsp" class="btn btn-small">注册</a>
                <% } %>
            </div>
        </div>
    </div>
    
    <div class="container main-content">
        <!-- Message Form -->
        <div class="message-form-container">
            <h3>发表留言</h3>
            <form id="messageForm" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="create">
                
                <% if (username == null) { %>
                    <div class="form-group">
                        <label for="nickname">昵称：</label>
                        <input type="text" id="nickname" name="nickname" required>
                    </div>
                    
                    <div class="form-group">
                        <label for="captcha">验证码：</label>
                        <div class="captcha-container">
                            <input type="text" id="captcha" name="captcha" required>
                            <img id="captchaImg" src="${pageContext.request.contextPath}/captcha" 
                                 alt="验证码" onclick="refreshCaptcha()">
                        </div>
                    </div>
                <% } %>
                
                <div class="form-group">
                    <label for="content">留言内容：</label>
                    <textarea id="content" name="content" rows="5" required></textarea>
                </div>
                
                <div class="form-group">
                    <label for="image">上传图片/表情：</label>
                    <input type="file" id="image" name="image" accept="image/*">
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">发表留言</button>
                </div>
            </form>
            <div id="messageAlert"></div>
        </div>
        
        <!-- Message List -->
        <div class="message-list-container">
            <h3>留言列表 <span id="totalCount"></span></h3>
            <div id="messageList"></div>
            <div id="pagination"></div>
        </div>
    </div>
    
    <!-- Edit Message Modal -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditModal()">&times;</span>
            <h3>编辑留言</h3>
            <form id="editForm" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editMessageId" name="id">
                
                <div class="form-group">
                    <label for="editContent">留言内容：</label>
                    <textarea id="editContent" name="content" rows="5" required></textarea>
                </div>
                
                <div class="form-group">
                    <label for="editImage">更换图片/表情：</label>
                    <input type="file" id="editImage" name="image" accept="image/*">
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <button type="button" class="btn" onclick="closeEditModal()">取消</button>
                </div>
            </form>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/js/messageboard.js"></script>
</body>
</html>
