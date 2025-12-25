<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>错误 - 在线留言板</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <h1>❌ 出错了</h1>
            <div style="margin: 20px 0;">
                <p style="color: #666;">抱歉，发生了一些错误。</p>
                <% if (exception != null) { %>
                    <p style="color: #999; font-size: 12px; margin-top: 10px;">
                        错误信息: <%= exception.getMessage() %>
                    </p>
                <% } %>
            </div>
            <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-primary" style="width: 100%;">返回首页</a>
        </div>
    </div>
</body>
</html>
