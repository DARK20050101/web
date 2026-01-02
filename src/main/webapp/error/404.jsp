<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>404 - 页面未找到</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            text-align: center;
            padding: 100px 20px;
        }
        .error-code {
            font-size: 100px;
            color: #667eea;
            margin: 0;
        }
        .error-message {
            font-size: 24px;
            color: #666;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    <div class="container error-container">
        <h1 class="error-code">404</h1>
        <p class="error-message">抱歉，您访问的页面不存在</p>
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
