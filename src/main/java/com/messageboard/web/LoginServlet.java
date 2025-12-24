package com.messageboard.web;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.messageboard.dao.UserDao;
import com.messageboard.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * LoginServlet - Handles user authentication
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String remember = request.getParameter("remember");
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            sendError(response, "Username and password are required");
            return;
        }
        
        // Find user
        User user = userDao.findByUsername(username);
        
        // SECURITY NOTE: This is a simple demo using plain text password comparison
        // In production, use PasswordUtil.verifyPassword() or BCrypt for hashed passwords
        if (user != null && user.getPassword().equals(password)) {
            // Login successful
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            
            // Set session timeout based on remember me
            if ("true".equals(remember)) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7 days
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Login successful");
            result.addProperty("username", user.getUsername());
            result.addProperty("isAdmin", user.isAdmin());
            response.getWriter().write(gson.toJson(result));
            
        } else {
            sendError(response, "Invalid username or password");
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("error", message);
        response.getWriter().write(gson.toJson(error));
    }
}
