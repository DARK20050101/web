package com.messageboard.service;

import com.messageboard.dao.UserDAO;
import com.messageboard.model.User;
import com.messageboard.util.SecurityUtil;

import java.util.List;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(SecurityUtil.hashPassword(password))) {
            return user;
        }
        return null;
    }

    public boolean register(String username, String password, String email) {
        // Check if username already exists
        if (userDAO.findByUsername(username) != null) {
            return false;
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecurityUtil.hashPassword(password));
        user.setEmail(email);
        user.setAdmin(false);
        
        return userDAO.save(user);
    }

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public boolean updateUser(User user) {
        return userDAO.update(user);
    }

    public boolean deleteUser(int id) {
        return userDAO.delete(id);
    }
}
