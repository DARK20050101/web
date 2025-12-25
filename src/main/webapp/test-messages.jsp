<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.messageboard.dao.MessageDAO" %>
<%@ page import="com.messageboard.model.Message" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>留言数据测试</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1000px;
            margin: 20px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .box {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 5px;
            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        }
        h1 { color: #667eea; }
        h2 { color: #764ba2; }
        .message {
            border-left: 4px solid #667eea;
            padding: 10px;
            margin: 10px 0;
            background: #f9f9f9;
        }
        .success { color: #28a745; }
        .error { color: #dc3545; }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
        }
        th {
            background: #667eea;
            color: white;
        }
        pre {
            background: #f4f4f4;
            padding: 10px;
            border-radius: 3px;
            overflow-x: auto;
        }
    </style>
</head>
<body>
    <h1>🔍 留言数据测试页面</h1>
    
    <div class="box">
        <h2>1. 直接从数据库读取留言</h2>
        <%
            try {
                MessageDAO messageDAO = new MessageDAO();
                List<Message> messages = messageDAO.findAll(1, 10);
                int totalCount = messageDAO.getTotalCount();
        %>
                <p class="success">✅ 成功从数据库读取数据</p>
                <p><strong>留言总数:</strong> <%= totalCount %></p>
                <p><strong>当前页留言数:</strong> <%= messages.size() %></p>
                
                <h3>留言列表：</h3>
                <% if (messages.isEmpty()) { %>
                    <p class="error">❌ 数据库中没有留言数据</p>
                    <p>请确保已执行 database_schema.sql 导入初始数据</p>
                <% } else { %>
                    <table>
                        <tr>
                            <th>ID</th>
                            <th>User ID</th>
                            <th>昵称</th>
                            <th>内容预览</th>
                            <th>匿名</th>
                            <th>时间</th>
                        </tr>
                        <% for (Message msg : messages) { %>
                        <tr>
                            <td><%= msg.getId() %></td>
                            <td><%= msg.getUserId() %></td>
                            <td><%= msg.getNickname() %></td>
                            <td><%= msg.getContent().substring(0, Math.min(30, msg.getContent().length())) %>...</td>
                            <td><%= msg.isAnonymous() ? "是" : "否" %></td>
                            <td><%= msg.getCreatedAt() %></td>
                        </tr>
                        <% } %>
                    </table>
                    
                    <h3>第一条留言的完整信息：</h3>
                    <% if (!messages.isEmpty()) {
                        Message first = messages.get(0);
                    %>
                        <pre><%= first.toString() %></pre>
                    <% } %>
                <% } %>
        <%
            } catch (Exception e) {
        %>
                <p class="error">❌ 读取数据失败</p>
                <p><strong>错误信息:</strong> <%= e.getMessage() %></p>
                <pre><%= e.getClass().getName() %>
<% 
                e.printStackTrace(new java.io.PrintWriter(out));
%></pre>
        <%
            }
        %>
    </div>
    
    <div class="box">
        <h2>2. 测试 JSON API</h2>
        <button onclick="testAPI()">点击测试 API</button>
        <div id="apiResult"></div>
    </div>
    
    <div class="box">
        <h2>3. 测试前端渲染</h2>
        <button onclick="testRender()">点击测试渲染</button>
        <div id="renderTest"></div>
    </div>
    
    <script>
        function testAPI() {
            const resultDiv = document.getElementById('apiResult');
            resultDiv.innerHTML = '<p>正在测试...</p>';
            
            fetch('message?action=list&page=1')
                .then(response => {
                    console.log('Response status:', response.status);
                    console.log('Response Content-Type:', response.headers.get('content-type'));
                    return response.text();
                })
                .then(text => {
                    console.log('Raw response:', text);
                    
                    try {
                        const data = JSON.parse(text);
                        console.log('Parsed data:', data);
                        
                        let html = '<h3 style="color: #28a745;">✅ API 响应成功</h3>';
                        html += '<p><strong>Success:</strong> ' + data.success + '</p>';
                        html += '<p><strong>Total Count:</strong> ' + data.totalCount + '</p>';
                        html += '<p><strong>Messages Length:</strong> ' + (data.messages ? data.messages.length : 0) + '</p>';
                        
                        if (data.messages && data.messages.length > 0) {
                            html += '<h4>第一条留言:</h4>';
                            html += '<pre>' + JSON.stringify(data.messages[0], null, 2) + '</pre>';
                            
                            html += '<h4>字段检查:</h4>';
                            const msg = data.messages[0];
                            html += '<ul>';
                            html += '<li>id: ' + (msg.id !== undefined ? '✅ ' + msg.id : '❌ 缺失') + '</li>';
                            html += '<li>nickname: ' + (msg.nickname !== undefined ? '✅ ' + msg.nickname : '❌ 缺失') + '</li>';
                            html += '<li>content: ' + (msg.content !== undefined ? '✅ 存在' : '❌ 缺失') + '</li>';
                            html += '<li>anonymous: ' + (msg.anonymous !== undefined ? '✅ ' + msg.anonymous : '❌ 缺失') + '</li>';
                            html += '<li>createdAt: ' + (msg.createdAt !== undefined ? '✅ ' + msg.createdAt : '❌ 缺失') + '</li>';
                            html += '</ul>';
                        }
                        
                        html += '<h4>完整 JSON 响应:</h4>';
                        html += '<pre>' + text + '</pre>';
                        
                        resultDiv.innerHTML = html;
                    } catch (e) {
                        resultDiv.innerHTML = '<h3 style="color: #dc3545;">❌ JSON 解析失败</h3>' +
                            '<p><strong>错误:</strong> ' + e.message + '</p>' +
                            '<p><strong>原始响应:</strong></p>' +
                            '<pre>' + text + '</pre>';
                    }
                })
                .catch(error => {
                    resultDiv.innerHTML = '<h3 style="color: #dc3545;">❌ 请求失败</h3>' +
                        '<p>' + error.message + '</p>';
                });
        }
        
        function testRender() {
            const resultDiv = document.getElementById('renderTest');
            resultDiv.innerHTML = '<p>正在测试...</p>';
            
            fetch('message?action=list&page=1')
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.messages) {
                        let html = '<h3 style="color: #28a745;">✅ 渲染测试</h3>';
                        html += '<div style="border: 1px solid #ddd; padding: 10px;">';
                        
                        data.messages.forEach(message => {
                            const date = new Date(message.createdAt).toLocaleString('zh-CN');
                            const anonymousBadge = message.anonymous ? '<span style="background:#ff6b6b;color:white;padding:2px 8px;border-radius:3px;font-size:12px;">匿名</span>' : '';
                            
                            html += `
                                <div style="border-left: 4px solid #667eea; padding: 10px; margin: 10px 0; background: #f9f9f9;">
                                    <div style="margin-bottom: 5px;">
                                        <strong>${message.nickname}</strong>
                                        ${anonymousBadge}
                                        <span style="float: right; color: #999; font-size: 14px;">${date}</span>
                                    </div>
                                    <div style="color: #333;">${message.content.substring(0, 100)}...</div>
                                </div>
                            `;
                        });
                        
                        html += '</div>';
                        resultDiv.innerHTML = html;
                    } else {
                        resultDiv.innerHTML = '<p class="error">无法渲染：数据格式错误</p>';
                    }
                })
                .catch(error => {
                    resultDiv.innerHTML = '<p class="error">渲染失败: ' + error.message + '</p>';
                });
        }
    </script>
</body>
</html>
