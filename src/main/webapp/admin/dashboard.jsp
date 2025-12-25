<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
    if (isAdmin == null || !isAdmin) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String username = (String) session.getAttribute("username");
    String csrfToken = (String) session.getAttribute("csrfToken");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理后台 - 留言板系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="header">
        <div class="container">
            <h1>管理后台</h1>
            <div class="user-info">
                欢迎, <strong><%= username %></strong> (管理员)
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-small">返回首页</a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-small">退出</a>
            </div>
        </div>
    </div>
    
    <div class="container main-content">
        <div class="admin-dashboard">
            <div class="tabs">
                <button class="tab-button active" onclick="showTab('users')">用户管理</button>
                <button class="tab-button" onclick="showTab('messages')">留言管理</button>
            </div>
            
            <!-- Users Management -->
            <div id="usersTab" class="tab-content active">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h3 style="margin: 0;">用户管理</h3>
                    <button class="btn btn-primary" onclick="showCreateUserModal()">➕ 添加用户</button>
                </div>
                <table id="usersTable" class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>用户名</th>
                            <th>邮箱</th>
                            <th>角色</th>
                            <th>注册时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody id="usersTableBody">
                        <tr><td colspan="6">加载中...</td></tr>
                    </tbody>
                </table>
            </div>
            
            <!-- Messages Management -->
            <div id="messagesTab" class="tab-content">
                <h3>留言管理</h3>
                <table id="messagesTable" class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>昵称/用户名</th>
                            <th>内容</th>
                            <th>类型</th>
                            <th>发布时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody id="messagesTableBody">
                        <tr><td colspan="6">加载中...</td></tr>
                    </tbody>
                </table>
                <div id="messagePagination"></div>
            </div>
        </div>
    </div>
    
    <!-- Create User Modal -->
    <div id="createUserModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeCreateUserModal()">&times;</span>
            <h3>添加新用户</h3>
            <form id="createUserForm">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="type" value="user">
                
                <div class="form-group">
                    <label for="createUsername">用户名：</label>
                    <input type="text" id="createUsername" name="username" required 
                           pattern="[a-zA-Z0-9_]{3,20}" title="用户名只能包含字母、数字和下划线，长度3-20位">
                </div>
                
                <div class="form-group">
                    <label for="createPassword">密码：</label>
                    <input type="password" id="createPassword" name="password" required 
                           minlength="6" title="密码长度至少6位">
                </div>
                
                <div class="form-group">
                    <label for="createEmail">邮箱：</label>
                    <input type="email" id="createEmail" name="email" required>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" id="createIsAdmin" name="isAdmin" value="true">
                        设为管理员
                    </label>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">创建</button>
                    <button type="button" class="btn" onclick="closeCreateUserModal()">取消</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Edit User Modal -->
    <div id="editUserModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditUserModal()">&times;</span>
            <h3>编辑用户</h3>
            <form id="editUserForm">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="type" value="user">
                <input type="hidden" id="editUserId" name="id">
                
                <div class="form-group">
                    <label for="editUsername">用户名：</label>
                    <input type="text" id="editUsername" name="username" required>
                </div>
                
                <div class="form-group">
                    <label for="editEmail">邮箱：</label>
                    <input type="email" id="editEmail" name="email" required>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" id="editIsAdmin" name="isAdmin" value="true">
                        管理员权限
                    </label>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <button type="button" class="btn" onclick="closeEditUserModal()">取消</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Edit Message Modal -->
    <div id="editMessageModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeEditMessageModal()">&times;</span>
            <h3>编辑留言</h3>
            <form id="editMessageForm">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="type" value="message">
                <input type="hidden" id="editMessageId" name="id">
                
                <div class="form-group">
                    <label for="editMessageContent">留言内容：</label>
                    <textarea id="editMessageContent" name="content" rows="5" required></textarea>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">保存</button>
                    <button type="button" class="btn" onclick="closeEditMessageModal()">取消</button>
                </div>
            </form>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
</body>
</html>
