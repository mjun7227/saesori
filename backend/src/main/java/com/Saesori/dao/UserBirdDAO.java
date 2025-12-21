package com.Saesori.dao;

import com.Saesori.dto.Bird;
import com.Saesori.dto.UserBird;
import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserBirdDAO {

    /**
     * 사용자가 새를 획득했음을 기록합니다.
     * 
     * @param userBird 사용자와 새 ID가 담긴 UserBird 객체
     * @return 성공적으로 기록되었으면 true, 그렇지 않으면 false
     */
    public boolean addUserBird(UserBird userBird) {
        String sql = "INSERT INTO user_birds (user_id, bird_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userBird.getUserId());
            stmt.setInt(2, userBird.getBirdId());

            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            // 이미 새를 획득한 경우 중복 항목 오류 처리
            if (e.getErrorCode() == 1062) { // MySQL 중복 항목 에러 코드
                System.err.println("User " + userBird.getUserId() + " already acquired bird " + userBird.getBirdId());
            } else {
                System.err.println("Error adding user bird: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 특정 사용자가 획득한 모든 새를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 사용자가 획득한 Bird 객체 리스트
     */
    public List<Bird> getUserBirds(int userId) {
        List<Bird> birds = new ArrayList<>();
        String sql = "SELECT b.id, b.name, b.description, b.condition_type, b.condition_value " +
                "FROM user_birds ub JOIN birds b ON ub.bird_id = b.id WHERE ub.user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Bird bird = new Bird();
                bird.setId(rs.getInt("id"));
                bird.setName(rs.getString("name"));
                bird.setDescription(rs.getString("description"));
                bird.setConditionType(rs.getString("condition_type"));
                bird.setConditionValue(rs.getInt("condition_value"));
                birds.add(bird);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user birds for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return birds;
    }

    /**
     * 사용자가 특정 새를 이미 획득했는지 확인합니다.
     * 
     * @param userId 사용자 ID
     * @param birdId 새 ID
     * @return 획득했으면 true, 아니면 false
     */
    public boolean hasUserAcquiredBird(int userId, int birdId) {
        String sql = "SELECT COUNT(*) FROM user_birds WHERE user_id = ? AND bird_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, birdId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user " + userId + " acquired bird " + birdId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }
}
