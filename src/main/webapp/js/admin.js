// Admin Dashboard JavaScript
let currentTab = 'users';
let currentMessagePage = 1;
let currentSearchKeyword = '';
let searchTimeout = null;

// Load data on page load
document.addEventListener('DOMContentLoaded', function() {
    loadUsers();
    
    // Setup edit user form submission
    document.getElementById('editUserForm').addEventListener('submit', handleEditUserSubmit);
    
    // Setup create user form submission
    document.getElementById('createUserForm').addEventListener('submit', handleCreateUserSubmit);
    
    // Setup edit message form submission
    document.getElementById('editMessageForm').addEventListener('submit', handleEditMessageSubmit);
});

// Debounce search function
function debounceSearch(keyword) {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(function() {
        searchMessages(keyword, 1);
    }, 500); // Wait 500ms after user stops typing
}

// Search messages
function searchMessages(keyword, page) {
    currentSearchKeyword = keyword;
    currentMessagePage = page;
    
    const searchStatus = document.getElementById('searchStatus');
    
    if (!keyword || keyword.trim() === '') {
        // If empty, load all messages
        loadMessages(page);
        searchStatus.textContent = '';
        return;
    }
    
    searchStatus.textContent = '搜索中...';
    
    fetch(`api?action=search&type=messages&keyword=${encodeURIComponent(keyword)}&page=${page}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayMessages(data.data);
                displayMessagePagination(page, data.totalPages, true);
                searchStatus.textContent = `找到 ${data.totalCount} 条结果`;
                if (data.data.length === 0) {
                    document.getElementById('messagesTableBody').innerHTML = 
                        '<tr><td colspan="6">未找到匹配的留言</td></tr>';
                }
            } else {
                showError('搜索失败');
                searchStatus.textContent = '搜索失败';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('搜索时发生错误');
            searchStatus.textContent = '搜索错误';
        });
}

// Clear search
function clearSearch() {
    document.getElementById('messageSearchInput').value = '';
    document.getElementById('searchStatus').textContent = '';
    currentSearchKeyword = '';
    loadMessages(1);
}

// Show tab
function showTab(tabName) {
    currentTab = tabName;
    
    // Update tab buttons
    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    document.getElementById(tabName + 'Tab').classList.add('active');
    
    // Load data for the tab
    if (tabName === 'users') {
        loadUsers();
    } else if (tabName === 'messages') {
        clearSearch(); // Clear search when switching to messages tab
        loadMessages(1);
    }
}

// Load users
function loadUsers() {
    fetch('api?action=list&type=users')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayUsers(data.data);
            } else {
                showError('加载用户列表失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载用户时发生错误');
        });
}

// Display users
function displayUsers(users) {
    const tbody = document.getElementById('usersTableBody');
    
    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">暂无用户</td></tr>';
        return;
    }
    
    tbody.innerHTML = users.map(user => {
        const role = user.admin ? 
            '<span class="role-badge admin">管理员</span>' : 
            '<span class="role-badge user">普通用户</span>';
        const date = new Date(user.createdAt).toLocaleString('zh-CN');
        
        return `
            <tr>
                <td>${user.id}</td>
                <td>${escapeHtml(user.username)}</td>
                <td>${escapeHtml(user.email || '')}</td>
                <td>${role}</td>
                <td>${date}</td>
                <td class="actions">
                    <button class="btn btn-small" onclick="editUser(${user.id})">编辑</button>
                    <button class="btn btn-small btn-danger" onclick="deleteUser(${user.id})">删除</button>
                </td>
            </tr>
        `;
    }).join('');
}

// Load messages
function loadMessages(page) {
    currentMessagePage = page;
    fetch(`api?action=list&type=messages&page=${page}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayMessages(data.data);
                displayMessagePagination(page, data.totalPages);
            } else {
                showError('加载留言列表失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载留言时发生错误');
        });
}

// Display messages
function displayMessages(messages) {
    const tbody = document.getElementById('messagesTableBody');
    
    if (messages.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6">暂无留言</td></tr>';
        return;
    }
    
    tbody.innerHTML = messages.map(message => {
        const type = message.anonymous ? 
            '<span class="type-badge anonymous">匿名</span>' : 
            '<span class="type-badge registered">注册用户</span>';
        const date = new Date(message.createdAt).toLocaleString('zh-CN');
        const content = message.content.length > 50 ? 
            message.content.substring(0, 50) + '...' : message.content;
        
        return `
            <tr>
                <td>${message.id}</td>
                <td>${escapeHtml(message.nickname)}</td>
                <td class="content-preview">${content}</td>
                <td>${type}</td>
                <td>${date}</td>
                <td class="actions">
                    <button class="btn btn-small" onclick="editMessage(${message.id})">编辑</button>
                    <button class="btn btn-small btn-danger" onclick="deleteMessage(${message.id})">删除</button>
                </td>
            </tr>
        `;
    }).join('');
}

// Display message pagination
function displayMessagePagination(currentPage, totalPages, isSearch) {
    const pagination = document.getElementById('messagePagination');
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let html = '';
    
    if (currentPage > 1) {
        if (isSearch && currentSearchKeyword) {
            html += `<button class="btn" onclick="searchMessages('${currentSearchKeyword}', ${currentPage - 1})">上一页</button>`;
        } else {
            html += `<button class="btn" onclick="loadMessages(${currentPage - 1})">上一页</button>`;
        }
    }
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            const activeClass = i === currentPage ? 'active' : '';
            if (isSearch && currentSearchKeyword) {
                html += `<button class="btn ${activeClass}" onclick="searchMessages('${currentSearchKeyword}', ${i})">${i}</button>`;
            } else {
                html += `<button class="btn ${activeClass}" onclick="loadMessages(${i})">${i}</button>`;
            }
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            html += '<span>...</span>';
        }
    }
    
    if (currentPage < totalPages) {
        if (isSearch && currentSearchKeyword) {
            html += `<button class="btn" onclick="searchMessages('${currentSearchKeyword}', ${currentPage + 1})">下一页</button>`;
        } else {
            html += `<button class="btn" onclick="loadMessages(${currentPage + 1})">下一页</button>`;
        }
    }
    
    pagination.innerHTML = html;
}

// Edit user
function editUser(userId) {
    fetch(`api?action=get&type=user&id=${userId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const user = data.data;
                document.getElementById('editUserId').value = user.id;
                document.getElementById('editUsername').value = user.username;
                document.getElementById('editEmail').value = user.email || '';
                document.getElementById('editIsAdmin').checked = user.admin;
                document.getElementById('editUserModal').style.display = 'block';
            } else {
                showError('加载用户信息失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载用户时发生错误');
        });
}

// Handle edit user form submission
function handleEditUserSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch('api', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeEditUserModal();
            loadUsers();
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('更新用户时发生错误');
    });
}

// Delete user
function deleteUser(userId) {
    if (!confirm('确定要删除该用户吗？这将删除该用户的所有相关数据。')) {
        return;
    }
    
    const csrfToken = document.querySelector('input[name="csrfToken"]').value;
    const formData = new FormData();
    formData.append('action', 'delete');
    formData.append('type', 'user');
    formData.append('id', userId);
    formData.append('csrfToken', csrfToken);
    
    fetch('api', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            loadUsers();
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('删除用户时发生错误');
    });
}

// Delete message
function deleteMessage(messageId) {
    if (!confirm('确定要删除该留言吗？')) {
        return;
    }
    
    const csrfToken = document.querySelector('input[name="csrfToken"]').value;
    const formData = new FormData();
    formData.append('action', 'delete');
    formData.append('type', 'message');
    formData.append('id', messageId);
    formData.append('csrfToken', csrfToken);
    
    fetch('api', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            loadMessages(currentMessagePage);
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('删除留言时发生错误');
    });
}

// Close edit user modal
function closeEditUserModal() {
    document.getElementById('editUserModal').style.display = 'none';
}

// Show create user modal
function showCreateUserModal() {
    document.getElementById('createUserForm').reset();
    document.getElementById('createUserModal').style.display = 'block';
}

// Close create user modal
function closeCreateUserModal() {
    document.getElementById('createUserModal').style.display = 'none';
}

// Handle create user form submission
function handleCreateUserSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch('api', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeCreateUserModal();
            loadUsers();
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('创建用户时发生错误');
    });
}

// Edit message
function editMessage(messageId) {
    fetch(`api?action=get&type=message&id=${messageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const message = data.data;
                document.getElementById('editMessageId').value = message.id;
                document.getElementById('editMessageContent').value = message.content;
                document.getElementById('editMessageModal').style.display = 'block';
            } else {
                showError('加载留言信息失败');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载留言时发生错误');
        });
}

// Close edit message modal
function closeEditMessageModal() {
    document.getElementById('editMessageModal').style.display = 'none';
}

// Handle edit message form submission
function handleEditMessageSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch('api', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeEditMessageModal();
            loadMessages(currentMessagePage);
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('更新留言时发生错误');
    });
}

// Show error
function showError(message) {
    alert(message);
}

// Escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Close modal when clicking outside
window.onclick = function(event) {
    const editUserModal = document.getElementById('editUserModal');
    const createUserModal = document.getElementById('createUserModal');
    const editMessageModal = document.getElementById('editMessageModal');
    
    if (event.target === editUserModal) {
        editUserModal.style.display = 'none';
    }
    if (event.target === createUserModal) {
        createUserModal.style.display = 'none';
    }
    if (event.target === editMessageModal) {
        editMessageModal.style.display = 'none';
    }
}
