// Message Board JavaScript
let currentPage = 1;
const pageSize = 10;

// Load messages on page load
document.addEventListener('DOMContentLoaded', function() {
    loadMessages(currentPage);
    
    // Setup message form submission
    document.getElementById('messageForm').addEventListener('submit', handleMessageSubmit);
    
    // Setup edit form submission
    const editForm = document.getElementById('editForm');
    if (editForm) {
        editForm.addEventListener('submit', handleEditSubmit);
    }
});

// Refresh captcha
function refreshCaptcha() {
    const captchaImg = document.getElementById('captchaImg');
    if (captchaImg) {
        captchaImg.src = captchaImg.src.split('?')[0] + '?t=' + new Date().getTime();
    }
}

// Load messages with pagination
function loadMessages(page) {
    fetch(`message?action=list&page=${page}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayMessages(data.messages);
                displayPagination(data.currentPage, data.totalPages);
                document.getElementById('totalCount').textContent = `(共 ${data.totalCount} 条留言)`;
            } else {
                showAlert('加载留言失败', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('加载留言时发生错误', 'error');
        });
}

// Display messages
function displayMessages(messages) {
    const messageList = document.getElementById('messageList');
    
    if (messages.length === 0) {
        messageList.innerHTML = '<p class="no-messages">暂无留言</p>';
        return;
    }
    
    messageList.innerHTML = messages.map(message => {
        const date = new Date(message.createdAt).toLocaleString('zh-CN');
        const anonymousBadge = message.anonymous ? '<span class="anonymous-badge">匿名</span>' : '';
        const imagePath = message.imagePath ? `<img src="${message.imagePath}" class="message-image" alt="图片">` : '';
        
        // Escape and preserve line breaks in content
        const content = escapeHtml(message.content).replace(/\n/g, '<br>');
        
        // Check if current user can edit/delete
        const canModify = checkPermission(message);
        const actions = canModify ? `
            <div class="message-actions">
                <button class="btn btn-small" onclick="editMessage(${message.id})">编辑</button>
                <button class="btn btn-small btn-danger" onclick="deleteMessage(${message.id})">删除</button>
            </div>
        ` : '';
        
        return `
            <div class="message-item" data-id="${message.id}">
                <div class="message-header">
                    <div>
                        <span class="message-author">${escapeHtml(message.nickname)}</span>
                        ${anonymousBadge}
                    </div>
                    <span class="message-time">${date}</span>
                </div>
                <div class="message-content">${content}</div>
                ${imagePath}
                ${actions}
            </div>
        `;
    }).join('');
}

// Display pagination
function displayPagination(currentPage, totalPages) {
    const pagination = document.getElementById('pagination');
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let html = '';
    
    // Previous button
    if (currentPage > 1) {
        html += `<button class="btn" onclick="loadMessages(${currentPage - 1})">上一页</button>`;
    }
    
    // Page numbers
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            const activeClass = i === currentPage ? 'active' : '';
            html += `<button class="btn ${activeClass}" onclick="loadMessages(${i})">${i}</button>`;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            html += '<span>...</span>';
        }
    }
    
    // Next button
    if (currentPage < totalPages) {
        html += `<button class="btn" onclick="loadMessages(${currentPage + 1})">下一页</button>`;
    }
    
    pagination.innerHTML = html;
}

// Handle message form submission
function handleMessageSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch('message', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            this.reset();
            
            // Refresh captcha if exists
            const captchaImg = document.getElementById('captchaImg');
            if (captchaImg) {
                refreshCaptcha();
            }
            
            // Reload messages
            loadMessages(1);
        } else {
            showAlert(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('发布留言时发生错误', 'error');
    });
}

// Edit message
function editMessage(messageId) {
    fetch(`message?action=get&id=${messageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('editMessageId').value = data.message.id;
                document.getElementById('editContent').value = data.message.content;
                document.getElementById('editModal').style.display = 'block';
            } else {
                showAlert('加载留言失败', 'error');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('加载留言时发生错误', 'error');
        });
}

// Handle edit form submission
function handleEditSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch('message', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            closeEditModal();
            loadMessages(currentPage);
        } else {
            showAlert(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('更新留言时发生错误', 'error');
    });
}

// Delete message
function deleteMessage(messageId) {
    if (!confirm('确定要删除这条留言吗？')) {
        return;
    }
    
    const csrfToken = document.querySelector('input[name="csrfToken"]').value;
    const formData = new FormData();
    formData.append('action', 'delete');
    formData.append('id', messageId);
    formData.append('csrfToken', csrfToken);
    
    fetch('message', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showAlert(data.message, 'success');
            loadMessages(currentPage);
        } else {
            showAlert(data.message, 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('删除留言时发生错误', 'error');
    });
}

// Close edit modal
function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

// Check if user has permission to modify message
function checkPermission(message) {
    // Server side will do the actual authorization
    // This is just for UI display
    
    // Admin can modify all messages
    if (currentIsAdmin) {
        return true;
    }
    
    // Logged-in user can modify their own messages
    if (currentUserId && message.userId && currentUserId === message.userId) {
        return true;
    }
    
    return false;
}

// Show alert message
function showAlert(message, type) {
    const alertDiv = document.getElementById('messageAlert');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.display = 'block';
    
    setTimeout(() => {
        alertDiv.style.display = 'none';
    }, 3000);
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('editModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
}
