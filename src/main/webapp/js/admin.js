// Admin Dashboard JavaScript
let currentTab = 'users';
let currentMessagePage = 1;
let currentSearchKeyword = '';
let searchTimeout = null;

// Get context path for API calls
const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2)) || '';
const apiUrl = contextPath + '/admin/api';

console.log('Admin.js loaded - API URL:', apiUrl);

// Load data on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOMContentLoaded - initializing admin dashboard');
    
    // Load initial data
    loadUsers();
    
    // Setup edit user form submission
    const editUserForm = document.getElementById('editUserForm');
    if (editUserForm) {
        editUserForm.addEventListener('submit', handleEditUserSubmit);
    }
    
    // Setup create user form submission
    const createUserForm = document.getElementById('createUserForm');
    if (createUserForm) {
        createUserForm.addEventListener('submit', handleCreateUserSubmit);
    }
    
    // Setup edit message form submission
    const editMessageForm = document.getElementById('editMessageForm');
    if (editMessageForm) {
        editMessageForm.addEventListener('submit', handleEditMessageSubmit);
    }
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
    console.log('Searching messages:', keyword, 'page:', page);
    
    fetch(apiUrl + `?action=search&type=messages&keyword=${encodeURIComponent(keyword)}&page=${page}`)
        .then(response => {
            console.log('Search response status:', response.status);
            if (!response.ok) {
                throw new Error('HTTP error ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Search data received:', data);
            if (data.success) {
                displayMessages(data.data);
                displayMessagePagination(page, data.totalPages, true);
                searchStatus.textContent = `找到 ${data.totalCount} 条结果`;
                if (data.data.length === 0) {
                    document.getElementById('messagesTableBody').innerHTML = 
                        '<tr><td colspan="6">未找到匹配的留言</td></tr>';
                }
            } else {
                showError('搜索失败: ' + (data.message || '未知错误'));
                searchStatus.textContent = '搜索失败';
            }
        })
        .catch(error => {
            console.error('Search error:', error);
            showError('搜索时发生错误: ' + error.message);
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
    console.log('Loading users from:', apiUrl + '?action=list&type=users');
    
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = '<tr><td colspan="6">加载中...</td></tr>';
    
    fetch(apiUrl + '?action=list&type=users')
        .then(response => {
            console.log('Users response status:', response.status);
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    showError('权限不足，请重新登录');
                    setTimeout(() => {
                        window.location.href = contextPath + '/login.jsp';
                    }, 2000);
                    throw new Error('Unauthorized');
                }
                throw new Error('HTTP error ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Users data received:', data);
            if (data.success) {
                displayUsers(data.data);
            } else {
                showError('加载用户列表失败: ' + (data.message || '未知错误'));
                tbody.innerHTML = '<tr><td colspan="6">加载失败</td></tr>';
            }
        })
        .catch(error => {
            console.error('Load users error:', error);
            if (error.message !== 'Unauthorized') {
                showError('加载用户时发生错误: ' + error.message);
                tbody.innerHTML = '<tr><td colspan="6">加载失败: ' + error.message + '</td></tr>';
            }
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
    console.log('Loading messages from:', apiUrl + `?action=list&type=messages&page=${page}`);
    
    const tbody = document.getElementById('messagesTableBody');
    tbody.innerHTML = '<tr><td colspan="6">加载中...</td></tr>';
    
    fetch(apiUrl + `?action=list&type=messages&page=${page}`)
        .then(response => {
            console.log('Messages response status:', response.status);
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    showError('权限不足，请重新登录');
                    setTimeout(() => {
                        window.location.href = contextPath + '/login.jsp';
                    }, 2000);
                    throw new Error('Unauthorized');
                }
                throw new Error('HTTP error ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('Messages data received:', data);
            if (data.success) {
                displayMessages(data.data);
                displayMessagePagination(page, data.totalPages);
            } else {
                showError('加载留言列表失败: ' + (data.message || '未知错误'));
                tbody.innerHTML = '<tr><td colspan="6">加载失败</td></tr>';
            }
        })
        .catch(error => {
            console.error('Load messages error:', error);
            if (error.message !== 'Unauthorized') {
                showError('加载留言时发生错误: ' + error.message);
                tbody.innerHTML = '<tr><td colspan="6">加载失败: ' + error.message + '</td></tr>';
            }
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
    fetch(apiUrl + `?action=get&type=user&id=${userId}`)
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
                showError('加载用户信息失败: ' + (data.message || ''));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载用户时发生错误: ' + error.message);
        });
}

// Handle edit user form submission
function handleEditUserSubmit(e) {
    e.preventDefault();
    
    const formData = new FormData(this);
    
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || '更新成功');
            closeEditUserModal();
            loadUsers();
        } else {
            showError(data.message || '更新失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('更新用户时发生错误: ' + error.message);
    });
}

// Delete user
function deleteUser(userId) {
    if (!confirm('确定要删除该用户吗？该用户的留言将变为匿名。')) {
        return;
    }
    
    const csrfToken = document.querySelector('input[name="csrfToken"]').value;
    const formData = new FormData();
    formData.append('action', 'delete');
    formData.append('type', 'user');
    formData.append('id', userId);
    formData.append('csrfToken', csrfToken);
    
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || '删除成功');
            loadUsers();
        } else {
            showError(data.message || '删除失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('删除用户时发生错误: ' + error.message);
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
    
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || '删除成功');
            loadMessages(currentMessagePage);
        } else {
            showError(data.message || '删除失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('删除留言时发生错误: ' + error.message);
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
    
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || '创建成功');
            closeCreateUserModal();
            loadUsers();
        } else {
            showError(data.message || '创建失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('创建用户时发生错误: ' + error.message);
    });
}

// Edit message
function editMessage(messageId) {
    fetch(apiUrl + `?action=get&type=message&id=${messageId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const message = data.data;
                document.getElementById('editMessageId').value = message.id;
                document.getElementById('editMessageContent').value = message.content;
                document.getElementById('editMessageModal').style.display = 'block';
            } else {
                showError('加载留言信息失败: ' + (data.message || ''));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('加载留言时发生错误: ' + error.message);
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
    
    fetch(apiUrl, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.message || '更新成功');
            closeEditMessageModal();
            loadMessages(currentMessagePage);
        } else {
            showError(data.message || '更新失败');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showError('更新留言时发生错误: ' + error.message);
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
