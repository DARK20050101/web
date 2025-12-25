// Message Board Application JavaScript

let csrfToken = '';

// Initialize application
document.addEventListener('DOMContentLoaded', function() {
    initCsrfToken();
    initEmojiPicker();
    initImageUpload();
    loadMessages();
    checkLoginStatus();
    initCaptcha();
});

// Initialize CSRF token
function initCsrfToken() {
    // Get CSRF token from meta tag or session
    const metaToken = document.querySelector('meta[name="csrf-token"]');
    if (metaToken) {
        csrfToken = metaToken.getAttribute('content');
    }
}

// Initialize emoji picker
function initEmojiPicker() {
    const emojiButtons = document.querySelectorAll('.emoji-btn');
    const contentTextarea = document.getElementById('content');
    
    if (emojiButtons && contentTextarea) {
        emojiButtons.forEach(button => {
            button.addEventListener('click', function() {
                const emoji = this.textContent;
                const cursorPos = contentTextarea.selectionStart;
                const textBefore = contentTextarea.value.substring(0, cursorPos);
                const textAfter = contentTextarea.value.substring(cursorPos);
                contentTextarea.value = textBefore + emoji + textAfter;
                contentTextarea.focus();
                contentTextarea.selectionStart = contentTextarea.selectionEnd = cursorPos + emoji.length;
            });
        });
    }
}

// Initialize image upload preview
function initImageUpload() {
    const imageInput = document.getElementById('image');
    const imagePreview = document.getElementById('imagePreview');
    
    if (imageInput && imagePreview) {
        imageInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                // Validate file size (2MB)
                if (file.size > 2 * 1024 * 1024) {
                    alert('File size must not exceed 2MB');
                    imageInput.value = '';
                    imagePreview.classList.remove('show');
                    return;
                }
                
                // Validate file type
                const validTypes = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif'];
                if (!validTypes.includes(file.type)) {
                    alert('Only PNG, JPEG, and GIF images are allowed');
                    imageInput.value = '';
                    imagePreview.classList.remove('show');
                    return;
                }
                
                // Show preview
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    imagePreview.classList.add('show');
                };
                reader.readAsDataURL(file);
            } else {
                imagePreview.classList.remove('show');
            }
        });
    }
}

// Initialize captcha for anonymous users
function initCaptcha() {
    const captchaContainer = document.getElementById('captchaContainer');
    if (captchaContainer && captchaContainer.style.display !== 'none') {
        fetch('/api/captcha')
            .then(response => response.json())
            .then(data => {
                const captchaQuestion = document.getElementById('captchaQuestion');
                if (captchaQuestion) {
                    captchaQuestion.textContent = data.question;
                }
            })
            .catch(error => console.error('Error loading captcha:', error));
    }
}

// Check login status
function checkLoginStatus() {
    // This would typically make an API call to check session
    // For now, we rely on JSP to set the initial state
}

// Submit message form
function submitMessage(event) {
    event.preventDefault();
    
    const form = document.getElementById('messageForm');
    const formData = new FormData(form);
    
    // Add CSRF token
    if (csrfToken) {
        formData.append('csrfToken', csrfToken);
    }
    
    // Show loading state
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading"></span> Posting...';
    
    fetch('/api/messages', {
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-Token': csrfToken
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Clear form
            form.reset();
            document.getElementById('imagePreview').classList.remove('show');
            
            // Reload messages
            loadMessages();
            
            // Show success message
            showAlert('Message posted successfully!', 'success');
            
            // Reload captcha if needed
            initCaptcha();
        } else {
            showAlert(data.error || 'Failed to post message', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Failed to post message', 'error');
    })
    .finally(() => {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
}

// Load messages
function loadMessages() {
    fetch('/api/messages')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                renderMessages(data.messages);
            } else {
                console.error('Failed to load messages:', data.error);
            }
        })
        .catch(error => {
            console.error('Error loading messages:', error);
        });
}

// Render messages
function renderMessages(messages) {
    const messageList = document.getElementById('messageList');
    
    if (!messageList) return;
    
    if (messages.length === 0) {
        messageList.innerHTML = '<p style="text-align: center; color: #999;">No messages yet. Be the first to post!</p>';
        return;
    }
    
    messageList.innerHTML = messages.map(message => {
        const date = new Date(message.createdAt);
        const formattedDate = formatDate(date);
        
        let imageHtml = '';
        if (message.imagePath) {
            imageHtml = `<img src="/image/${message.imagePath}" 
                              alt="Attached image" 
                              class="message-image" 
                              onclick="openImageModal(this.src)">`;
        }
        
        const isAdmin = message.author === 'admin' || (window.currentUser && window.currentUser.isAdmin);
        const deleteBtn = (window.currentUser && window.currentUser.isAdmin) ? 
            `<button class="delete-btn" onclick="deleteMessage(${message.id})">Delete</button>` : '';
        
        return `
            <div class="message-item">
                <div class="message-header">
                    <span class="message-author">
                        ${escapeHtml(message.author)}
                        ${isAdmin ? '<span class="admin-badge">ADMIN</span>' : ''}
                    </span>
                    <div>
                        <span class="message-time">${formattedDate}</span>
                        ${deleteBtn}
                    </div>
                </div>
                <div class="message-content">${message.content}</div>
                ${imageHtml}
            </div>
        `;
    }).join('');
}

// Delete message (admin only)
function deleteMessage(id) {
    if (!confirm('Are you sure you want to delete this message?')) {
        return;
    }
    
    fetch(`/api/messages?id=${id}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-Token': csrfToken
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            loadMessages();
            showAlert('Message deleted successfully', 'success');
        } else {
            showAlert(data.error || 'Failed to delete message', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Failed to delete message', 'error');
    });
}

// Format date
function formatDate(date) {
    const now = new Date();
    const diff = now - date;
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);
    
    if (days > 7) {
        return date.toLocaleDateString();
    } else if (days > 0) {
        return `${days} day${days > 1 ? 's' : ''} ago`;
    } else if (hours > 0) {
        return `${hours} hour${hours > 1 ? 's' : ''} ago`;
    } else if (minutes > 0) {
        return `${minutes} minute${minutes > 1 ? 's' : ''} ago`;
    } else {
        return 'Just now';
    }
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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

// Open image in modal (simple implementation)
function openImageModal(src) {
    window.open(src, '_blank');
}

// Login function
function login(event) {
    event.preventDefault();
    
    const form = event.target;
    const formData = new FormData(form);
    
    // Add CSRF token
    if (csrfToken) {
        formData.append('csrfToken', csrfToken);
    }
    
    const submitBtn = form.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="loading"></span> Logging in...';
    
    fetch('/login', {
        method: 'POST',
        body: formData,
        headers: {
            'X-CSRF-Token': csrfToken
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            csrfToken = data.csrfToken;
            window.location.href = '/index.jsp';
        } else {
            showAlert(data.error || 'Login failed', 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('Login failed', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    });
}

// Logout function
function logout() {
    window.location.href = '/login?action=logout';
}
