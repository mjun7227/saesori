package com.Saesori.dao;

import com.Saesori.dto.User;
import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // java.sql.Timestamp 임포트 확인
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    /**
     * 데이터베이스에 새 사용자를 등록합니다.
     * 비밀번호는 이 메서드를 호출하기 전에 해시 처리되어야 합니다.
     * 
     * @param user 사용자 정보가 담긴 User 객체
     * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (handle, password, nickname) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getHandle());
            stmt.setString(2, user.getPassword()); // 해시된 비밀번호
            stmt.setString(3, user.getNickname());

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
            System.err.println("Error adding user: " + e.getMessage());
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
     * handle로 사용자를 조회합니다.
     * 
     * @param handle 검색할 handle
     * @return User 객체 (존재하지 않으면 null)
     */
    public User getUserByHandle(String handle) {
        String sql = "SELECT id, handle, password, nickname, created_at, follower_count, following_count, posts_count, bio, profile_image_url FROM users WHERE handle = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, handle);
            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setHandle(rs.getString("handle"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setFollowerCount(rs.getInt("follower_count"));
                user.setFollowingCount(rs.getInt("following_count"));
                user.setPostsCount(rs.getInt("posts_count"));
                user.setBio(rs.getString("bio"));
                user.setProfileImageUrl(rs.getString("profile_image_url"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by handle: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * ID로 사용자를 조회합니다.
     * 
     * @param id 검색할 사용자 ID
     * @return User 객체 (존재하지 않으면 null)
     */
    public User getUserById(int id) {
        String sql = "SELECT id, handle, password, nickname, created_at, follower_count, following_count, posts_count, bio, profile_image_url FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setHandle(rs.getString("handle"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setFollowerCount(rs.getInt("follower_count"));
                user.setFollowingCount(rs.getInt("following_count"));
                user.setPostsCount(rs.getInt("posts_count"));
                user.setBio(rs.getString("bio"));
                user.setProfileImageUrl(rs.getString("profile_image_url"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * 데이터베이스의 사용자 정보를 업데이트합니다.
     * 
     * @param user 업데이트된 정보가 담긴 User 객체
     * @return 성공적으로 업데이트되었으면 true, 그렇지 않으면 false
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET handle = ?, nickname = ?, bio = ?, profile_image_url = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getHandle());
            stmt.setString(2, user.getNickname());
            stmt.setString(3, user.getBio());
            stmt.setString(4, user.getProfileImageUrl());
            stmt.setInt(5, user.getId());

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
            System.err.println("Error updating user: " + e.getMessage());
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
     * 데이터베이스에서 사용자를 삭제합니다.
     * 
     * @param id 삭제할 사용자 ID
     * @return 성공적으로 삭제되었으면 true, 그렇지 않으면 false
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

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
            System.err.println("Error deleting user: " + e.getMessage());
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
     * 사용자 검색 (handle 또는 nickname 기반, 단순 LIKE 검색)
     * 
     * @param q 검색어
     * @return User 리스트 (최대 50개)
     */
    public List<User> searchUsers(String q) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, handle, nickname, created_at, follower_count, following_count, posts_count, bio, profile_image_url FROM users WHERE handle LIKE ? OR nickname LIKE ? ORDER BY created_at DESC LIMIT 50";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            String like = "%" + q + "%";
            stmt.setString(1, like);
            stmt.setString(2, like);
            rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setHandle(rs.getString("handle"));
                user.setNickname(rs.getString("nickname"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                user.setFollowerCount(rs.getInt("follower_count"));
                user.setFollowingCount(rs.getInt("following_count"));
                user.setPostsCount(rs.getInt("posts_count"));
                user.setBio(rs.getString("bio"));
                user.setProfileImageUrl(rs.getString("profile_image_url"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return users;
    }
}
