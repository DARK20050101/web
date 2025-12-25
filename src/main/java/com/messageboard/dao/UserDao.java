package com.messageboard.dao;

import com.messageboard.model.User;
import com.messageboard.util.DBUtil;
import com.messageboard.util.SecurityUtil;

import java.sql.*;

/**
 * User Data Access Object
 */
public class UserDao {

    /**
     * Authenticate user with username and password
     */
    public User authenticate(String username, String password) throws SQLException {
        String hashedPassword = SecurityUtil.hashPassword(password);
        String sql = "SELECT id, username, email, is_admin, created_at FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT id, username, email, is_admin, created_at FROM users WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Get user by username
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, is_admin, created_at FROM users WHERE username = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Create new user
     */
    public boolean createUser(String username, String password, String email) throws SQLException {
        String hashedPassword = SecurityUtil.hashPassword(password);
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, email);
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Save remember-me session token
     */
    public boolean saveSessionToken(String sessionId, int userId, Timestamp expiresAt) throws SQLException {
        String sql = "INSERT INTO sessions (id, user_id, expires_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, expiresAt);
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get user by session token
     */
    public User getUserBySessionToken(String sessionId) throws SQLException {
        String sql = "SELECT u.id, u.username, u.email, u.is_admin, u.created_at " +
                     "FROM users u JOIN sessions s ON u.id = s.user_id " +
                     "WHERE s.id = ? AND s.expires_at > NOW()";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setAdmin(rs.getBoolean("is_admin"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Delete session token
     */
    public void deleteSessionToken(String sessionId) throws SQLException {
        String sql = "DELETE FROM sessions WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sessionId);
            stmt.executeUpdate();
        }
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() throws SQLException {
        String sql = "DELETE FROM sessions WHERE expires_at < NOW()";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }
}
