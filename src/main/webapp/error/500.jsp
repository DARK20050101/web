<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>500 - 服务器错误</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            text-align: center;
            padding: 100px 20px;
        }
        .error-code {
            font-size: 100px;
            color: #e74c3c;
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
        <h1 class="error-code">500</h1>
        <p class="error-message">抱歉，服务器发生错误</p>
        <p>请稍后再试或联系管理员</p>
        <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-primary">返回首页</a>
    </div>
</body>
</html>
