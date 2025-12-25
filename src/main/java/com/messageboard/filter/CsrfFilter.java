package com.messageboard.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * CSRF Protection Filter
 * Validates CSRF token for all state-changing requests (POST, PUT, DELETE)
 */
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        
        // Only check CSRF for state-changing methods
        if ("POST".equalsIgnoreCase(method) || 
            "PUT".equalsIgnoreCase(method) || 
            "DELETE".equalsIgnoreCase(method)) {
            
            HttpSession session = httpRequest.getSession(false);
            String sessionToken = null;
            if (session != null) {
                sessionToken = (String) session.getAttribute("csrfToken");
            }
            
            String requestToken = httpRequest.getParameter("csrfToken");
            if (requestToken == null) {
                requestToken = httpRequest.getHeader("X-CSRF-Token");
            }
            
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Invalid CSRF token\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
