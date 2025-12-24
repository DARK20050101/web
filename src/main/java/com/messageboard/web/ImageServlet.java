package com.messageboard.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ImageServlet - Securely serves uploaded images
 * Only allows access to files in the upload directory with safe filenames
 */
@WebServlet("/image/*")
public class ImageServlet extends HttpServlet {
    private String uploadDir;

    @Override
    public void init() throws ServletException {
        // Get upload directory from context parameter or use default
        uploadDir = getServletContext().getInitParameter("uploadDir");
        if (uploadDir == null || uploadDir.isEmpty()) {
            uploadDir = "/var/tmp/board_uploads";
        }
        
        // Create upload directory if it doesn't exist
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        
        // Validate path info
        if (pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid image path");
            return;
        }

        // Remove leading slash
        String filename = pathInfo.substring(1);
        
        // Security: validate filename to prevent path traversal
        if (!isValidFilename(filename)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid filename");
            return;
        }

        // Construct full file path
        Path filePath = Paths.get(uploadDir, filename);
        File file = filePath.toFile();

        // Check if file exists and is within upload directory
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
            return;
        }

        // Prevent path traversal - ensure file is in upload directory
        String canonicalUploadDir = new File(uploadDir).getCanonicalPath();
        String canonicalFilePath = file.getCanonicalPath();
        if (!canonicalFilePath.startsWith(canonicalUploadDir)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return;
        }

        // Determine content type
        String contentType = getServletContext().getMimeType(filename);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // Set security and caching headers
        response.setContentType(contentType);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=86400"); // Cache for 1 day
        response.setContentLengthLong(file.length());

        // Write file to response
        Files.copy(filePath, response.getOutputStream());
    }

    /**
     * Validate filename to prevent path traversal and ensure safe characters
     */
    private boolean isValidFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        // Must match UUID pattern with extension: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.ext
        return filename.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.(png|jpg|jpeg|gif)$");
    }
}
