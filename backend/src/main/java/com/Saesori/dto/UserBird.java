package com.Saesori.dto;

import java.sql.Timestamp;

public class UserBird {
    private int userId;
    private int birdId;
    private Timestamp acquiredAt;

    public UserBird() {
    }

    public UserBird(int userId, int birdId, Timestamp acquiredAt) {
        this.userId = userId;
        this.birdId = birdId;
        this.acquiredAt = acquiredAt;
    }

    // Getter 및 Setter
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBirdId() {
        return birdId;
    }

    public void setBirdId(int birdId) {
        this.birdId = birdId;
    }

    public Timestamp getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(Timestamp acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    @Override
    public String toString() {
        return "UserBird{" +
               "userId=" + userId +
               ", birdId=" + birdId +
               ", acquiredAt=" + acquiredAt +
               '}';
    }
}
