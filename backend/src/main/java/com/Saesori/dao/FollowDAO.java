package com.Saesori.dao;

import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FollowDAO {

    /**
     * 두 사용자 간의 팔로우 관계를 생성합니다.
     * @param followerId 팔로우하는 사용자 ID
     * @param followingId 팔로우 대상 사용자 ID
     * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
     */
    public boolean addFollow(int followerId, int followingId) {
        String sql = "INSERT INTO follows (follower_id, following_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // 이미 팔로우한 경우 중복 항목 오류 처리
            if (e.getErrorCode() == 1062) { // MySQL 중복 항목 에러 코드
                System.err.println("User " + followerId + " is already following " + followingId);
            } else {
                System.err.println("Error adding follow relationship: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 두 사용자 간의 팔로우 관계를 제거합니다.
     * @param followerId 언팔로우하는 사용자 ID
     * @param followingId 언팔로우 대상 사용자 ID
     * @return 성공적으로 제거되었으면 true, 그렇지 않으면 false
     */
    public boolean removeFollow(int followerId, int followingId) {
        String sql = "DELETE FROM follows WHERE follower_id = ? AND following_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing follow relationship: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 사용자가 다른 사용자를 팔로우하고 있는지 확인합니다.
     * @param followerId 팔로우하는지 확인할 사용자 ID
     * @param followingId 팔로우 대상 ID
     * @return 팔로우 중이면 true, 아니면 false
     */
    public boolean isFollowing(int followerId, int followingId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ? AND following_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, followerId);
            stmt.setInt(2, followingId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking follow relationship: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return false;
    }

    /**
     * 특정 사용자가 팔로우하는 사용자 수를 반환합니다.
     * @param userId 사용자 ID
     * @return 팔로잉 수
     */
    public int getFollowingCount(int userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE follower_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting following count: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return 0;
    }

    /**
     * 특정 사용자의 팔로워 수를 반환합니다.
     * @param userId 사용자 ID
     * @return 팔로워 수
     */
    public int getFollowerCount(int userId) {
        String sql = "SELECT COUNT(*) FROM follows WHERE following_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting follower count: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return 0;
    }
}
