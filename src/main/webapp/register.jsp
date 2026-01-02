<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 留言板系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="form-container">
            <h2>用户注册</h2>
            
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            
            <form action="${pageContext.request.contextPath}/register" method="post">
                <div class="form-group">
                    <label for="username">用户名：</label>
                    <input type="text" id="username" name="username" required 
                           pattern="[a-zA-Z0-9_]{3,20}" title="用户名只能包含字母、数字和下划线，长度3-20位">
                </div>
                
                <div class="form-group">
                    <label for="email">邮箱：</label>
                    <input type="email" id="email" name="email" required>
                </div>
                
                <div class="form-group">
                    <label for="password">密码：</label>
                    <input type="password" id="password" name="password" required 
                           minlength="6" title="密码长度至少6位">
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">确认密码：</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">注册</button>
                </div>
                
                <div class="form-footer">
                    已有账号？ <a href="${pageContext.request.contextPath}/login.jsp">立即登录</a>
                    <br>
                    <a href="${pageContext.request.contextPath}/index.jsp">返回首页</a>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        document.querySelector('form').addEventListener('submit', function(e) {
            var password = document.getElementById('password').value;
            var confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('两次输入的密码不一致！');
            }
        });
    </script>
</body>
</html>
