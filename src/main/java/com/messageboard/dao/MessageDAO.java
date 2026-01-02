package com.messageboard.dao;

import com.messageboard.model.Message;
import com.messageboard.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public boolean save(Message message) {
        String sql = "INSERT INTO messages (user_id, nickname, content, image_path, is_anonymous) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, message.getUserId());
            stmt.setString(2, message.getNickname());
            stmt.setString(3, message.getContent());
            stmt.setString(4, message.getImagePath());
            stmt.setBoolean(5, message.isAnonymous());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Message message) {
        String sql = "UPDATE messages SET nickname = ?, content = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getNickname());
            stmt.setString(2, message.getContent());
            stmt.setString(3, message.getImagePath());
            stmt.setInt(4, message.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    public Message findById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractMessageFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Message> findAll(int page, int pageSize) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        System.out.println("=== DAO Query Execution START ===");
        System.out.println("SQL: " + sql);
        System.out.println("Page: " + page + ", PageSize: " + pageSize);
        System.out.println("OFFSET: " + ((page - 1) * pageSize));
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            System.out.println("Database connection obtained: " + (conn != null ? "SUCCESS" : "FAILED"));
            
            if (conn != null) {
                System.out.println("Connection autoCommit: " + conn.getAutoCommit());
                System.out.println("Connection catalog: " + conn.getCatalog());
            }
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            
            System.out.println("About to execute query...");
            rs = stmt.executeQuery();
            System.out.println("Query executed, ResultSet obtained: " + (rs != null ? "SUCCESS" : "FAILED"));
            
            int count = 0;
            System.out.println("Starting to iterate through ResultSet...");
            
            while (rs.next()) {
                try {
                    count++;
                    System.out.println("Processing row #" + count);
                    Message msg = extractMessageFromResultSet(rs);
                    messages.add(msg);
                    System.out.println("Extracted message #" + count + ": id=" + msg.getId() + ", nickname=" + msg.getNickname());
                } catch (SQLException e) {
                    System.err.println("ERROR extracting message #" + count + " from ResultSet:");
                    e.printStackTrace();
                    // Continue to next record instead of breaking entire loop
                }
            }
            
            System.out.println("Finished iterating. Total messages extracted: " + count);
            System.out.println("Messages list size: " + messages.size());
            
        } catch (SQLException e) {
            System.err.println("CRITICAL ERROR in findAll query:");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        } finally {
            // Close resources manually to ensure proper cleanup
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources:");
                e.printStackTrace();
            }
        }
        
        System.out.println("=== DAO Query Execution END ===");
        System.out.println("Returning " + messages.size() + " messages");
        System.out.println("========================");
        
        return messages;
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM messages";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Message> searchMessages(String keyword, int page, int pageSize) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE content LIKE ? OR nickname LIKE ? OR id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            // Try to parse keyword as ID, if fails use 0
            int searchId = 0;
            try {
                searchId = Integer.parseInt(keyword);
            } catch (NumberFormatException e) {
                // Not a number, use 0
            }
            stmt.setInt(3, searchId);
            stmt.setInt(4, pageSize);
            stmt.setInt(5, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    public int getSearchResultCount(String keyword) {
        String sql = "SELECT COUNT(*) FROM messages WHERE content LIKE ? OR nickname LIKE ? OR id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            int searchId = 0;
            try {
                searchId = Integer.parseInt(keyword);
            } catch (NumberFormatException e) {
                // Not a number, use 0
            }
            stmt.setInt(3, searchId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        try {
            message.setId(rs.getInt("id"));
            message.setUserId((Integer) rs.getObject("user_id"));
            message.setNickname(rs.getString("nickname"));
            message.setContent(rs.getString("content"));
            message.setImagePath(rs.getString("image_path"));
            message.setAnonymous(rs.getBoolean("is_anonymous"));
            message.setCreatedAt(rs.getTimestamp("created_at"));
            message.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException e) {
            System.err.println("ERROR in extractMessageFromResultSet:");
            System.err.println("Trying to extract: id, user_id, nickname, content, image_path, is_anonymous, created_at, updated_at");
            System.err.println("Exception: " + e.getMessage());
            throw e; // Re-throw so caller can handle
        }
        return message;
    }
}
