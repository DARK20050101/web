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
                <h3>用户管理</h3>
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
    
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
</body>
</html>
