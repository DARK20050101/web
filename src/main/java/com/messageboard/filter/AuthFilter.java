package com.messageboard.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Authentication Filter
 * Protects admin pages requiring authentication
 */
public class AuthFilter implements Filter {
    private static final List<String> PROTECTED_PATHS = Arrays.asList("/admin.jsp", "/admin");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());

        // Check if the requested path requires authentication
        boolean isProtected = PROTECTED_PATHS.stream().anyMatch(path::startsWith);

        if (isProtected) {
            HttpSession session = httpRequest.getSession(false);
            
            if (session == null || session.getAttribute("user") == null) {
                httpResponse.sendRedirect(contextPath + "/login.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
