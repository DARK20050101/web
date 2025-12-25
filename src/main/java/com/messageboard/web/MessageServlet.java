package com.messageboard.web;

import com.google.gson.Gson;
import com.messageboard.dao.MessageDao;
import com.messageboard.model.Message;
import com.messageboard.model.User;
import com.messageboard.util.SecurityUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Message Servlet - handles message CRUD operations with image upload support
 */
@WebServlet("/api/messages")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,     // 1 MB
    maxFileSize = 1024 * 1024 * 2,        // 2 MB
    maxRequestSize = 1024 * 1024 * 3      // 3 MB
)
public class MessageServlet extends HttpServlet {
    
    private MessageDao messageDao;
    private Gson gson;
    private String uploadDir;
    
    // Allowed image types
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
        "image/png", "image/jpeg", "image/jpg", "image/gif"
    ));
    
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    @Override
    public void init() throws ServletException {
        messageDao = new MessageDao();
        gson = new Gson();
        
        // Get upload directory from context parameter or use default
        uploadDir = getServletContext().getInitParameter("uploadDir");
        if (uploadDir == null || uploadDir.isEmpty()) {
            // Use system temp directory by default
            String tmpDir = System.getProperty("java.io.tmpdir");
            uploadDir = tmpDir + File.separator + "board_uploads";
        }
        
        // Create upload directory if it doesn't exist
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            if (!uploadPath.mkdirs()) {
                throw new ServletException("Failed to create upload directory: " + uploadDir);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            String idParam = request.getParameter("id");
            
            if (idParam != null) {
                // Get single message
                int id = Integer.parseInt(idParam);
                Message message = messageDao.getMessageById(id);
                
                if (message != null) {
                    result.put("success", true);
                    result.put("message", convertMessageToMap(message));
                } else {
                    result.put("success", false);
                    result.put("error", "Message not found");
                }
            } else {
                // Get all messages
                List<Message> messages = messageDao.getAllMessages();
                List<Map<String, Object>> messageList = new ArrayList<>();
                
                for (Message message : messages) {
                    messageList.add(convertMessageToMap(message));
                }
                
                result.put("success", true);
                result.put("messages", messageList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            User user = null;
            if (session != null) {
                user = (User) session.getAttribute("user");
            }
            
            // Get form parameters
            String content = request.getParameter("content");
            String author = request.getParameter("author");
            String captcha = request.getParameter("captcha");
            
            // Validate content
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("error", "Message content is required");
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // If user is not logged in, require author name and captcha
            if (user == null) {
                if (author == null || author.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("error", "Author name is required for anonymous posts");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                // Simple captcha validation (for demo purposes)
                String sessionCaptcha = (String) session.getAttribute("captcha");
                if (captcha == null || !captcha.equals(sessionCaptcha)) {
                    result.put("success", false);
                    result.put("error", "Invalid captcha");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
            } else {
                // Use logged-in username as author
                author = user.getUsername();
            }
            
            // Handle image upload
            String imagePath = null;
            Part filePart = request.getPart("image");
            
            if (filePart != null && filePart.getSize() > 0) {
                // Validate file size
                if (filePart.getSize() > MAX_FILE_SIZE) {
                    result.put("success", false);
                    result.put("error", "Image file size must not exceed 2MB");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                // Validate content type
                String contentType = filePart.getContentType();
                if (!ALLOWED_TYPES.contains(contentType)) {
                    result.put("success", false);
                    result.put("error", "Only PNG, JPEG, and GIF images are allowed");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                // Validate actual image content using ImageIO
                try (InputStream inputStream = filePart.getInputStream()) {
                    BufferedImage image = ImageIO.read(inputStream);
                    if (image == null) {
                        result.put("success", false);
                        result.put("error", "Invalid image file");
                        response.getWriter().write(gson.toJson(result));
                        return;
                    }
                }
                
                // Generate unique filename
                String extension = getFileExtension(contentType);
                String filename = UUID.randomUUID().toString() + "." + extension;
                File file = new File(uploadDir, filename);
                
                // Save the file
                filePart.write(file.getAbsolutePath());
                imagePath = filename;
            }
            
            // Get client IP address
            String ipAddress = request.getRemoteAddr();
            
            // Save message to database
            Integer userId = user != null ? user.getId() : null;
            boolean success = messageDao.createMessage(content, author, userId, imagePath, ipAddress);
            
            if (success) {
                result.put("success", true);
                result.put("message", "Message posted successfully");
            } else {
                result.put("success", false);
                result.put("error", "Failed to save message");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            User user = null;
            if (session != null) {
                user = (User) session.getAttribute("user");
            }
            
            // Only admin can delete messages
            if (user == null || !user.isAdmin()) {
                result.put("success", false);
                result.put("error", "Unauthorized");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            String idParam = request.getParameter("id");
            if (idParam == null) {
                result.put("success", false);
                result.put("error", "Message ID is required");
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            int id = Integer.parseInt(idParam);
            
            // Get message to delete associated image
            Message message = messageDao.getMessageById(id);
            
            boolean success = messageDao.deleteMessage(id);
            
            if (success) {
                // Delete associated image file if exists
                if (message != null && message.getImagePath() != null) {
                    File imageFile = new File(uploadDir, message.getImagePath());
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                }
                
                result.put("success", true);
                result.put("message", "Message deleted successfully");
            } else {
                result.put("success", false);
                result.put("error", "Failed to delete message");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    /**
     * Convert Message object to Map for JSON serialization
     */
    private Map<String, Object> convertMessageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("content", SecurityUtil.escapeHtml(message.getContent()));
        map.put("author", SecurityUtil.escapeHtml(message.getAuthor()));
        map.put("userId", message.getUserId());
        map.put("imagePath", message.getImagePath());
        map.put("createdAt", message.getCreatedAt().getTime());
        return map;
    }

    /**
     * Get file extension from content type
     */
    private String getFileExtension(String contentType) {
        switch (contentType) {
            case "image/png":
                return "png";
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/gif":
                return "gif";
            default:
                return "bin";
        }
    }
}
