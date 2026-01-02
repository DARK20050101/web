package com.messageboard.servlet;

import com.messageboard.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {
    private UserService userService = new UserService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");

        // Validate input
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "所有字段都必须填写");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "两次输入的密码不一致");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        if (password.length() < 6) {
            request.setAttribute("error", "密码长度至少为6个字符");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        // Register user
        boolean success = userService.register(username, password, email);
        if (success) {
            request.setAttribute("success", "注册成功！请登录");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "用户名已存在");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/register.jsp");
    }
}
