# Image Upload Fix Guide

## Problem: Images Not Saving to uploads/ Directory

If images are not being saved to the uploads directory, follow these steps.

## Quick Fix

### Step 1: Ensure uploads/ Directory Exists

The directory should already exist at:
```
src/main/webapp/uploads/
```

But when running, files are saved to:
```
target/messageboard/uploads/
```

### Step 2: Recompile with Enhanced Logging

The updated `MessageServlet` includes detailed logging:

```bash
mvn clean package
mvn tomcat7:run
```

### Step 3: Test Upload and Check Logs

1. Upload an image through the web interface
2. Check server console output for:
   ```
   Upload directory created: /path/to/target/messageboard/uploads - Success: true
   Saving uploaded file to: /path/to/target/messageboard/uploads/uuid-123.jpg
   File saved successfully: /path/to/uploads/uuid-123.jpg (Size: 12345 bytes)
   ```

3. If you see warnings, check the output path

### Step 4: Verify File Location

```bash
# Windows
dir target\messageboard\uploads

# Linux/Mac
ls -la target/messageboard/uploads/
```

## Common Issues and Solutions

### Issue 1: Permission Denied

**Symptom**: Upload fails with permission error

**Solution**:
```bash
# Windows: Run command prompt as Administrator
# Linux/Mac:
chmod 755 target/messageboard/uploads/
```

### Issue 2: Directory Not Created

**Symptom**: `mkdirs()` returns false

**Solutions**:
1. Check disk space
2. Verify parent directories are writable
3. Check for file system errors

### Issue 3: File Size Too Large

**Symptom**: Upload fails silently or with error

**Solution**: Check file is < 10MB:
```java
// In web.xml, already configured:
<max-file-size>10485760</max-file-size>  // 10MB
```

### Issue 4: Wrong Directory Path

**Symptom**: Files save but images don't display

**Check**:
1. Database should store: `uploads/filename.jpg`
2. JSP should render: `${pageContext.request.contextPath}/uploads/filename.jpg`
3. Browser requests: `/messageboard/uploads/filename.jpg`

## Debugging Upload Issues

### Enable Detailed Logging

The updated servlet includes console output:
- Directory creation status
- File save path
- File size after save
- Warnings if file doesn't exist after save

### Check Server Logs

Look for these messages in console:
```
Upload directory created: ...
Saving uploaded file to: ...
File saved successfully: ... (Size: ... bytes)
```

### Manual Verification

1. Note the UUID in console output
2. Check if file exists:
   ```bash
   ls target/messageboard/uploads/ | grep {uuid}
   ```

3. Check database:
   ```sql
   SELECT image_path FROM messages WHERE image_path IS NOT NULL ORDER BY id DESC LIMIT 1;
   ```

4. Verify URL works:
   ```
   http://localhost:8080/messageboard/uploads/{filename}
   ```

## Upload Flow

```
User uploads image
    ↓
MessageServlet.handleCreateMessage()
    ↓
saveUploadedFile()
    ↓
1. Validate filename
2. Generate UUID filename
3. Get webapp real path: /path/to/target/messageboard/
4. Create uploads dir: /path/to/target/messageboard/uploads/
5. Write file: /path/to/target/messageboard/uploads/uuid-123.jpg
6. Log success/failure
    ↓
Return "uploads/uuid-123.jpg"
    ↓
Save to database
    ↓
Display in JSP: ${pageContext.request.contextPath}/uploads/uuid-123.jpg
```

## Verification Checklist

After uploading an image:

- [ ] Check server console for "File saved successfully" message
- [ ] Verify file exists in `target/messageboard/uploads/`
- [ ] Check database `image_path` column has correct path
- [ ] Verify image displays on main page
- [ ] Check image displays in admin dashboard
- [ ] Verify image URL is accessible directly

## Production Deployment

For production deployment, consider:

1. **External Storage**: Store uploads outside webapp
   ```java
   // Example: Use system property
   String uploadPath = System.getProperty("app.upload.dir", "/var/uploads/messageboard");
   ```

2. **Cloud Storage**: Use AWS S3, Azure Blob, etc.

3. **CDN**: Serve images through CDN for performance

4. **Backup**: Regular backup of uploads directory

## Troubleshooting Commands

### Windows

```cmd
# Check directory exists
dir target\messageboard\uploads

# Check file count
dir target\messageboard\uploads /b | find /c /v ""

# View recent files
dir target\messageboard\uploads /o-d

# Check disk space
wmic logicaldisk get size,freespace,caption
```

### Linux/Mac

```bash
# Check directory exists
ls -ld target/messageboard/uploads/

# Check file count
ls target/messageboard/uploads/ | wc -l

# View recent files
ls -lt target/messageboard/uploads/ | head

# Check disk space
df -h .
```

## Summary

✅ Enhanced `MessageServlet` with detailed logging  
✅ Better error handling for file uploads  
✅ Validation of filenames and extensions  
✅ Verification that files are saved  
✅ Clear console output for debugging  

**After recompiling, all uploads will include detailed logging to help diagnose any issues.**
