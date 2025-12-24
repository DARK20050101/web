package com.messageboard.web;

import com.messageboard.dao.UserDao;
import com.messageboard.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * AdminServlet - Handles admin operations
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private UserDao userDao;

    @Override
    public void init() throws ServletException {
        userDao = new UserDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check if user is admin
        User currentUser = (User) request.getSession().getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        
        // Get all users for admin panel
        List<User> users = userDao.findAll();
        request.setAttribute("users", users);
        
        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }
}
