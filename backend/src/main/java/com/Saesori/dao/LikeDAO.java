package com.Saesori.dao;

import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDAO {

    /**
     * Add a like for a post by a user.
     * This method adds a row to 'likes' table and increments 'like_count' in
     * 'posts' table.
     * Transactional.
     * 
     * @param postId
     * @param userId
     * @return true if successful
     */
    public boolean addLike(int postId, int userId) {
        Connection conn = null;
        PreparedStatement stmtInsert = null;
        PreparedStatement stmtUpdate = null;

        String sqlInsert = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)";
        String sqlUpdate = "UPDATE posts SET like_count = like_count + 1 WHERE id = ?";

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into likes
            stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, postId);
            stmtInsert.setInt(2, userId);
            int inserted = stmtInsert.executeUpdate();

            if (inserted > 0) {
                // 2. Update posts like_count
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
     * Remove a like for a post by a user.
     * This method removes a row from 'likes' table and decrements 'like_count' in
     * 'posts' table.
     * Transactional.
     * 
     * @param postId
     * @param userId
     * @return true if successful
     */
    public boolean removeLike(int postId, int userId) {
        Connection conn = null;
        PreparedStatement stmtDelete = null;
        PreparedStatement stmtUpdate = null;

        String sqlDelete = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        String sqlUpdate = "UPDATE posts SET like_count = like_count - 1 WHERE id = ?";

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete from likes
            stmtDelete = conn.prepareStatement(sqlDelete);
            stmtDelete.setInt(1, postId);
            stmtDelete.setInt(2, userId);
            int deleted = stmtDelete.executeUpdate();

            if (deleted > 0) {
                // 2. Update posts like_count
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
     * Check if a user likes a post.
     * 
     * @param postId
     * @param userId
     * @return true if liked
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
}
