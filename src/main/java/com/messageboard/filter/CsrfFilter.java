package com.messageboard.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF Protection Filter
 * Generates CSRF tokens for sessions and validates them on non-GET requests
 */
public class CsrfFilter implements Filter {
    private static final String CSRF_TOKEN_ATTR = "csrfToken";
    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true);

        // Generate CSRF token if not exists
        if (session.getAttribute(CSRF_TOKEN_ATTR) == null) {
            session.setAttribute(CSRF_TOKEN_ATTR, generateToken());
        }

        String method = httpRequest.getMethod();
        
        // Validate CSRF token for non-GET requests
        if (!"GET".equalsIgnoreCase(method) && !"OPTIONS".equalsIgnoreCase(method)) {
            String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTR);
            String requestToken = httpRequest.getHeader(CSRF_HEADER);
            
            // Also check parameter for form submissions
            if (requestToken == null) {
                requestToken = httpRequest.getParameter("csrfToken");
            }
            
            if (requestToken == null || !requestToken.equals(sessionToken)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    /**
     * Generate a secure random CSRF token
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
