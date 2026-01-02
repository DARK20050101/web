<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>留言板诊断工具</title>
    <style>
        body {
            font-family: 'Segoe UI', Arial, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background: #f5f7fa;
        }
        .diagnostic-section {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 {
            color: #667eea;
        }
        h2 {
            color: #764ba2;
            border-bottom: 2px solid #667eea;
            padding-bottom: 10px;
        }
        .status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 4px;
        }
        .status.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .status.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .status.info {
            background: #d1ecf1;
            color: #0c5460;
            border: 1px solid #bee5eb;
        }
        button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
            margin: 5px;
        }
        button:hover {
            opacity: 0.9;
        }
        pre {
            background: #f4f4f4;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            border-left: 4px solid #667eea;
        }
        .test-result {
            margin: 10px 0;
            padding: 10px;
            border-left: 4px solid #ccc;
        }
        .pass {
            border-left-color: #28a745;
            background: #d4edda;
        }
        .fail {
            border-left-color: #dc3545;
            background: #f8d7da;
        }
    </style>
</head>
<body>
    <h1>🔍 留言板系统诊断工具</h1>
    
    <div class="diagnostic-section">
        <h2>1. 服务器环境检查</h2>
        <div class="status info">
            <strong>JSP 引擎:</strong> <%= application.getServerInfo() %><br>
            <strong>Java 版本:</strong> <%= System.getProperty("java.version") %><br>
            <strong>系统编码:</strong> <%= System.getProperty("file.encoding") %><br>
            <strong>应用路径:</strong> <%= application.getRealPath("/") %>
        </div>
    </div>
    
    <div class="diagnostic-section">
        <h2>2. 数据库连接测试</h2>
        <button onclick="testDatabaseConnection()">测试数据库连接</button>
        <div id="dbResult"></div>
    </div>
    
    <div class="diagnostic-section">
        <h2>3. API 接口测试</h2>
        <button onclick="testMessageAPI()">测试留言列表API</button>
        <button onclick="testDetailedAPI()">详细API测试</button>
        <div id="apiResult"></div>
    </div>
    
    <div class="diagnostic-section">
        <h2>4. 前端JavaScript测试</h2>
        <button onclick="testJavaScript()">测试JavaScript环境</button>
        <div id="jsResult"></div>
    </div>
    
    <div class="diagnostic-section">
        <h2>5. 完整诊断报告</h2>
        <button onclick="runFullDiagnostic()">运行完整诊断</button>
        <div id="fullResult"></div>
    </div>
    
    <script>
        // Test database connection
        function testDatabaseConnection() {
            const resultDiv = document.getElementById('dbResult');
            resultDiv.innerHTML = '<div class="status info">正在测试数据库连接...</div>';
            
            fetch('diagnostic-db')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        resultDiv.innerHTML = `
                            <div class="status success">
                                <strong>✅ 数据库连接成功</strong><br>
                                用户数: ${data.userCount}<br>
                                留言数: ${data.messageCount}
                            </div>
                        `;
                    } else {
                        resultDiv.innerHTML = `
                            <div class="status error">
                                <strong>❌ 数据库连接失败</strong><br>
                                错误: ${data.error}
                            </div>
                        `;
                    }
                })
                .catch(error => {
                    resultDiv.innerHTML = `
                        <div class="status error">
                            <strong>❌ 请求失败</strong><br>
                            错误: ${error.message}
                        </div>
                    `;
                });
        }
        
        // Test message API
        function testMessageAPI() {
            const resultDiv = document.getElementById('apiResult');
            resultDiv.innerHTML = '<div class="status info">正在测试API...</div>';
            
            fetch('message?action=list&page=1')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        resultDiv.innerHTML = `
                            <div class="status success">
                                <strong>✅ API 响应成功</strong><br>
                                留言数量: ${data.messages.length}<br>
                                总页数: ${data.totalPages}<br>
                                总留言数: ${data.totalCount}
                            </div>
                            <pre>${JSON.stringify(data, null, 2)}</pre>
                        `;
                    } else {
                        resultDiv.innerHTML = `
                            <div class="status error">
                                <strong>❌ API 返回错误</strong><br>
                                ${data.message || '未知错误'}
                            </div>
                        `;
                    }
                })
                .catch(error => {
                    resultDiv.innerHTML = `
                        <div class="status error">
                            <strong>❌ API 请求失败</strong><br>
                            错误: ${error.message}
                        </div>
                    `;
                });
        }
        
        // Detailed API test
        function testDetailedAPI() {
            const resultDiv = document.getElementById('apiResult');
            resultDiv.innerHTML = '<div class="status info">正在进行详细测试...</div>';
            
            fetch('message?action=list&page=1')
                .then(response => {
                    console.log('Response status:', response.status);
                    console.log('Response headers:', response.headers);
                    return response.text();
                })
                .then(text => {
                    console.log('Raw response:', text);
                    try {
                        const data = JSON.parse(text);
                        let html = `<div class="status success"><strong>✅ JSON 解析成功</strong></div>`;
                        
                        if (data.success && data.messages && data.messages.length > 0) {
                            html += '<h3>留言数据结构分析:</h3>';
                            const msg = data.messages[0];
                            html += '<div class="test-result pass">';
                            html += '<strong>第一条留言的字段:</strong><br>';
                            html += '<pre>' + JSON.stringify(msg, null, 2) + '</pre>';
                            html += '<strong>字段检查:</strong><ul>';
                            html += `<li>id: ${msg.id !== undefined ? '✅' : '❌'}</li>`;
                            html += `<li>nickname: ${msg.nickname !== undefined ? '✅' : '❌'}</li>`;
                            html += `<li>content: ${msg.content !== undefined ? '✅' : '❌'}</li>`;
                            html += `<li>anonymous: ${msg.anonymous !== undefined ? '✅' : '❌'}</li>`;
                            html += `<li>createdAt: ${msg.createdAt !== undefined ? '✅' : '❌'}</li>`;
                            html += '</ul></div>';
                        } else {
                            html += '<div class="test-result fail">❌ 没有留言数据</div>';
                        }
                        
                        html += '<h3>完整响应:</h3>';
                        html += '<pre>' + text + '</pre>';
                        
                        resultDiv.innerHTML = html;
                    } catch (e) {
                        resultDiv.innerHTML = `
                            <div class="status error">
                                <strong>❌ JSON 解析失败</strong><br>
                                错误: ${e.message}<br>
                                <strong>原始响应:</strong>
                                <pre>${text}</pre>
                            </div>
                        `;
                    }
                })
                .catch(error => {
                    resultDiv.innerHTML = `
                        <div class="status error">
                            <strong>❌ 请求失败</strong><br>
                            ${error.message}
                        </div>
                    `;
                });
        }
        
        // Test JavaScript environment
        function testJavaScript() {
            const resultDiv = document.getElementById('jsResult');
            let html = '<h3>JavaScript 环境检测:</h3>';
            
            const tests = [
                { name: 'Fetch API', test: () => typeof fetch !== 'undefined' },
                { name: 'Promise', test: () => typeof Promise !== 'undefined' },
                { name: 'Array.map', test: () => typeof Array.prototype.map !== 'undefined' },
                { name: 'JSON', test: () => typeof JSON !== 'undefined' },
                { name: 'localStorage', test: () => typeof localStorage !== 'undefined' }
            ];
            
            tests.forEach(t => {
                const passed = t.test();
                html += `<div class="test-result ${passed ? 'pass' : 'fail'}">
                    ${passed ? '✅' : '❌'} ${t.name}
                </div>`;
            });
            
            resultDiv.innerHTML = html;
        }
        
        // Run full diagnostic
        function runFullDiagnostic() {
            const resultDiv = document.getElementById('fullResult');
            resultDiv.innerHTML = '<div class="status info">正在运行完整诊断，请稍候...</div>';
            
            let report = '<h3>完整诊断报告</h3>';
            
            // Test 1: Database
            fetch('diagnostic-db')
                .then(response => response.json())
                .then(dbData => {
                    report += '<div class="test-result ' + (dbData.success ? 'pass' : 'fail') + '">';
                    report += '<strong>数据库连接:</strong> ' + (dbData.success ? '✅ 成功' : '❌ 失败') + '<br>';
                    if (dbData.success) {
                        report += `用户数: ${dbData.userCount}, 留言数: ${dbData.messageCount}`;
                    } else {
                        report += `错误: ${dbData.error}`;
                    }
                    report += '</div>';
                    
                    // Test 2: API
                    return fetch('message?action=list&page=1');
                })
                .then(response => response.json())
                .then(apiData => {
                    report += '<div class="test-result ' + (apiData.success ? 'pass' : 'fail') + '">';
                    report += '<strong>API 接口:</strong> ' + (apiData.success ? '✅ 正常' : '❌ 异常') + '<br>';
                    if (apiData.success) {
                        report += `返回 ${apiData.messages.length} 条留言，共 ${apiData.totalCount} 条`;
                        if (apiData.messages.length > 0) {
                            const msg = apiData.messages[0];
                            report += '<br><strong>数据示例:</strong><pre>' + JSON.stringify(msg, null, 2) + '</pre>';
                        }
                    }
                    report += '</div>';
                    
                    // Test 3: JavaScript
                    report += '<div class="test-result pass">';
                    report += '<strong>JavaScript 环境:</strong> ✅ 正常<br>';
                    report += 'Fetch API, Promise, JSON 等功能可用';
                    report += '</div>';
                    
                    // Summary
                    report += '<h3>📊 诊断总结</h3>';
                    report += '<div class="status success">';
                    report += '<strong>如果所有测试都通过:</strong><br>';
                    report += '系统配置正确，如果页面仍不显示留言，请检查:<br>';
                    report += '1. 浏览器控制台(F12)是否有JavaScript错误<br>';
                    report += '2. 网络标签是否显示API请求成功<br>';
                    report += '3. 页面HTML元素 id="messageList" 是否存在';
                    report += '</div>';
                    
                    resultDiv.innerHTML = report;
                })
                .catch(error => {
                    report += '<div class="status error">';
                    report += '<strong>❌ 诊断过程出错:</strong><br>';
                    report += error.message;
                    report += '</div>';
                    resultDiv.innerHTML = report;
                });
        }
    </script>
</body>
</html>
