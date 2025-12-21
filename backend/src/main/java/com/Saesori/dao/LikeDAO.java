package com.Saesori.dao;

import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDAO {

    /**
     * 게시글에 좋아요를 추가합니다.
     * 'likes' 테이블에 행을 추가하고 'posts' 테이블의 'like_count'를 증가시킵니다.
     * 트랜잭션으로 처리됩니다.
     * 
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 성공 시 true
     */
    public boolean addLike(int postId, int userId) {
        Connection conn = null;
        PreparedStatement stmtInsert = null;
        PreparedStatement stmtUpdate = null;

        String sqlInsert = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)";
        String sqlUpdate = "UPDATE posts SET like_count = like_count + 1 WHERE id = ?";

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. likes 테이블에 좋아요 정보 삽입
            stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, postId);
            stmtInsert.setInt(2, userId);
            int inserted = stmtInsert.executeUpdate();

            if (inserted > 0) {
                // 2. posts 테이블의 like_count 증가
                stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, postId);
                stmtUpdate.executeUpdate();

                conn.commit(); // Commit transaction
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error adding like: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            DBUtil.close(null, stmtInsert); // DBUtil close helpers might vary, checking usage
            DBUtil.close(null, stmtUpdate);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 게시글의 좋아요를 취소합니다.
     * 'likes' 테이블에서 행을 삭제하고 'posts' 테이블의 'like_count'를 감소시킵니다.
     * 트랜잭션으로 처리됩니다.
     * 
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 성공 시 true
     */
    public boolean removeLike(int postId, int userId) {
        Connection conn = null;
        PreparedStatement stmtDelete = null;
        PreparedStatement stmtUpdate = null;

        String sqlDelete = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        String sqlUpdate = "UPDATE posts SET like_count = like_count - 1 WHERE id = ?";

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. likes 테이블에서 데이터 삭제
            stmtDelete = conn.prepareStatement(sqlDelete);
            stmtDelete.setInt(1, postId);
            stmtDelete.setInt(2, userId);
            int deleted = stmtDelete.executeUpdate();

            if (deleted > 0) {
                // 2. posts 테이블의 like_count 감소
                stmtUpdate = conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, postId);
                stmtUpdate.executeUpdate();

                conn.commit(); // Commit transaction
                return true;
            } else {
                conn.rollback();
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error removing like: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            DBUtil.close(null, stmtDelete);
            DBUtil.close(null, stmtUpdate);
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 사용자가 특정 게시글에 좋아요를 눌렀는지 확인합니다.
     * 
     * @param postId 게시글 ID
     * @param userId 사용자 ID
     * @return 좋아요를 눌렀으면 true
     */
    public boolean isLiked(int postId, int userId) {
        String sql = "SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking isLiked: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
    }

    /**
     * 사용자가 지금까지 누른 총 좋아요 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 총 좋아요 수
     */
    public int getTotalLikesGiven(int userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE user_id = ?";
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
            System.err.println("Error getting total likes given: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return 0;
    }
}
