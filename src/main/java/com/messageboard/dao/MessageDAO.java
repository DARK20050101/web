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
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setUserId((Integer) rs.getObject("user_id"));
        message.setNickname(rs.getString("nickname"));
        message.setContent(rs.getString("content"));
        message.setImagePath(rs.getString("image_path"));
        message.setAnonymous(rs.getBoolean("is_anonymous"));
        message.setCreatedAt(rs.getTimestamp("created_at"));
        message.setUpdatedAt(rs.getTimestamp("updated_at"));
        return message;
    }
}
