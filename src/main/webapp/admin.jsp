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
                    <h3 style="margin: 0; color: #667eea;">用户总数</h3>
                    <p style="font-size: 2em; margin: 10px 0; font-weight: bold;" id="totalUsers">-</p>
                </div>
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                    <h3 style="margin: 0; color: #667eea;">系统状态</h3>
                    <p style="font-size: 1.5em; margin: 10px 0; color: #10b981; font-weight: bold;">运行中</p>
                </div>
            </div>
        </div>

        <!-- Message Search -->
        <div class="message-form">
            <h2>🔍 留言搜索</h2>
            <div style="display: flex; gap: 10px; margin-bottom: 20px;">
                <input type="text" 
                       id="searchInput" 
                       placeholder="搜索留言内容或作者..." 
                       style="flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 5px;">
                <button onclick="searchMessages()" class="btn" style="background: #667eea;">搜索</button>
                <button onclick="clearSearch()" class="btn btn-secondary">清除</button>
            </div>
            <div id="searchResults"></div>
        </div>

        <!-- User Management -->
        <div class="message-form">
            <h2>👥 用户管理</h2>
            <button onclick="showCreateUserForm()" class="btn" style="background: #10b981; margin-bottom: 20px;">+ 创建新用户</button>
            
            <!-- Create User Form (hidden by default) -->
            <div id="createUserForm" style="display: none; background: #f8f9fa; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                <h3>创建新用户</h3>
                <form onsubmit="createUser(event)">
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px;">用户名:</label>
                        <input type="text" id="newUsername" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px;">密码:</label>
                        <input type="password" id="newPassword" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; margin-bottom: 5px;">邮箱:</label>
                        <input type="email" id="newEmail" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 5px;">
                    </div>
                    <div style="margin-bottom: 15px;">
                        <label>
                            <input type="checkbox" id="newIsAdmin"> 设为管理员
                        </label>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button type="submit" class="btn" style="background: #10b981;">创建</button>
                        <button type="button" onclick="hideCreateUserForm()" class="btn btn-secondary">取消</button>
                    </div>
                </form>
            </div>
            
            <div id="userList">
                <p style="text-align: center; color: #999;">Loading users...</p>
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
            loadAllData();
        });
        
        function loadAllData() {
            loadMessages();
            loadUsers();
            loadStats();
        }
        
        function loadMessages() {
            fetch(contextPath + '/api/messages')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('totalMessages').textContent = data.messages.length;
                        renderMessages(data.messages);
                    }
                });
        }
        
        function loadUsers() {
            fetch(contextPath + '/api/admin/users', {
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        renderUsers(data.users);
                    } else {
                        document.getElementById('userList').innerHTML = 
                            '<p style="text-align: center; color: #f44336;">Error: ' + data.error + '</p>';
                    }
                })
                .catch(error => {
                    console.error('Error loading users:', error);
                    document.getElementById('userList').innerHTML = 
                        '<p style="text-align: center; color: #f44336;">Failed to load users</p>';
                });
        }
        
        function loadStats() {
            fetch(contextPath + '/api/admin/stats', {
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('totalUsers').textContent = data.userCount;
                    }
                })
                .catch(error => console.error('Error loading stats:', error));
        }
        
        function renderMessages(messages) {
            const messageList = document.getElementById('messageList');
            
            if (!messageList) return;
            
            if (messages.length === 0) {
                messageList.innerHTML = '<p style="text-align: center; color: #999;">No messages found.</p>';
                return;
            }
            
            messageList.innerHTML = messages.map(message => {
                const date = new Date(message.createdAt);
                const formattedDate = date.toLocaleString();
                
                let imageHtml = '';
                if (message.imagePath) {
                    imageHtml = '<img src="' + contextPath + '/image/' + message.imagePath + '" alt="Attached image" class="message-image" style="max-width: 200px; border-radius: 5px; margin-top: 10px;">';
                }
                
                return '<div class="message-item" style="background: white; padding: 15px; margin-bottom: 15px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">' +
                    '<div class="message-header" style="display: flex; justify-content: space-between; margin-bottom: 10px;">' +
                        '<span class="message-author" style="font-weight: bold;">' + escapeHtml(message.author) + '</span>' +
                        '<div>' +
                            '<span class="message-time" style="color: #999; margin-right: 10px;">' + formattedDate + '</span>' +
                            '<button class="delete-btn" onclick="deleteMessage(' + message.id + ')" style="background: #f44336; color: white; border: none; padding: 5px 10px; border-radius: 5px; cursor: pointer;">删除</button>' +
                        '</div>' +
                    '</div>' +
                    '<div class="message-content" style="margin-bottom: 10px;">' + escapeHtml(message.content) + '</div>' +
                    imageHtml +
                '</div>';
            }).join('');
        }
        
        function renderUsers(users) {
            const userList = document.getElementById('userList');
            
            if (!userList) return;
            
            if (users.length === 0) {
                userList.innerHTML = '<p style="text-align: center; color: #999;">No users found.</p>';
                return;
            }
            
            userList.innerHTML = '<table style="width: 100%; border-collapse: collapse;">' +
                '<thead>' +
                    '<tr style="background: #f8f9fa;">' +
                        '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">用户名</th>' +
                        '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">邮箱</th>' +
                        '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">角色</th>' +
                        '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">创建时间</th>' +
                        '<th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">操作</th>' +
                    '</tr>' +
                '</thead>' +
                '<tbody>' +
                    users.map(user => {
                        const date = new Date(user.createdAt);
                        const formattedDate = date.toLocaleDateString();
                        const roleClass = user.isAdmin ? 'admin-badge' : '';
                        const roleText = user.isAdmin ? '管理员' : '用户';
                        
                        return '<tr style="border-bottom: 1px solid #eee;">' +
                            '<td style="padding: 10px;">' + escapeHtml(user.username) + '</td>' +
                            '<td style="padding: 10px;">' + (user.email ? escapeHtml(user.email) : '-') + '</td>' +
                            '<td style="padding: 10px;"><span class="' + roleClass + '">' + roleText + '</span></td>' +
                            '<td style="padding: 10px;">' + formattedDate + '</td>' +
                            '<td style="padding: 10px;">' +
                                '<button onclick="toggleUserAdmin(' + user.id + ', ' + !user.isAdmin + ')" class="btn btn-secondary" style="padding: 5px 10px; margin-right: 5px;">' +
                                    (user.isAdmin ? '取消管理员' : '设为管理员') +
                                '</button>' +
                                (user.username !== window.currentUser.username ? 
                                    '<button onclick="deleteUser(' + user.id + ')" class="btn" style="background: #f44336; padding: 5px 10px;">删除</button>' 
                                    : '') +
                            '</td>' +
                        '</tr>';
                    }).join('') +
                '</tbody>' +
            '</table>';
        }
        
        function searchMessages() {
            const keyword = document.getElementById('searchInput').value.trim();
            
            if (!keyword) {
                alert('请输入搜索关键词');
                return;
            }
            
            fetch(contextPath + '/api/messages?search=' + encodeURIComponent(keyword))
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        const searchResults = document.getElementById('searchResults');
                        if (data.messages.length === 0) {
                            searchResults.innerHTML = '<p style="color: #999;">未找到匹配的留言</p>';
                        } else {
                            searchResults.innerHTML = '<p style="color: #10b981; margin-bottom: 10px;">找到 ' + data.messages.length + ' 条匹配的留言</p>' +
                                '<div>' + data.messages.map(message => {
                                    const date = new Date(message.createdAt);
                                    const formattedDate = date.toLocaleString();
                                    return '<div style="background: white; padding: 15px; margin-bottom: 10px; border-radius: 10px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">' +
                                        '<div style="display: flex; justify-content: space-between; margin-bottom: 10px;">' +
                                            '<span style="font-weight: bold;">' + escapeHtml(message.author) + '</span>' +
                                            '<span style="color: #999;">' + formattedDate + '</span>' +
                                        '</div>' +
                                        '<div>' + escapeHtml(message.content) + '</div>' +
                                    '</div>';
                                }).join('') + '</div>';
                        }
                    } else {
                        alert('搜索失败: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('搜索失败');
                });
        }
        
        function clearSearch() {
            document.getElementById('searchInput').value = '';
            document.getElementById('searchResults').innerHTML = '';
        }
        
        function showCreateUserForm() {
            document.getElementById('createUserForm').style.display = 'block';
        }
        
        function hideCreateUserForm() {
            document.getElementById('createUserForm').style.display = 'none';
            document.getElementById('newUsername').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('newEmail').value = '';
            document.getElementById('newIsAdmin').checked = false;
        }
        
        function createUser(event) {
            event.preventDefault();
            
            const username = document.getElementById('newUsername').value;
            const password = document.getElementById('newPassword').value;
            const email = document.getElementById('newEmail').value;
            const isAdmin = document.getElementById('newIsAdmin').checked;
            
            const formData = new FormData();
            formData.append('username', username);
            formData.append('password', password);
            formData.append('email', email);
            formData.append('isAdmin', isAdmin);
            
            fetch(contextPath + '/api/admin/users', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('用户创建成功');
                        hideCreateUserForm();
                        loadUsers();
                        loadStats();
                    } else {
                        alert('创建失败: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('创建失败');
                });
        }
        
        function toggleUserAdmin(userId, isAdmin) {
            if (!confirm('确定要' + (isAdmin ? '设为管理员' : '取消管理员') + '吗？')) {
                return;
            }
            
            const formData = new FormData();
            formData.append('isAdmin', isAdmin);
            
            fetch(contextPath + '/api/admin/users/' + userId, {
                method: 'PUT',
                body: formData,
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('更新成功');
                        loadUsers();
                    } else {
                        alert('更新失败: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('更新失败');
                });
        }
        
        function deleteUser(userId) {
            if (!confirm('确定要删除此用户吗？此操作无法撤销。')) {
                return;
            }
            
            fetch(contextPath + '/api/admin/users/' + userId, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('用户删除成功');
                        loadUsers();
                        loadStats();
                    } else {
                        alert('删除失败: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('删除失败');
                });
        }
        
        function deleteMessage(id) {
            if (!confirm('确定要删除此留言吗？')) {
                return;
            }
            
            fetch(contextPath + '/api/messages?id=' + id, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-Token': csrfToken
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('留言删除成功');
                        loadMessages();
                    } else {
                        alert('删除失败: ' + data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('删除失败');
                });
        }
        
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
    <script src="<%= request.getContextPath() %>/js/app.js"></script>
</body>
</html>
