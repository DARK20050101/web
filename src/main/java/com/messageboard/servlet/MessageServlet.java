package com.messageboard.servlet;

import com.messageboard.model.Message;
import com.messageboard.service.MessageService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 10,      // 10MB
    maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class MessageServlet extends HttpServlet {
    private MessageService messageService = new MessageService();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();

        if ("list".equals(action)) {
            int page = 1;
            int pageSize = 10;
            
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException e) {
                page = 1;
            }

            List<Message> messages = messageService.getMessages(page, pageSize);
            int totalPages = messageService.getTotalPages(pageSize);
            int totalCount = messageService.getTotalCount();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("messages", messages);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            result.put("totalCount", totalCount);

            out.print(gson.toJson(result));
        } else if ("get".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            Message message = messageService.getMessageById(id);
            
            Map<String, Object> result = new HashMap<>();
            if (message != null) {
                result.put("success", true);
                result.put("message", message);
            } else {
                result.put("success", false);
                result.put("message", "留言不存在");
            }
            out.print(gson.toJson(result));
        }
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String action = request.getParameter("action");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();

        if ("create".equals(action)) {
            handleCreateMessage(request, response, session, out);
        } else if ("update".equals(action)) {
            handleUpdateMessage(request, response, session, out);
        } else if ("delete".equals(action)) {
            handleDeleteMessage(request, response, session, out);
        }

        out.flush();
        out.close();
    }

    private void handleCreateMessage(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, PrintWriter out) throws ServletException, IOException {
        String nickname = request.getParameter("nickname");
        String content = request.getParameter("content");
        String captcha = request.getParameter("captcha");
        
        Integer userId = (Integer) session.getAttribute("userId");
        boolean isAnonymous = (userId == null);

        Map<String, Object> result = new HashMap<>();

        // Validate captcha for anonymous users
        if (isAnonymous) {
            String sessionCaptcha = (String) session.getAttribute("captcha");
            if (captcha == null || !captcha.equalsIgnoreCase(sessionCaptcha)) {
                result.put("success", false);
                result.put("message", "验证码错误");
                out.print(gson.toJson(result));
                return;
            }
        }

        // Validate input
        if ((isAnonymous && (nickname == null || nickname.trim().isEmpty())) ||
            content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "昵称和内容不能为空");
            out.print(gson.toJson(result));
            return;
        }

        // Handle file upload
        String imagePath = null;
        try {
            Part filePart = request.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                imagePath = saveUploadedFile(filePart, request);
            }
        } catch (Exception e) {
            // File upload is optional, continue without it
        }

        // Create message
        Message message = new Message();
        message.setUserId(userId);
        message.setNickname(isAnonymous ? nickname : (String) session.getAttribute("username"));
        message.setContent(content);
        message.setImagePath(imagePath);
        message.setAnonymous(isAnonymous);

        boolean success = messageService.createMessage(message);
        result.put("success", success);
        result.put("message", success ? "留言发布成功" : "留言发布失败");
        
        out.print(gson.toJson(result));
    }

    private void handleUpdateMessage(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, PrintWriter out) throws ServletException, IOException {
        int messageId = Integer.parseInt(request.getParameter("id"));
        String content = request.getParameter("content");
        
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            out.print(gson.toJson(result));
            return;
        }

        // Handle file upload
        String imagePath = null;
        try {
            Part filePart = request.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                imagePath = saveUploadedFile(filePart, request);
            }
        } catch (Exception e) {
            // File upload is optional, continue without it
        }

        boolean success = messageService.updateMessage(messageId, content, imagePath, userId, isAdmin != null && isAdmin);
        result.put("success", success);
        result.put("message", success ? "留言更新成功" : "留言更新失败或无权限");
        
        out.print(gson.toJson(result));
    }

    private void handleDeleteMessage(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, PrintWriter out) {
        int messageId = Integer.parseInt(request.getParameter("id"));
        
        Integer userId = (Integer) session.getAttribute("userId");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");

        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            out.print(gson.toJson(result));
            return;
        }

        boolean success = messageService.deleteMessage(messageId, userId, isAdmin != null && isAdmin);
        result.put("success", success);
        result.put("message", success ? "留言删除成功" : "留言删除失败或无权限");
        
        out.print(gson.toJson(result));
    }

    private String saveUploadedFile(Part filePart, HttpServletRequest request) throws IOException {
        String fileName = getFileName(filePart);
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        
        String uploadPath = request.getServletContext().getRealPath("/") + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        String filePath = uploadPath + File.separator + newFileName;
        filePart.write(filePath);
        
        return "uploads/" + newFileName;
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return "";
    }
}
