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
        String sqlInsert = "INSERT INTO follows (follower_id, following_id) VALUES (?, ?)";
        String sqlUpdateFollower = "UPDATE users SET following_count = following_count + 1 WHERE id = ?";
        String sqlUpdateFollowing = "UPDATE users SET follower_count = follower_count + 1 WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtInsert = null;
        PreparedStatement stmtUpdateFollower = null;
        PreparedStatement stmtUpdateFollowing = null;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 팔로우 관계 추가
            stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, followerId);
            stmtInsert.setInt(2, followingId);
            stmtInsert.executeUpdate();

            // 2. 팔로워의 팔로잉 수 증가
            stmtUpdateFollower = conn.prepareStatement(sqlUpdateFollower);
            stmtUpdateFollower.setInt(1, followerId);
            stmtUpdateFollower.executeUpdate();

            // 3. 팔로잉 당하는 사람의 팔로워 수 증가
            stmtUpdateFollowing = conn.prepareStatement(sqlUpdateFollowing);
            stmtUpdateFollowing.setInt(1, followingId);
            stmtUpdateFollowing.executeUpdate();

            conn.commit(); // 트랜잭션 커밋
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // 오류 시 롤백
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            // 이미 팔로우한 경우 중복 항목 오류 처리
            if (e.getErrorCode() == 1062) { // MySQL 중복 항목 에러 코드
                System.err.println("User " + followerId + " is already following " + followingId);
            } else {
                System.err.println("Error adding follow relationship: " + e.getMessage());
            }
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(null, stmtUpdateFollowing);
            DBUtil.close(null, stmtUpdateFollower);
            DBUtil.close(conn, stmtInsert);
        }
    }

    /**
     * 두 사용자 간의 팔로우 관계를 제거합니다.
     * @param followerId 언팔로우하는 사용자 ID
     * @param followingId 언팔로우 대상 사용자 ID
     * @return 성공적으로 제거되었으면 true, 그렇지 않으면 false
     */
    public boolean removeFollow(int followerId, int followingId) {
        String sqlDelete = "DELETE FROM follows WHERE follower_id = ? AND following_id = ?";
        String sqlUpdateFollower = "UPDATE users SET following_count = following_count - 1 WHERE id = ?";
        String sqlUpdateFollowing = "UPDATE users SET follower_count = follower_count - 1 WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement stmtDelete = null;
        PreparedStatement stmtUpdateFollower = null;
        PreparedStatement stmtUpdateFollowing = null;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 팔로우 관계 삭제
            stmtDelete = conn.prepareStatement(sqlDelete);
            stmtDelete.setInt(1, followerId);
            stmtDelete.setInt(2, followingId);
            int rowsAffected = stmtDelete.executeUpdate();

            if (rowsAffected > 0) {
                // 2. 팔로워의 팔로잉 수 감소
                stmtUpdateFollower = conn.prepareStatement(sqlUpdateFollower);
                stmtUpdateFollower.setInt(1, followerId);
                stmtUpdateFollower.executeUpdate();

                // 3. 팔로잉 당하는 사람의 팔로워 수 감소
                stmtUpdateFollowing = conn.prepareStatement(sqlUpdateFollowing);
                stmtUpdateFollowing.setInt(1, followingId);
                stmtUpdateFollowing.executeUpdate();
                
                conn.commit(); // 트랜잭션 커밋
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // 오류 시 롤백
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error removing follow relationship: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(null, stmtUpdateFollowing);
            DBUtil.close(null, stmtUpdateFollower);
            DBUtil.close(conn, stmtDelete);
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
