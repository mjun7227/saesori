package com.Saesori.dto;

import java.sql.Timestamp;

/**
 * 사용자가 획득한 새 정보를 관리하는 데이터 전송 객체(DTO)입니다.
 */
public class UserBird {
    private int userId; // 사용자 고유 ID
    private int birdId; // 새 고유 ID
    private Timestamp acquiredAt; // 새를 획득한 일시

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
