package com.Saesori.dto;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 게시글 정보를 담는 데이터 전송 객체(DTO)입니다.
 */
public class Post {
    private int id; // 게시글 고유 ID
    private int userId; // 작성자 ID
    private String content; // 게시글 내용
    private Timestamp createdAt; // 작성 일시
    private String nickname; // 작성자 닉네임 (화면 표시용)
    private String handle; // 작성자 핸들 (화면 표시용)
    private int likeCount; // 좋아요 수
    private String type; // 게시글 유형 (ORIGINAL, REPOST, QUOTE, REPLY)
    private int originalPostId; // 원본 게시글 ID (리포스트/인용/답글인 경우)
    private boolean isLiked; // 현재 사용자가 좋아요를 눌렀는지 여부
    private Post originalPost; // 원본 게시글 객체
    private int replyCount; // 답글 수
    private String imageUrl; // 첨부 이미지 URL
    private String profileImageUrl; // 작성자 프로필 이미지 URL

    public Post() {
    }

    public Post(int id, int userId, String content, Timestamp createdAt, int originalPostId) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.originalPostId = originalPostId;

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

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    @JsonProperty("isLiked")
    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOriginalPostId() {
        return originalPostId;
    }

    public void setOriginalPostId(int originalPostId) {
        this.originalPostId = originalPostId;
    }

    public Post getOriginalPost() {
        return originalPost;
    }

    public void setOriginalPost(Post originalPost) {
        this.originalPost = originalPost;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Override
    public String toString() {
        return "Post{"
                + "id=" + id + ","
                + "userId=" + userId + ","
                + "nickname='" + nickname + "',"
                + "handle='" + handle + "',"
                + "content='" + content + "',"
                + "createdAt=" + createdAt + ","
                + "likeCount=" + likeCount + ","
                + "isLiked=" + isLiked + ","
                + "profileImageUrl='" + profileImageUrl + "'"
                + '}';
    }
}
