package com.Saesori.dao;

import com.Saesori.dto.Post;
import com.Saesori.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

	/**
	 * 데이터베이스에 새 게시글을 추가합니다.
	 * 
	 * @param post 게시글 정보가 담긴 Post 객체
	 * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
	 */
	public boolean addPost(Post post) {
		String sql = "INSERT INTO posts (user_id, content) VALUES (?, ?)";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setString(2, post.getContent());

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding post: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.close(conn, stmt);
		}
	}

	/**
	 * 데이터베이스에서 모든 게시글을 조회합니다.
	 * 
	 * @return Post 객체 리스트
	 */
	public List<Post> getAllPosts() {
		List<Post> posts = new ArrayList<>();
		// 사용자 테이블과 조인하여 닉네임 가져오기
		String sql = "SELECT p.id, p.user_id, p.content, p.created_at, u.nickname " + "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id " + "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setNickname(rs.getString("nickname")); // 닉네임 설정
				posts.add(post);
			}
		} catch (SQLException e) {
			System.err.println("Error getting all posts: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return posts;
	}

	/**
	 * 특정 사용자가 작성한 게시글을 조회합니다.
	 * 
	 * @param userId 사용자 ID
	 * @return 사용자가 작성한 Post 객체 리스트
	 */
	public List<Post> getPostsByUserId(int userId) {
		List<Post> posts = new ArrayList<>();
		// 사용자 테이블과 조인하여 닉네임 가져오기
		String sql = "SELECT p.id, p.user_id, p.content, p.created_at, u.nickname " + "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id " + "WHERE p.user_id = ? " + "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setNickname(rs.getString("nickname")); // Set nickname
				posts.add(post);
			}
		} catch (SQLException e) {
			System.err.println("Error getting posts by user ID: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return posts;
	}

	/**
	 * ID로 단일 게시글을 조회합니다.
	 * 
	 * @param postId 게시글 ID
	 * @return Post 객체 (존재하지 않으면 null)
	 */
	public Post getPostById(int postId) {
		Post post = null;
		String sql = "SELECT id, user_id, content, created_at FROM posts WHERE id = ?"; // 인증 확인용이므로 닉네임 불필요 일반적으로
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, postId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
			}
		} catch (SQLException e) {
			System.err.println("Error getting post by ID: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return post;
	}

	/**
	 * 데이터베이스에서 게시글을 삭제합니다.
	 * 
	 * @param postId 삭제할 게시글 ID
	 * @return 성공적으로 삭제되었으면 true, 그렇지 않으면 false
	 */
	public boolean deletePost(int postId) {
		String sql = "DELETE FROM posts WHERE id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, postId);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error deleting post: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.close(conn, stmt);
		}
	}
}
