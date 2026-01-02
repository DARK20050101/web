<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 留言板系统</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="form-container">
            <h2>用户登录</h2>
            
            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            
            <% if (request.getAttribute("success") != null) { %>
                <div class="alert alert-success">
                    <%= request.getAttribute("success") %>
                </div>
            <% } %>
            
            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="form-group">
                    <label for="username">用户名：</label>
                    <input type="text" id="username" name="username" required 
                           value="<%= request.getCookies() != null ? getCookieValue(request.getCookies(), "username") : "" %>">
                </div>
                
                <div class="form-group">
                    <label for="password">密码：</label>
                    <input type="password" id="password" name="password" required>
                </div>
                
                <div class="form-group">
                    <label for="captcha">验证码：</label>
                    <div class="captcha-container">
                        <input type="text" id="captcha" name="captcha" required>
                        <img id="captchaImg" src="${pageContext.request.contextPath}/captcha" 
                             alt="验证码" onclick="refreshCaptcha()">
                    </div>
                </div>
                
                <div class="form-group">
                    <label>
                        <input type="checkbox" name="rememberMe"> 记住我
                    </label>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary">登录</button>
                </div>
                
                <div class="form-footer">
                    还没有账号？ <a href="${pageContext.request.contextPath}/register.jsp">立即注册</a>
                    <br>
                    <a href="${pageContext.request.contextPath}/index.jsp">返回首页</a>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        function refreshCaptcha() {
            document.getElementById('captchaImg').src = 
                '${pageContext.request.contextPath}/captcha?t=' + new Date().getTime();
        }
    </script>
</body>
</html>

<%!
    private String getCookieValue(Cookie[] cookies, String name) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return "";
    }
%>
