package com.messageboard.dao;

import com.messageboard.model.Message;
import com.messageboard.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Message Data Access Object
 */
public class MessageDao {

    /**
     * Create a new message with optional image
     */
    public boolean createMessage(String content, String author, Integer userId, String imagePath, String ipAddress) throws SQLException {
        String sql = "INSERT INTO messages (content, author, user_id, image_path, ip_address) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, content);
            stmt.setString(2, author);
            if (userId != null) {
                stmt.setInt(3, userId);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, imagePath);
            stmt.setString(5, ipAddress);
            
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all messages ordered by creation time (newest first)
     */
    public List<Message> getAllMessages() throws SQLException {
        return getAllMessages(100, 0);
    }

    /**
     * Get messages with pagination
     */
    public List<Message> getAllMessages(int limit, int offset) throws SQLException {
        String sql = "SELECT id, content, author, user_id, image_path, created_at, ip_address " +
                     "FROM messages ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setContent(rs.getString("content"));
                    message.setAuthor(rs.getString("author"));
                    message.setUserId((Integer) rs.getObject("user_id"));
                    message.setImagePath(rs.getString("image_path"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    message.setIpAddress(rs.getString("ip_address"));
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }

    /**
     * Get message by ID
     */
    public Message getMessageById(int id) throws SQLException {
        String sql = "SELECT id, content, author, user_id, image_path, created_at, ip_address " +
                     "FROM messages WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setContent(rs.getString("content"));
                    message.setAuthor(rs.getString("author"));
                    message.setUserId((Integer) rs.getObject("user_id"));
                    message.setImagePath(rs.getString("image_path"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    message.setIpAddress(rs.getString("ip_address"));
                    return message;
                }
            }
        }
        
        return null;
    }

    /**
     * Delete message by ID
     */
    public boolean deleteMessage(int id) throws SQLException {
        String sql = "DELETE FROM messages WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get total message count
     */
    public int getMessageCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM messages";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }

    /**
     * Get messages by user ID
     */
    public List<Message> getMessagesByUserId(int userId) throws SQLException {
        String sql = "SELECT id, content, author, user_id, image_path, created_at, ip_address " +
                     "FROM messages WHERE user_id = ? ORDER BY created_at DESC";
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setContent(rs.getString("content"));
                    message.setAuthor(rs.getString("author"));
                    message.setUserId((Integer) rs.getObject("user_id"));
                    message.setImagePath(rs.getString("image_path"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    message.setIpAddress(rs.getString("ip_address"));
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }

    /**
     * Search messages by keyword (searches in content and author fields)
     */
    public List<Message> searchMessages(String keyword) throws SQLException {
        // Sanitize keyword to prevent SQL injection through LIKE pattern
        String sanitizedKeyword = keyword.replace("\\", "\\\\")
                                        .replace("%", "\\%")
                                        .replace("_", "\\_");
        
        String sql = "SELECT id, content, author, user_id, image_path, created_at, ip_address " +
                     "FROM messages WHERE content LIKE ? OR author LIKE ? ORDER BY created_at DESC";
        
        List<Message> messages = new ArrayList<>();
        String searchPattern = "%" + sanitizedKeyword + "%";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setContent(rs.getString("content"));
                    message.setAuthor(rs.getString("author"));
                    message.setUserId((Integer) rs.getObject("user_id"));
                    message.setImagePath(rs.getString("image_path"));
                    message.setCreatedAt(rs.getTimestamp("created_at"));
                    message.setIpAddress(rs.getString("ip_address"));
                    messages.add(message);
                }
            }
        }
        
        return messages;
    }
}
