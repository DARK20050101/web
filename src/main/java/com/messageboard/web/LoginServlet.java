package com.messageboard.web;

import com.google.gson.Gson;
import com.messageboard.dao.UserDao;
import com.messageboard.model.User;
import com.messageboard.util.SecurityUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Servlet - handles user authentication
 */
@WebServlet("/login")
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
        String rememberMe = request.getParameter("rememberMe");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            User user = userDao.authenticate(username, password);
            
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("isAdmin", user.isAdmin());
                
                // Generate CSRF token for the session
                String csrfToken = SecurityUtil.generateToken();
                session.setAttribute("csrfToken", csrfToken);
                
                // Handle remember me
                if ("true".equals(rememberMe)) {
                    String sessionToken = SecurityUtil.generateSessionId();
                    // Session expires in 30 days
                    Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
                    
                    if (userDao.saveSessionToken(sessionToken, user.getId(), expiresAt)) {
                        Cookie cookie = new Cookie("sessionToken", sessionToken);
                        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                        cookie.setHttpOnly(true);
                        cookie.setPath(request.getContextPath());
                        // Set secure flag if request is over HTTPS
                        cookie.setSecure(request.isSecure());
                        response.addCookie(cookie);
                    }
                }
                
                result.put("success", true);
                result.put("username", user.getUsername());
                result.put("isAdmin", user.isAdmin());
                result.put("csrfToken", csrfToken);
            } else {
                result.put("success", false);
                result.put("error", "Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", "Server error: " + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Remove remember-me cookie
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("sessionToken".equals(cookie.getName())) {
                        try {
                            userDao.deleteSessionToken(cookie.getValue());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        cookie.setMaxAge(0);
                        cookie.setPath(request.getContextPath());
                        // Set secure flag if request is over HTTPS
                        cookie.setSecure(request.isSecure());
                        response.addCookie(cookie);
                        break;
                    }
                }
            }
            
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
