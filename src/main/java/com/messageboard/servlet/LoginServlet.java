package com.messageboard.servlet;

import com.messageboard.model.User;
import com.messageboard.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String captcha = request.getParameter("captcha");
        String rememberMe = request.getParameter("rememberMe");

        HttpSession session = request.getSession();
        String sessionCaptcha = (String) session.getAttribute("captcha");

        // Validate captcha
        if (captcha == null || !captcha.equalsIgnoreCase(sessionCaptcha)) {
            request.setAttribute("error", "验证码错误");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Validate credentials
        User user = userService.login(username, password);
        if (user != null) {
            // Set session attributes
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("isAdmin", user.isAdmin());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Handle "Remember Me" functionality
            if ("on".equals(rememberMe)) {
                Cookie userCookie = new Cookie("username", username);
                userCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                response.addCookie(userCookie);
            }

            // Redirect based on user role
            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
            } else {
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } else {
            request.setAttribute("error", "用户名或密码错误");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}
