package com.Saesori.dto;

import java.time.LocalDateTime;
import java.sql.Timestamp; // 데이터베이스 DATETIME 호환성을 위해 java.sql.Timestamp 사용

public class User {
    private int id;
    private String username;
    private String password; // 해시된 비밀번호
    private String nickname;
    private Timestamp createdAt;
    private int followerCount;
    private int followingCount;

    public User() {
    }

    public User(int id, String username, String password, String nickname, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.createdAt = createdAt;
    }

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", username='" + username + "'" +
               ", password='[PROTECTED]'" +
                ", nickname='" + nickname + "'" +
                ", createdAt=" + createdAt +
                ", followerCount=" + followerCount +
                ", followingCount=" + followingCount +
                '}';
    }
}
