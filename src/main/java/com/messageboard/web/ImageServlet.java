package com.messageboard.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Image Servlet - securely serves uploaded images
 */
@WebServlet("/image/*")
public class ImageServlet extends HttpServlet {
    
    private String uploadDir;
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9_-]+\\.(png|jpg|jpeg|gif)$");

    @Override
    public void init() throws ServletException {
        uploadDir = getServletContext().getInitParameter("uploadDir");
        if (uploadDir == null || uploadDir.isEmpty()) {
            String tmpDir = System.getProperty("java.io.tmpdir");
            uploadDir = tmpDir + File.separator + "board_uploads";
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        // Validate path
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image path");
            return;
        }
        
        // Get filename (remove leading slash)
        String filename = pathInfo.substring(1);
        
        // Security: Validate filename to prevent path traversal
        if (!SAFE_FILENAME.matcher(filename).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid filename");
            return;
        }
        
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid filename");
            return;
        }
        
        File imageFile = new File(uploadDir, filename);
        
        // Check if file exists and is within upload directory
        if (!imageFile.exists() || !imageFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }
        
        // Verify the file is actually in the upload directory (prevent symlink attacks)
        String canonicalUploadDir = new File(uploadDir).getCanonicalPath();
        String canonicalFilePath = imageFile.getCanonicalPath();
        if (!canonicalFilePath.startsWith(canonicalUploadDir)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }
        
        // Determine content type
        String contentType = getServletContext().getMimeType(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        // Set security headers
        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=31536000"); // Cache for 1 year
        response.setContentLengthLong(imageFile.length());
        
        // Write file to response
        Files.copy(imageFile.toPath(), response.getOutputStream());
    }
}
