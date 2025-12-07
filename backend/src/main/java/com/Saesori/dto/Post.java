package com.Saesori.dto;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int userId;
    private String content;
    private Timestamp createdAt;
    private String nickname; // 화면 표시용 임시 필드

    public Post() {
    }

    public Post(int id, int userId, String content, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Post{"
               + "id=" + id + ","
               + "userId=" + userId + ","
               + "nickname='" + nickname + "',"
               + "content='" + content + "'"
               + "createdAt=" + createdAt + 
               '}';
    }
}
