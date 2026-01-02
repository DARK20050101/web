package com.messageboard.filter;

import com.messageboard.util.SecurityUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CSRFFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true);

        String method = httpRequest.getMethod();
        
        // Generate CSRF token for session if not exists
        if (session.getAttribute("csrfToken") == null) {
            session.setAttribute("csrfToken", SecurityUtil.generateCSRFToken());
        }

        // Validate CSRF token for POST, PUT, DELETE requests
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || 
            "DELETE".equalsIgnoreCase(method)) {
            
            String uri = httpRequest.getRequestURI();
            // Skip CSRF validation for login and register
            if (!uri.endsWith("/login") && !uri.endsWith("/register") && !uri.endsWith("/captcha")) {
                String sessionToken = (String) session.getAttribute("csrfToken");
                String requestToken = httpRequest.getParameter("csrfToken");
                
                if (!SecurityUtil.validateCSRFToken(sessionToken, requestToken)) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
