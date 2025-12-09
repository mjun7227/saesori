package com.Saesori.dto;

import java.sql.Timestamp;

public class Post {
    private int id;
    private int userId;
    private String content;
    private Timestamp createdAt;
    private String nickname; // 화면 표시용 임시 필드
    private int likeCount;
    private String type;
    private int originalPostId;
    private boolean isLiked;
    private Post originalPost;

    public Post() {
    }

    public Post(int id, int userId, String content, Timestamp createdAt, int originalPostId) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.originalPostId=originalPostId;
        
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

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

	@Override
    public String toString() {
        return "Post{"
               + "id=" + id + ","
               + "userId=" + userId + ","
               + "nickname='" + nickname + "',"
               + "content='" + content + "',"
               + "createdAt=" + createdAt + ","
               + "likeCount=" + likeCount + ","
               + "isLiked=" + isLiked +
               '}';
    }
}
