// Message Board App JavaScript

// Get CSRF token from meta tag
function getCsrfToken() {
    const meta = document.querySelector('meta[name="csrf-token"]');
    return meta ? meta.getAttribute('content') : '';
}

// Load messages from server
async function loadMessages() {
    try {
        const response = await fetch('/message-board/api/messages', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            displayMessages(result.data);
        } else {
            console.error('Failed to load messages:', result.error);
        }
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

// Display messages in the UI
function displayMessages(messages) {
    const messageList = document.getElementById('messageList');
    if (!messageList) return;
    
    if (messages.length === 0) {
        messageList.innerHTML = '<div class="card"><p>No messages yet. Be the first to post!</p></div>';
        return;
    }
    
    messageList.innerHTML = messages.map(msg => {
        const date = new Date(msg.createdAt);
        const formattedDate = date.toLocaleString();
        
        let imageHtml = '';
        if (msg.imagePath) {
            imageHtml = `
                <div class="message-image">
                    <img src="/message-board/image/${msg.imagePath}" 
                         alt="Message image" 
                         onclick="openImageModal(this.src)">
                </div>
            `;
        }
        
        return `
            <div class="message-item">
                <div class="message-header">
                    <span class="message-author">${escapeHtml(msg.nickname)}</span>
                    <span class="message-time">${formattedDate}</span>
                </div>
                <div class="message-content">${escapeHtml(msg.content)}</div>
                ${imageHtml}
            </div>
        `;
    }).join('');
}

// Submit message form
async function submitMessage(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Add CSRF token
    formData.append('csrfToken', getCsrfToken());
    
    try {
        const response = await fetch('/message-board/api/messages', {
            method: 'POST',
            headers: {
                'X-CSRF-Token': getCsrfToken()
            },
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            form.reset();
            clearImagePreview();
            loadMessages();
            showAlert('Message posted successfully!', 'success');
        } else {
            showAlert(result.error || 'Failed to post message', 'error');
        }
    } catch (error) {
        console.error('Error submitting message:', error);
        showAlert('Error posting message', 'error');
    }
}

// Handle image selection and preview
function handleImageSelect(event) {
    const file = event.target.files[0];
    const preview = document.getElementById('imagePreview');
    
    if (!preview) return;
    
    if (file) {
        // Validate file type
        const allowedTypes = ['image/png', 'image/jpeg', 'image/gif'];
        if (!allowedTypes.includes(file.type)) {
            showAlert('Please select a PNG, JPG, or GIF image', 'error');
            event.target.value = '';
            preview.innerHTML = '';
            return;
        }
        
        // Validate file size (2MB)
        if (file.size > 2 * 1024 * 1024) {
            showAlert('Image must be less than 2MB', 'error');
            event.target.value = '';
            preview.innerHTML = '';
            return;
        }
        
        // Show preview
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
        };
        reader.readAsDataURL(file);
    } else {
        preview.innerHTML = '';
    }
}

// Clear image preview
function clearImagePreview() {
    const preview = document.getElementById('imagePreview');
    const fileInput = document.getElementById('imageInput');
    
    if (preview) preview.innerHTML = '';
    if (fileInput) fileInput.value = '';
}

// Insert emoji into textarea
function insertEmoji(emoji) {
    const textarea = document.getElementById('content');
    if (!textarea) return;
    
    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const text = textarea.value;
    
    textarea.value = text.substring(0, start) + emoji + text.substring(end);
    textarea.selectionStart = textarea.selectionEnd = start + emoji.length;
    textarea.focus();
}

// Open image in modal (full size)
function openImageModal(src) {
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.9);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 9999;
        cursor: pointer;
    `;
    
    const img = document.createElement('img');
    img.src = src;
    img.style.cssText = 'max-width: 90%; max-height: 90%; border-radius: 8px;';
    
    modal.appendChild(img);
    modal.onclick = function() {
        document.body.removeChild(modal);
    };
    
    document.body.appendChild(modal);
}

// Show alert message
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    
    const container = document.querySelector('.container');
    if (container) {
        container.insertBefore(alertDiv, container.firstChild);
        
        setTimeout(() => {
            alertDiv.remove();
        }, 5000);
    }
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Login function
async function login(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    try {
        const response = await fetch('/message-board/api/login', {
            method: 'POST',
            headers: {
                'X-CSRF-Token': getCsrfToken()
            },
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            window.location.href = '/message-board/';
        } else {
            showAlert(result.error || 'Login failed', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('Login error', 'error');
    }
}

// Logout function
async function logout() {
    try {
        const response = await fetch('/message-board/api/logout', {
            method: 'POST',
            headers: {
                'X-CSRF-Token': getCsrfToken()
            }
        });
        
        const result = await response.json();
        
        if (result.success) {
            window.location.href = '/message-board/login.jsp';
        }
    } catch (error) {
        console.error('Logout error:', error);
    }
}

// Initialize app
document.addEventListener('DOMContentLoaded', function() {
    // Load messages on home page
    if (document.getElementById('messageList')) {
        loadMessages();
    }
    
    // Setup message form
    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        messageForm.addEventListener('submit', submitMessage);
    }
    
    // Setup image input
    const imageInput = document.getElementById('imageInput');
    if (imageInput) {
        imageInput.addEventListener('change', handleImageSelect);
    }
    
    // Setup login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', login);
    }
});
