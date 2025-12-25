package com.messageboard.filter;

import com.messageboard.dao.UserDao;
import com.messageboard.model.User;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Authentication Filter
 * Handles session and cookie-based authentication
 */
public class AuthFilter implements Filter {

    private UserDao userDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        userDao = new UserDao();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true);
        
        // Check if user is already logged in via session
        User user = (User) session.getAttribute("user");
        
        // If not logged in, check for remember-me cookie
        if (user == null) {
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("sessionToken".equals(cookie.getName())) {
                        try {
                            user = userDao.getUserBySessionToken(cookie.getValue());
                            if (user != null) {
                                session.setAttribute("user", user);
                                session.setAttribute("userId", user.getId());
                                session.setAttribute("username", user.getUsername());
                                session.setAttribute("isAdmin", user.isAdmin());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
