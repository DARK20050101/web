package com.messageboard.util;

/**
 * HTML output utility for XSS prevention
 */
public class HtmlUtil {
    
    /**
     * Escape HTML special characters to prevent XSS
     * Replaces ALL occurrences of special characters
     * 
     * @param text The text to escape
     * @return HTML-escaped text
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        
        return text.replaceAll("&", "&amp;")
                   .replaceAll("<", "&lt;")
                   .replaceAll(">", "&gt;")
                   .replaceAll("\"", "&quot;")
                   .replaceAll("'", "&#x27;")
                   .replaceAll("/", "&#x2F;");
    }
}
