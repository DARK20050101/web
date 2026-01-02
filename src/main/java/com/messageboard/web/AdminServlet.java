package com.messageboard.web;

import com.google.gson.Gson;
import com.messageboard.dao.UserDao;
import com.messageboard.model.User;
import com.messageboard.util.SecurityUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * Admin Servlet - handles admin operations including user management
 */
@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {
    
    private UserDao userDao;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        // Check admin authorization
        if (!isAdmin(request)) {
            result.put("success", false);
            result.put("error", "Unauthorized");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(result));
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            if ("/users".equals(pathInfo)) {
                // Get all users
                List<User> users = userDao.getAllUsers();
                List<Map<String, Object>> userList = new ArrayList<>();
                
                for (User user : users) {
                    userList.add(convertUserToMap(user));
                }
                
                result.put("success", true);
                result.put("users", userList);
            } else if (pathInfo != null && pathInfo.startsWith("/users/")) {
                // Get specific user
                String idStr = pathInfo.substring(7);
                int userId = Integer.parseInt(idStr);
                User user = userDao.getUserById(userId);
                
                if (user != null) {
                    result.put("success", true);
                    result.put("user", convertUserToMap(user));
                } else {
                    result.put("success", false);
                    result.put("error", "User not found");
                }
            } else if ("/stats".equals(pathInfo)) {
                // Get system statistics
                int userCount = userDao.getUserCount();
                result.put("success", true);
                result.put("userCount", userCount);
            } else {
                result.put("success", false);
                result.put("error", "Invalid endpoint");
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
        
        // Check admin authorization
        if (!isAdmin(request)) {
            result.put("success", false);
            result.put("error", "Unauthorized");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(result));
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            if ("/users".equals(pathInfo)) {
                // Create new user
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String email = request.getParameter("email");
                String isAdminStr = request.getParameter("isAdmin");
                
                if (username == null || username.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("error", "Username is required");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                if (password == null || password.trim().isEmpty()) {
                    result.put("success", false);
                    result.put("error", "Password is required");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                // Check if username already exists
                User existingUser = userDao.getUserByUsername(username);
                if (existingUser != null) {
                    result.put("success", false);
                    result.put("error", "Username already exists");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                boolean success = userDao.createUser(username, password, email);
                
                if (success && "true".equals(isAdminStr)) {
                    // Set admin status
                    User newUser = userDao.getUserByUsername(username);
                    if (newUser != null) {
                        userDao.updateUserAdminStatus(newUser.getId(), true);
                    }
                }
                
                result.put("success", success);
                result.put("message", success ? "User created successfully" : "Failed to create user");
            } else {
                result.put("success", false);
                result.put("error", "Invalid endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        
        // Check admin authorization
        if (!isAdmin(request)) {
            result.put("success", false);
            result.put("error", "Unauthorized");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(result));
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/users/")) {
                String idStr = pathInfo.substring(7);
                int userId = Integer.parseInt(idStr);
                
                // Get parameters
                String password = request.getParameter("password");
                String isAdminStr = request.getParameter("isAdmin");
                
                boolean updated = false;
                
                // Update password if provided
                if (password != null && !password.trim().isEmpty()) {
                    updated = userDao.updateUserPassword(userId, password);
                }
                
                // Update admin status if provided
                if (isAdminStr != null) {
                    boolean isAdmin = "true".equals(isAdminStr);
                    updated = userDao.updateUserAdminStatus(userId, isAdmin) || updated;
                }
                
                result.put("success", updated);
                result.put("message", updated ? "User updated successfully" : "Failed to update user");
            } else {
                result.put("success", false);
                result.put("error", "Invalid endpoint");
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
        
        // Check admin authorization
        if (!isAdmin(request)) {
            result.put("success", false);
            result.put("error", "Unauthorized");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(gson.toJson(result));
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/users/")) {
                String idStr = pathInfo.substring(7);
                int userId = Integer.parseInt(idStr);
                
                // Prevent deleting own account
                HttpSession session = request.getSession(false);
                if (session == null) {
                    result.put("success", false);
                    result.put("error", "Session not found");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                User currentUser = (User) session.getAttribute("user");
                if (currentUser == null) {
                    result.put("success", false);
                    result.put("error", "User not found in session");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                if (currentUser.getId() == userId) {
                    result.put("success", false);
                    result.put("error", "Cannot delete your own account");
                    response.getWriter().write(gson.toJson(result));
                    return;
                }
                
                boolean success = userDao.deleteUser(userId);
                result.put("success", success);
                result.put("message", success ? "User deleted successfully" : "Failed to delete user");
            } else {
                result.put("success", false);
                result.put("error", "Invalid endpoint");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    /**
     * Check if current user is admin
     */
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            return user != null && user.isAdmin();
        }
        return false;
    }

    /**
     * Convert User object to Map for JSON serialization
     */
    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", SecurityUtil.escapeHtml(user.getUsername()));
        map.put("email", user.getEmail() != null ? SecurityUtil.escapeHtml(user.getEmail()) : null);
        map.put("isAdmin", user.isAdmin());
        map.put("createdAt", user.getCreatedAt().getTime());
        return map;
    }
}
