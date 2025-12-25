package com.messageboard.service;

import com.messageboard.dao.MessageDAO;
import com.messageboard.model.Message;
import com.messageboard.util.SecurityUtil;

import java.util.List;

public class MessageService {
    private MessageDAO messageDAO = new MessageDAO();

    public boolean createMessage(Message message) {
        // Sanitize content to prevent XSS
        message.setContent(SecurityUtil.sanitizeHtml(message.getContent()));
        message.setNickname(SecurityUtil.sanitizeHtml(message.getNickname()));
        
        return messageDAO.save(message);
    }

    public boolean updateMessage(int messageId, String content, String imagePath, int userId, boolean isAdmin) {
        Message message = messageDAO.findById(messageId);
        if (message == null) {
            return false;
        }
        
        // Check permission: only message owner or admin can update
        if (!isAdmin && message.getUserId() != null && message.getUserId() != userId) {
            return false;
        }
        
        message.setContent(SecurityUtil.sanitizeHtml(content));
        if (imagePath != null) {
            message.setImagePath(imagePath);
        }
        
        return messageDAO.update(message);
    }

    public boolean deleteMessage(int messageId, int userId, boolean isAdmin) {
        Message message = messageDAO.findById(messageId);
        if (message == null) {
            return false;
        }
        
        // Check permission: only message owner or admin can delete
        if (!isAdmin && message.getUserId() != null && message.getUserId() != userId) {
            return false;
        }
        
        return messageDAO.delete(messageId);
    }

    public Message getMessageById(int id) {
        return messageDAO.findById(id);
    }

    public List<Message> getMessages(int page, int pageSize) {
        return messageDAO.findAll(page, pageSize);
    }

    public int getTotalPages(int pageSize) {
        int totalCount = messageDAO.getTotalCount();
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public int getTotalCount() {
        return messageDAO.getTotalCount();
    }
}
