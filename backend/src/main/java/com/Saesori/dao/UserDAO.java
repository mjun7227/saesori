package com.Saesori.dao;

import com.Saesori.dto.User;
import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // Make sure to import java.sql.Timestamp

public class UserDAO {

    /**
     * 데이터베이스에 새 사용자를 등록합니다.
     * 비밀번호는 이 메서드를 호출하기 전에 해시 처리되어야 합니다.
     * @param user 사용자 정보가 담긴 User 객체
     * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, nickname) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // Hashed password
            stmt.setString(3, user.getNickname());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 사용자명으로 사용자를 조회합니다.
     * @param username 검색할 사용자명
     * @return User 객체 (존재하지 않으면 null)
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT id, username, password, nickname, created_at FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return null;
    }

    /**
     * ID로 사용자를 조회합니다.
     * @param id 검색할 사용자 ID
     * @return User 객체 (존재하지 않으면 null)
     */
    public User getUserById(int id) {
        String sql = "SELECT id, username, password, nickname, created_at FROM users WHERE id = ?";
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
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
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
     * @param user 업데이트된 정보가 담긴 User 객체
     * @return 성공적으로 업데이트되었으면 true, 그렇지 않으면 false
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, nickname = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getNickname());
            stmt.setInt(3, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }

    /**
     * 데이터베이스에서 사용자를 삭제합니다.
     * @param id 삭제할 사용자 ID
     * @return 성공적으로 삭제되었으면 true, 그렇지 않으면 false
     */
    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.close(conn, stmt);
        }
    }
}
