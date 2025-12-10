package com.Saesori.dao;

import com.Saesori.dto.Post;
import com.Saesori.dto.User;
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
		String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement updatestmt = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setString(2, post.getContent());

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				updatestmt = conn.prepareStatement(updateCountSql);
				updatestmt.setInt(1, post.getUserId());
				updatestmt.executeUpdate();
			}
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
		return getAllPosts(0); // 로그인하지 않은 경우
	}

	public List<Post> getAllPosts(int currentUserId) {
		List<Post> posts = new ArrayList<>();
		// 사용자 테이블과 조인하여 닉네임 가져오기
		// LEFT JOIN with likes to check isLiked
		String sql = "SELECT p.id, p.user_id, p.content, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked " + "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id " + "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
				
				// 리포스트나 인용인 경우 원본 게시글 정보 가져오기
				if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
					Post originalPost = getPostById(post.getOriginalPostId());
					post.setOriginalPost(originalPost);
				}
				
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
		return getPostsByUserId(userId, 0);
	}

	public List<Post> getPostsByUserId(int targetUserId, int currentUserId) {
		List<Post> posts = new ArrayList<>();
		// 사용자 테이블과 조인하여 닉네임 가져오기
		String sql = "SELECT p.id, p.user_id, p.content, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked " + "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id " + "WHERE p.user_id = ? " + "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, targetUserId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
				
				// 리포스트나 인용인 경우 원본 게시글 정보 가져오기
				if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
					Post originalPost = getPostById(post.getOriginalPostId());
					post.setOriginalPost(originalPost);
				}
				
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
		String sql = "SELECT p.id, p.user_id, p.content, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname "
				+ "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id "
				+ "WHERE p.id = ?";
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
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
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
	public boolean deletePost(int postId, int userId) {
		String sql = "DELETE FROM posts WHERE id = ?";
		String updateCountSql = "UPDATE users SET posts_count = posts_count - 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement updatestmt = null;
		try {
			conn = DBUtil.getConnection();
			
			// 먼저 리포스트 삭제 (인용은 유지)
			deleteRepostsByOriginalId(postId);
			
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, postId);

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				updatestmt = conn.prepareStatement(updateCountSql);
				updatestmt.setInt(1, userId);
				updatestmt.executeUpdate();
			}
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error deleting post: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.close(conn, stmt);
		}
	}

	/**
	 * 원본 게시글이 삭제될 때 해당 게시글의 리포스트만 삭제합니다.
	 * 인용 게시글은 유지됩니다.
	 * 
	 * @param originalPostId 원본 게시글 ID
	 */
	public void deleteRepostsByOriginalId(int originalPostId) {
		String sql = "DELETE FROM posts WHERE original_post_id = ? AND type = 'REPOST'";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, originalPostId);
			int deleted = stmt.executeUpdate();
			if (deleted > 0) {
				System.out.println("Deleted " + deleted + " repost(s) of original post " + originalPostId);
			}
		} catch (SQLException e) {
			System.err.println("Error deleting reposts: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt);
		}
	}

	/**
	 * 데이터베이스에 새 재게시글을 추가합니다.
	 * 
	 * @param post 재게시글 정보가 담긴 Post 객체
	 * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
	 */
	public boolean rePost(Post post) {
		String sql = "INSERT INTO posts (user_id, original_post_id, type) VALUES (?, ?, ?)";
		String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement updatestmt = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setInt(2, post.getOriginalPostId());
			stmt.setString(3, post.getType());

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				updatestmt = conn.prepareStatement(updateCountSql);
				updatestmt.setInt(1, post.getUserId());
				updatestmt.executeUpdate();
			}
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
	 * 데이터베이스에 새 인용 게시글을 추가합니다.
	 * 
	 * @param post 게시글 정보가 담긴 Post 객체
	 * @return 성공적으로 추가되었으면 true, 그렇지 않으면 false
	 */
	public boolean addQuote(Post post) {
		String sql = "INSERT INTO posts (user_id, original_post_id, type, content) VALUES (?, ?, ?,?)";
		String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement updatestmt = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setInt(2, post.getOriginalPostId());
			stmt.setString(3, post.getType());
			stmt.setString(4, post.getContent());

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				updatestmt = conn.prepareStatement(updateCountSql);
				updatestmt.setInt(1, post.getUserId());
				updatestmt.executeUpdate();
			}
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding post: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.close(conn, stmt);
		}
	}
}
