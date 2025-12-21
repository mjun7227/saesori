package com.Saesori.dto;

import java.time.LocalDateTime;
import java.sql.Timestamp; // 데이터베이스 DATETIME 호환성을 위해 java.sql.Timestamp 사용

/**
 * 사용자 정보를 담는 데이터 전송 객체(DTO)입니다.
 */
public class User {
    private int id; // 사용자 고유 ID
    private String handle; // 사용자 핸들 (아이디)
    private String password; // 해시된 비밀번호
    private String nickname; // 사용자 닉네임
    private Timestamp createdAt; // 계정 생성 일시
    private int followerCount; // 팔로워 수
    private int followingCount; // 팔로잉 수
    private int postsCount; // 게시글 수
    private String bio; // 자기소개
    private String profileImageUrl; // 프로필 이미지 URL

    public User() {
    }

    public User(int id, String handle, String password, String nickname, Timestamp createdAt) {
        this.id = id;
        this.handle = handle;
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

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
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

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", handle='" + handle + "'" +
                ", password='[PROTECTED]'" +
                ", nickname='" + nickname + "'" +
                ", createdAt=" + createdAt +
                ", followerCount=" + followerCount +
                ", followingCount=" + followingCount +
                ", postsCount=" + postsCount +
                ", bio='" + bio + "'" +
                ", profileImageUrl='" + profileImageUrl + "'" +
                '}';
    }
}
