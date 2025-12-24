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
    <div class="container">
        <div class="card" style="text-align: center; margin-top: 100px;">
            <h1 style="color: #dc3545; font-size: 72px; margin-bottom: 20px;">😕</h1>
            <h2>出错了</h2>
            <p style="color: #666; margin: 20px 0;">抱歉，页面出现了一些问题。</p>
            <a href="<%= request.getContextPath() %>/" class="btn btn-primary">返回首页</a>
        </div>
    </div>
</body>
</html>
