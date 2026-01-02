package com.messageboard.servlet;

import com.messageboard.model.Message;
import com.messageboard.model.User;
import com.messageboard.service.MessageService;
import com.messageboard.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminServlet extends HttpServlet {
    private UserService userService = new UserService();
    private MessageService messageService = new MessageService();
    private Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        
        // Enhanced admin check with logging
        if (session == null) {
            System.err.println("AdminServlet: No session found");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }
        
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        Integer userId = (Integer) session.getAttribute("userId");
        
        System.out.println("AdminServlet doGet - isAdmin: " + isAdmin + ", userId: " + userId);
        
        if (isAdmin == null || !isAdmin) {
            System.err.println("AdminServlet: User is not admin - isAdmin=" + isAdmin);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return;
        }

        String action = request.getParameter("action");
        String type = request.getParameter("type");
        
        System.out.println("AdminServlet: action=" + action + ", type=" + type);
        
        PrintWriter out = response.getWriter();

        Map<String, Object> result = new HashMap<>();

        try {
            if ("list".equals(action)) {
                if ("users".equals(type)) {
                    List<User> users = userService.getAllUsers();
                    System.out.println("AdminServlet: Retrieved " + users.size() + " users");
                    result.put("success", true);
                    result.put("data", users);
                } else if ("messages".equals(type)) {
                    int page = 1;
                    int pageSize = 20;
                    try {
                        page = Integer.parseInt(request.getParameter("page"));
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                    List<Message> messages = messageService.getMessages(page, pageSize);
                    int totalPages = messageService.getTotalPages(pageSize);
                    System.out.println("AdminServlet: Retrieved " + messages.size() + " messages, page " + page + " of " + totalPages);
                    result.put("success", true);
                    result.put("data", messages);
                    result.put("totalPages", totalPages);
                }
            } else if ("search".equals(action)) {
                if ("messages".equals(type)) {
                    String keyword = request.getParameter("keyword");
                    int page = 1;
                    int pageSize = 20;
                    try {
                        page = Integer.parseInt(request.getParameter("page"));
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                    
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        List<Message> messages = messageService.searchMessages(keyword, page, pageSize);
                        int totalPages = messageService.getSearchTotalPages(keyword, pageSize);
                        int totalCount = messageService.getSearchResultCount(keyword);
                        System.out.println("AdminServlet: Search found " + totalCount + " messages for keyword: " + keyword);
                        result.put("success", true);
                        result.put("data", messages);
                        result.put("totalPages", totalPages);
                        result.put("totalCount", totalCount);
                    } else {
                        // Empty keyword, return all messages
                        List<Message> messages = messageService.getMessages(page, pageSize);
                        int totalPages = messageService.getTotalPages(pageSize);
                        int totalCount = messageService.getTotalCount();
                        result.put("success", true);
                        result.put("data", messages);
                        result.put("totalPages", totalPages);
                        result.put("totalCount", totalCount);
                    }
                }
            } else if ("get".equals(action)) {
                if ("user".equals(type)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    User user = userService.getUserById(id);
                    result.put("success", user != null);
                    result.put("data", user);
                } else if ("message".equals(type)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Message message = messageService.getMessageById(id);
                    result.put("success", message != null);
                    result.put("data", message);
                }
            }
        } catch (Exception e) {
            System.err.println("AdminServlet doGet error: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        String jsonResponse = gson.toJson(result);
        System.out.println("AdminServlet: Response length=" + jsonResponse.length());
        out.print(jsonResponse);
        out.flush();
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        
        // Enhanced admin check with logging
        if (session == null) {
            System.err.println("AdminServlet POST: No session found");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }
        
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        Integer userId = (Integer) session.getAttribute("userId");
        
        System.out.println("AdminServlet doPost - isAdmin: " + isAdmin + ", userId: " + userId);

        if (isAdmin == null || !isAdmin) {
            System.err.println("AdminServlet POST: User is not admin");
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问");
            return;
        }

        String action = request.getParameter("action");
        String type = request.getParameter("type");
        
        System.out.println("AdminServlet POST: action=" + action + ", type=" + type);
        
        PrintWriter out = response.getWriter();

        Map<String, Object> result = new HashMap<>();

        try {
            if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean success = false;
                
                if ("user".equals(type)) {
                    success = userService.deleteUser(id);
                    System.out.println("AdminServlet: Delete user " + id + " - " + (success ? "success" : "failed"));
                } else if ("message".equals(type)) {
                    success = messageService.deleteMessage(id, userId, true);
                    System.out.println("AdminServlet: Delete message " + id + " - " + (success ? "success" : "failed"));
                }
                
                result.put("success", success);
                result.put("message", success ? "删除成功" : "删除失败");
            } else if ("update".equals(action)) {
                if ("user".equals(type)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    String username = request.getParameter("username");
                    String email = request.getParameter("email");
                    boolean isAdminUser = "true".equals(request.getParameter("isAdmin"));
                    
                    System.out.println("AdminServlet: Update user " + id + " - username=" + username + ", isAdmin=" + isAdminUser);
                    
                    User user = userService.getUserById(id);
                    if (user != null) {
                        user.setUsername(username);
                        user.setEmail(email);
                        user.setAdmin(isAdminUser);
                        boolean success = userService.updateUser(user);
                        result.put("success", success);
                        result.put("message", success ? "更新成功" : "更新失败");
                    } else {
                        result.put("success", false);
                        result.put("message", "用户不存在");
                    }
                } else if ("message".equals(type)) {
                    int id = Integer.parseInt(request.getParameter("id"));
                    String content = request.getParameter("content");
                    
                    System.out.println("AdminServlet: Update message " + id);
                    
                    boolean success = messageService.updateMessage(id, content, null, userId, true);
                    result.put("success", success);
                    result.put("message", success ? "留言更新成功" : "留言更新失败");
                }
            } else if ("create".equals(action)) {
                if ("user".equals(type)) {
                    String username = request.getParameter("username");
                    String password = request.getParameter("password");
                    String email = request.getParameter("email");
                    boolean isAdminUser = "true".equals(request.getParameter("isAdmin"));
                    
                    System.out.println("AdminServlet: Create user - username=" + username + ", isAdmin=" + isAdminUser);
                    
                    boolean success = userService.register(username, password, email, isAdminUser);
                    result.put("success", success);
                    result.put("message", success ? "用户创建成功" : "用户创建失败（用户名可能已存在）");
                }
            }
        } catch (Exception e) {
            System.err.println("AdminServlet doPost error: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        String jsonResponse = gson.toJson(result);
        System.out.println("AdminServlet POST: Response length=" + jsonResponse.length());
        out.print(jsonResponse);
        out.flush();
        out.close();
    }
}
