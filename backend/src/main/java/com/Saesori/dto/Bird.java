package com.Saesori.dto;

/**
 * 새 정보를 담는 데이터 전송 객체(DTO)입니다.
 */
public class Bird {
    private int id; // 새 고유 ID
    private String name; // 새 이름
    private String imageUrl; // 새 이미지 URL
    private String description; // 새 설명
    private String conditionType; // 획득 조건 유형 (예: post_count, like_count)
    private int conditionValue; // 획득 조건 값

    public Bird() {
    }

    public Bird(int id, String name, String imageUrl, String description, String conditionType, int conditionValue) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    // Getter 및 Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public int getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(int conditionValue) {
        this.conditionValue = conditionValue;
    }

    @Override
    public String toString() {
        return "Bird{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", imageUrl='" + imageUrl + "'" +
                ", description='" + description + "'" +
                ", conditionType='" + conditionType + "'" +
                ", conditionValue=" + conditionValue +
                '}';
    }
}
