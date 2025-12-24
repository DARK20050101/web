package com.messageboard.dao;

import com.messageboard.model.Message;
import com.messageboard.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Message operations with image_path support
 */
public class MessageDao {

    /**
     * Get all messages ordered by creation time (newest first)
     */
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, user_id, nickname, content, image_path, created_at FROM messages ORDER BY created_at DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                messages.add(extractMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Get messages with pagination
     */
    public List<Message> findWithPagination(int page, int pageSize) {
        List<Message> messages = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, user_id, nickname, content, image_path, created_at FROM messages ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(extractMessage(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Find message by ID
     */
    public Message findById(int id) {
        String sql = "SELECT id, user_id, nickname, content, image_path, created_at FROM messages WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractMessage(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create a new message with optional image
     */
    public boolean create(Message message) {
        String sql = "INSERT INTO messages (user_id, nickname, content, image_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (message.getUserId() != null) {
                stmt.setInt(1, message.getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, message.getNickname());
            stmt.setString(3, message.getContent());
            stmt.setString(4, message.getImagePath());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Delete message by ID
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM messages WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get total count of messages
     */
    public int count() {
        String sql = "SELECT COUNT(*) as total FROM messages";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extract Message object from ResultSet
     */
    private Message extractMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            message.setUserId(userId);
        }
        
        message.setNickname(rs.getString("nickname"));
        message.setContent(rs.getString("content"));
        message.setImagePath(rs.getString("image_path"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        return message;
    }
}
