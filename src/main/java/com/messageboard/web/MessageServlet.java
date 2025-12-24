package com.messageboard.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.messageboard.dao.MessageDao;
import com.messageboard.model.Message;
import com.messageboard.model.User;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * MessageServlet - Handles message operations including image upload
 */
@WebServlet("/api/messages")
@MultipartConfig(
    maxFileSize = 2 * 1024 * 1024,      // 2MB max file size
    maxRequestSize = 3 * 1024 * 1024    // 3MB max request size
)
public class MessageServlet extends HttpServlet {
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
        Arrays.asList("image/png", "image/jpeg", "image/gif")
    );
    private static final Map<String, String> MIME_TO_EXT = new HashMap<>();
    
    static {
        MIME_TO_EXT.put("image/png", "png");
        MIME_TO_EXT.put("image/jpeg", "jpg");
        MIME_TO_EXT.put("image/gif", "gif");
    }

    private MessageDao messageDao;
    private Gson gson;
    private String uploadDir;

    @Override
    public void init() throws ServletException {
        messageDao = new MessageDao();
        gson = new Gson();
        
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

    /**
     * GET - Retrieve all messages
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        List<Message> messages = messageDao.findAll();
        
        // Convert messages to JSON-friendly format
        List<Map<String, Object>> messageList = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("userId", msg.getUserId());
            map.put("nickname", msg.getNickname());
            map.put("content", msg.getContent());
            map.put("imagePath", msg.getImagePath());
            map.put("createdAt", msg.getCreatedAt().getTime());
            messageList.add(map);
        }
        
        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.add("data", gson.toJsonTree(messageList));
        
        response.getWriter().write(gson.toJson(result));
    }

    /**
     * POST - Create a new message with optional image
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Get form parameters
            String content = request.getParameter("content");
            String nickname = request.getParameter("nickname");
            
            // Validate required fields
            if (content == null || content.trim().isEmpty()) {
                sendError(response, "Content is required");
                return;
            }
            
            // Get current user from session
            User currentUser = (User) request.getSession().getAttribute("user");
            
            // If not logged in, nickname is required
            if (currentUser == null) {
                if (nickname == null || nickname.trim().isEmpty()) {
                    sendError(response, "Nickname is required for anonymous users");
                    return;
                }
            } else {
                // Use logged-in username as nickname
                nickname = currentUser.getUsername();
            }
            
            // Handle image upload (optional)
            String imagePath = null;
            Part imagePart = request.getPart("image");
            
            if (imagePart != null && imagePart.getSize() > 0) {
                imagePath = handleImageUpload(imagePart);
                if (imagePath == null) {
                    sendError(response, "Invalid image file");
                    return;
                }
            }
            
            // Create message
            Message message = new Message();
            message.setContent(content);
            message.setNickname(nickname);
            message.setImagePath(imagePath);
            
            if (currentUser != null) {
                message.setUserId(currentUser.getId());
            }
            
            boolean success = messageDao.create(message);
            
            if (success) {
                JsonObject result = new JsonObject();
                result.addProperty("success", true);
                result.addProperty("message", "Message created successfully");
                response.getWriter().write(gson.toJson(result));
            } else {
                sendError(response, "Failed to create message");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Server error: " + e.getMessage());
        }
    }

    /**
     * DELETE - Delete a message (admin only)
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Check if user is admin
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            sendError(response, "Unauthorized");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            sendError(response, "Message ID is required");
            return;
        }
        
        try {
            int messageId = Integer.parseInt(pathInfo.substring(1));
            boolean success = messageDao.delete(messageId);
            
            JsonObject result = new JsonObject();
            result.addProperty("success", success);
            result.addProperty("message", success ? "Message deleted" : "Failed to delete message");
            response.getWriter().write(gson.toJson(result));
            
        } catch (NumberFormatException e) {
            sendError(response, "Invalid message ID");
        }
    }

    /**
     * Handle image upload with validation
     */
    private String handleImageUpload(Part imagePart) throws IOException {
        // Check file size
        if (imagePart.getSize() > MAX_FILE_SIZE) {
            return null;
        }
        
        // Check MIME type
        String contentType = imagePart.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            return null;
        }
        
        // Validate actual image content using ImageIO
        try (InputStream is = imagePart.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                // Not a valid image
                return null;
            }
        }
        
        // Generate safe filename with UUID
        String extension = MIME_TO_EXT.get(contentType);
        String filename = UUID.randomUUID().toString() + "." + extension;
        
        // Save file
        File file = new File(uploadDir, filename);
        try (InputStream is = imagePart.getInputStream();
             FileOutputStream fos = new FileOutputStream(file)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        
        return filename;
    }

    /**
     * Send error response
     */
    private void sendError(HttpServletResponse response, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("error", message);
        response.getWriter().write(gson.toJson(error));
    }
}
