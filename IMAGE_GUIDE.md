# Image Upload and Display Guide

## Overview

This guide explains how image upload and display works in the Message Board System.

## Storage Location

Images are stored in the application's `uploads/` directory:

```
<webapp-root>/uploads/
Example: E:/1/1/web/target/messageboard/uploads/
```

## Image Upload Process

### 1. File Upload

When a user uploads an image:

1. User selects an image file (max 10MB)
2. System validates file type and size
3. System generates unique filename using UUID
4. File is saved to `<webapp>/uploads/` directory
5. Database stores the relative path: `uploads/filename.jpg`

### 2. Supported Formats

- **Images**: .jpg, .jpeg, .png, .gif, .bmp
- **File Size**: Max 10MB per file
- **Request Size**: Max 50MB total

### 3. Filename Generation

```java
String filename = UUID.randomUUID().toString() + extension;
// Example: abc123-def456-789.jpg
```

## Image Display

### In JSP/HTML

```html
<!-- Display image in message -->
<img src="${pageContext.request.contextPath}/uploads/filename.jpg" 
     alt="Message Image" 
     style="max-width: 100%; height: auto;">
```

### URL Resolution

```
JSP: ${pageContext.request.contextPath}/uploads/filename.jpg
Resolves to: /messageboard/uploads/filename.jpg
```

### In JavaScript

```javascript
// Construct image URL
const imageUrl = `${contextPath}/uploads/${imagePath}`;

// Display in HTML
const imgHtml = `<img src="${imageUrl}" alt="Image" class="message-image">`;
```

## Image Display Contexts

### 1. Main Page (index.jsp)

Messages with images show thumbnail with click-to-enlarge:

```javascript
if (message.imagePath) {
    const imgUrl = contextPath + '/' + message.imagePath;
    messageContent += `
        <div class="message-image-container">
            <img src="${imgUrl}" alt="Message Image" class="message-image">
        </div>
    `;
}
```

### 2. Admin Dashboard

Messages show image preview in the message list:

```javascript
if (message.imagePath) {
    content += ` <span class="image-indicator">📷</span>`;
}
```

### 3. Edit Message

When editing a message with an image:
- Current image is displayed
- User can upload a new image to replace it
- Old image remains in filesystem

## Security Considerations

### 1. File Type Validation

```java
// In MessageServlet
String contentType = filePart.getContentType();
if (!contentType.startsWith("image/")) {
    // Reject non-image files
}
```

### 2. File Size Limits

Configured in `web.xml`:

```xml
<multipart-config>
    <max-file-size>10485760</max-file-size>      <!-- 10MB -->
    <max-request-size>52428800</max-request-size>  <!-- 50MB -->
</multipart-config>
```

### 3. Unique Filenames

- UUID-based naming prevents conflicts
- Prevents path traversal attacks
- No user-supplied filenames used

## Troubleshooting

### Image Not Displaying

**Problem**: Image shows broken icon  
**Solutions**:
1. Check if file exists in `uploads/` directory
2. Verify database `image_path` value is correct
3. Check file permissions (readable by web server)
4. Verify `uploads/` directory exists

### Upload Fails

**Problem**: "File too large" or upload error  
**Solutions**:
1. Check file size (must be < 10MB)
2. Verify file is valid image format
3. Check disk space on server
4. Verify `uploads/` directory is writable

### Image Path Issues

**Problem**: 404 error when loading image  
**Solutions**:
1. Check database stores: `uploads/filename.jpg` (with uploads/ prefix)
2. Verify JSP uses: `${pageContext.request.contextPath}/uploads/...`
3. Check tomcat context path matches

## File Management

### Cleanup Old Images

Currently, old images are not automatically deleted when:
- Message is deleted
- Image is replaced during edit

To implement cleanup:

```java
// Delete old image file
if (oldImagePath != null) {
    File oldFile = new File(uploadPath, oldImagePath.replace("uploads/", ""));
    if (oldFile.exists()) {
        oldFile.delete();
    }
}
```

### Backup Images

To backup uploaded images:

```bash
# Windows
xcopy target\messageboard\uploads\* backup\uploads\ /S /Y

# Linux/Mac
cp -r target/messageboard/uploads/* backup/uploads/
```

## Testing Image Upload

### 1. Test Upload

```
1. Go to main page
2. Post a message
3. Click "Choose File"
4. Select an image
5. Submit message
6. Verify image displays in message
```

### 2. Verify Storage

```bash
# Check file exists
ls target/messageboard/uploads/

# Check database
SELECT id, nickname, content, image_path FROM messages WHERE image_path IS NOT NULL;
```

### 3. Test Display

```
1. Refresh page
2. Image should still display
3. Click image to view full size
4. Check in admin dashboard
```

## Configuration

### Upload Directory

Default location: `<webapp>/uploads/`

To change, modify in `MessageServlet`:

```java
// Get upload directory
String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
```

### File Size Limits

Modify in `web.xml`:

```xml
<multipart-config>
    <max-file-size>10485760</max-file-size>      <!-- Change to desired size -->
    <max-request-size>52428800</max-request-size>
</multipart-config>
```

## Best Practices

1. **Validate all uploads** - Check type and size
2. **Use unique filenames** - Prevent conflicts
3. **Store relative paths** - Database portability
4. **Implement cleanup** - Remove orphaned files
5. **Monitor disk space** - Prevent filling disk
6. **Backup regularly** - Protect user uploads

## API Response Format

### Successful Upload

```json
{
    "success": true,
    "message": "留言发表成功",
    "data": {
        "id": 123,
        "nickname": "user",
        "content": "Message content",
        "imagePath": "uploads/abc-123.jpg",
        "anonymous": false,
        "createdAt": "2025-12-26T10:30:00"
    }
}
```

### Upload Error

```json
{
    "success": false,
    "message": "图片上传失败：文件过大"
}
```

## Summary

- ✅ Images stored in `uploads/` directory
- ✅ UUID-based filenames for security
- ✅ Database stores relative path
- ✅ Max 10MB per image
- ✅ Supported formats: jpg, jpeg, png, gif, bmp
- ✅ Display on main page and admin dashboard
- ✅ Can upload/replace images when editing

For more information, see the API Documentation and Troubleshooting guides.
