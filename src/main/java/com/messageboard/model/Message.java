package com.messageboard.model;

import java.sql.Timestamp;

/**
 * Message model representing a message board entry
 */
public class Message {
    private int id;
    private Integer userId;
    private String nickname;
    private String content;
    private String imagePath;
    private Timestamp createdAt;

    public Message() {
    }

    public Message(int id, Integer userId, String nickname, String content, String imagePath, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.nickname = nickname;
        this.content = content;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
}
