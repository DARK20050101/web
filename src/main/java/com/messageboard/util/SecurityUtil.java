package com.messageboard.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtil {

    /**
     * Hash password using SHA-256
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    /**
     * Sanitize HTML to prevent XSS attacks
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;")
                    .replace("/", "&#x2F;");
    }

    /**
     * Generate CSRF token
     */
    public static String generateCSRFToken() {
        return hashPassword(String.valueOf(System.currentTimeMillis() + Math.random()));
    }

    /**
     * Validate CSRF token
     */
    public static boolean validateCSRFToken(String sessionToken, String requestToken) {
        return sessionToken != null && sessionToken.equals(requestToken);
    }

    /**
     * Validate input to prevent SQL injection (basic check)
     */
    public static boolean isValidInput(String input) {
        if (input == null) {
            return false;
        }
        // Check for common SQL injection patterns
        String[] sqlKeywords = {"--", ";", "/*", "*/", "xp_", "sp_", "exec", "execute", "select", "insert", "update", "delete", "drop", "create", "alter"};
        String lowerInput = input.toLowerCase();
        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                return false;
            }
        }
        return true;
    }
}
