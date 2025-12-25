package com.messageboard.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String uri = httpRequest.getRequestURI();
        
        // Allow access to public resources
        if (uri.endsWith("/login.jsp") || uri.endsWith("/register.jsp") || 
            uri.endsWith("/login") || uri.endsWith("/register") ||
            uri.endsWith("/captcha") || uri.contains("/css/") || 
            uri.contains("/js/") || uri.contains("/images/") ||
            uri.endsWith("/index.jsp") || uri.endsWith("/") ||
            uri.endsWith("/message") || uri.endsWith("/messages")) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in for admin pages
        if (uri.contains("/admin/")) {
            if (session == null || session.getAttribute("user") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
                return;
            }
            
            Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            if (isAdmin == null || !isAdmin) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/index.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
