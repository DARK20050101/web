package com.messageboard.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing utility using SHA-256
 * Note: In production, use BCrypt or Argon2 instead
 */
public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    
    /**
     * Hash a password with salt
     * @param password The plain text password
     * @return Base64 encoded hash
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a hash
     * @param password The plain text password
     * @param hash The hashed password to compare against
     * @return true if password matches
     */
    public static boolean verifyPassword(String password, String hash) {
        String hashedPassword = hashPassword(password);
        return MessageDigest.isEqual(hashedPassword.getBytes(), hash.getBytes());
    }
    
    /**
     * Generate a random salt
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
