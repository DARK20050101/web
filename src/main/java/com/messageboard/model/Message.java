package com.messageboard.model;

import com.google.gson.annotations.SerializedName;
import java.sql.Timestamp;

public class Message {
    private int id;
    private Integer userId;
    private String nickname;
    private String content;
    private String imagePath;
    
    @SerializedName("anonymous")
    private boolean isAnonymous;
    
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Message() {
    }

    public Message(String nickname, String content, boolean isAnonymous) {
        this.nickname = nickname;
        this.content = content;
        this.isAnonymous = isAnonymous;
    }

    public Message(Integer userId, String nickname, String content, boolean isAnonymous) {
        this.userId = userId;
        this.nickname = nickname;
        this.content = content;
        this.isAnonymous = isAnonymous;
    }

    // Getters and Setters
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

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", content='" + content + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", isAnonymous=" + isAnonymous +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
