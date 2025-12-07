package com.Saesori.dao;

import com.Saesori.dto.Bird;
import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BirdDAO {

    /**
     * 모든 새들을 데이터베이스에서 검색
     * @return 모든 새들의 리스트를 반환
     */
    public List<Bird> getAllBirds() {
        List<Bird> birds = new ArrayList<>();
        String sql = "SELECT id, name, image_url, description, condition_type, condition_value FROM birds";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Bird bird = new Bird();
                bird.setId(rs.getInt("id"));
                bird.setName(rs.getString("name"));
                bird.setImageUrl(rs.getString("image_url"));
                bird.setDescription(rs.getString("description"));
                bird.setConditionType(rs.getString("condition_type"));
                bird.setConditionValue(rs.getInt("condition_value"));
                birds.add(bird);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all birds: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return birds;
    }

    /**
     * ID로 특정한 새를 검색한다
     * @param id 검색하려는 새의 ID
     * @return 검색될 경우 새 오브젝트를 반환, 아닐경우 null
     */
    public Bird getBirdById(int id) {
        String sql = "SELECT id, name, image_url, description, condition_type, condition_value FROM birds WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Bird bird = new Bird();
                bird.setId(rs.getInt("id"));
                bird.setName(rs.getString("name"));
                bird.setImageUrl(rs.getString("image_url"));
                bird.setDescription(rs.getString("description"));
                bird.setConditionType(rs.getString("condition_type"));
                bird.setConditionValue(rs.getInt("condition_value"));
                return bird;
            }
        } catch (SQLException e) {
            System.err.println("Error getting bird by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 특정 조건 타입과 값에 일치하는 새들을 조회합니다.
     * 사용자 업적에 기반한 새 획득 로직에 사용됩니다.
     * @param conditionType 조건 타입 (예: "friend_count", "post_count")
     * @param conditionValue 조건 값 (예: 친구 5명이면 5)
     * @return 조건을 만족하는 Bird 객체 리스트
     */
    public List<Bird> getBirdsByCondition(String conditionType, int conditionValue) {
        List<Bird> birds = new ArrayList<>();
        String sql = "SELECT id, name, image_url, description, condition_type, condition_value FROM birds WHERE condition_type = ? AND condition_value <= ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, conditionType);
            stmt.setInt(2, conditionValue);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Bird bird = new Bird();
                bird.setId(rs.getInt("id"));
                bird.setName(rs.getString("name"));
                bird.setImageUrl(rs.getString("image_url"));
                bird.setDescription(rs.getString("description"));
                bird.setConditionType(rs.getString("condition_type"));
                bird.setConditionValue(rs.getInt("condition_value"));
                birds.add(bird);
            }
        } catch (SQLException e) {
            System.err.println("Error getting birds by condition: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return birds;
    }
}
