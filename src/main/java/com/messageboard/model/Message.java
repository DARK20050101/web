package com.messageboard.model;

import java.sql.Timestamp;

/**
 * Message model class representing a message board entry
 */
public class Message {
    private int id;
    private String content;
    private String author;
    private Integer userId;
    private String imagePath;
    private Timestamp createdAt;
    private String ipAddress;

    public Message() {
    }

    public Message(String content, String author, Integer userId, String imagePath) {
        this.content = content;
        this.author = author;
        this.userId = userId;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
