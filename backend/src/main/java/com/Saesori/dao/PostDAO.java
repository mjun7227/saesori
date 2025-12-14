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
		String sql = "INSERT INTO posts (user_id, content, image_url) VALUES (?, ?, ?)";
		String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement updatestmt = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setString(2, post.getContent());
            stmt.setString(3, post.getImageUrl());

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
		// LEFT JOIN으로 원본 게시글 정보도 함께 가져옴 (N+1 문제 해결)
		String sql = "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
				+ "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
				+ "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
				+ "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id "
				+ "LEFT JOIN posts op ON p.original_post_id = op.id "
				+ "LEFT JOIN users ou ON op.user_id = ou.id "
				+ "WHERE p.type != 'REPLY' "
				+ "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, currentUserId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
				
				// 리포스트나 인용인 경우 원본 게시글 정보 설정 (이미 JOIN으로 가져옴)
				if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
					int opId = rs.getInt("op_id");
					if (opId > 0) {
						Post originalPost = new Post();
						originalPost.setId(opId);
						originalPost.setUserId(rs.getInt("op_user_id"));
						originalPost.setContent(rs.getString("op_content"));
						originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
						originalPost.setLikeCount(rs.getInt("op_like_count"));
						originalPost.setType(rs.getString("op_type"));
						originalPost.setNickname(rs.getString("op_nickname"));
						originalPost.setLiked(rs.getBoolean("op_is_liked"));
						originalPost.setReplyCount(rs.getInt("op_reply_count")); // SQL alias
						post.setOriginalPost(originalPost);
					}
				}
				
				post.setReplyCount(rs.getInt("reply_count")); // SQL alias
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
		// LEFT JOIN으로 원본 게시글 정보도 함께 가져옴 (N+1 문제 해결)
		String sql = "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
				+ "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
				+ "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
				+ "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id "
				+ "LEFT JOIN posts op ON p.original_post_id = op.id "
				+ "LEFT JOIN users ou ON op.user_id = ou.id "
				+ "WHERE p.user_id = ? AND p.type != 'REPLY' "
				+ "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, currentUserId);
			stmt.setInt(3, targetUserId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
				
				// 리포스트나 인용인 경우 원본 게시글 정보 설정 (이미 JOIN으로 가져옴)
				if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
					int opId = rs.getInt("op_id");
					if (opId > 0) {
						Post originalPost = new Post();
						originalPost.setId(opId);
						originalPost.setUserId(rs.getInt("op_user_id"));
						originalPost.setContent(rs.getString("op_content"));
                        originalPost.setImageUrl(rs.getString("op_image_url"));
						originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
						originalPost.setLikeCount(rs.getInt("op_like_count"));
						originalPost.setType(rs.getString("op_type"));
						originalPost.setNickname(rs.getString("op_nickname"));
						originalPost.setLiked(rs.getBoolean("op_is_liked"));
						originalPost.setReplyCount(rs.getInt("op_reply_count")); // SQL alias
						post.setOriginalPost(originalPost);
					}
				}
				
				post.setReplyCount(rs.getInt("reply_count")); // SQL alias
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
		return getPostById(postId, 0);
	}

	/**
	 * ID로 단일 게시글을 조회합니다 (현재 사용자의 좋아요 상태 포함).
	 * 
	 * @param postId 게시글 ID
	 * @param currentUserId 현재 로그인한 사용자 ID (0이면 로그인 안 함)
	 * @return Post 객체 (존재하지 않으면 null)
	 */
	public Post getPostById(int postId, int currentUserId) {
		Post post = null;
		String sql = "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
				+ "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
				+ "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
				+ "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id "
				+ "LEFT JOIN posts op ON p.original_post_id = op.id "
				+ "LEFT JOIN users ou ON op.user_id = ou.id "
				+ "WHERE p.id = ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, currentUserId);
			stmt.setInt(3, postId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id"));
				post.setReplyCount(rs.getInt("reply_count")); // SQL alias

				// 리포스트나 인용인 경우 원본 게시글 정보 설정
				if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
					int opId = rs.getInt("op_id");
					if (opId > 0) {
						Post originalPost = new Post();
						originalPost.setId(opId);
						originalPost.setUserId(rs.getInt("op_user_id"));
						originalPost.setContent(rs.getString("op_content"));
                        originalPost.setImageUrl(rs.getString("op_image_url"));
						originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
						originalPost.setLikeCount(rs.getInt("op_like_count"));
						originalPost.setType(rs.getString("op_type"));
						originalPost.setNickname(rs.getString("op_nickname"));
						originalPost.setLiked(rs.getBoolean("op_is_liked"));
						originalPost.setReplyCount(rs.getInt("op_reply_count")); // SQL alias
						post.setOriginalPost(originalPost);
					}
				}
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
	 * 사용자가 특정 게시글을 이미 리포스트했는지 확인합니다.
	 * 
	 * @param userId 사용자 ID
	 * @param originalPostId 원본 게시글 ID
	 * @return 이미 리포스트했으면 true, 그렇지 않으면 false
	 */
	public boolean hasUserReposted(int userId, int originalPostId) {
		String sql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND original_post_id = ? AND type = 'REPOST'";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			stmt.setInt(2, originalPostId);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0;
			}
		} catch (SQLException e) {
			System.err.println("Error checking repost: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return false;
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

	/**
	 * 특정 게시글에 좋아요를 누른 사용자 목록을 조회합니다.
	 * 
	 * @param postId 게시글 ID
	 * @return 좋아요를 누른 User 객체 리스트
	 */
	public List<User> getLikedUsers(int postId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.nickname FROM users u "
				+ "JOIN likes l ON u.id = l.user_id "
				+ "WHERE l.post_id = ? ";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, postId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setNickname(rs.getString("nickname"));
				users.add(user);
			}
		} catch (SQLException e) {
			System.err.println("Error getting liked users: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return users;
	}

	/**
	 * 특정 게시글을 리트윗한 사용자 목록을 조회합니다.
	 * 
	 * @param postId 게시글 ID
	 * @return 리트윗한 User 객체 리스트
	 */
	public List<User> getRepostedUsers(int postId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.nickname, p.created_at FROM users u "
				+ "JOIN posts p ON u.id = p.user_id "
				+ "WHERE p.original_post_id = ? AND p.type = 'REPOST' "
				+ "ORDER BY p.created_at DESC";
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, postId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setNickname(rs.getString("nickname"));
				users.add(user);
			}
		} catch (SQLException e) {
			System.err.println("Error getting reposted users: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return users;
	}

	/**
	 * 데이터베이스에 답글을 추가합니다.
	 * 
	 * @param post 답글 정보가 담긴 Post 객체
	 * @return 성공여부
	 */
	public boolean addReply(Post post) {
		String sql = "INSERT INTO posts (user_id, original_post_id, type, content, image_url) VALUES (?, ?, ?, ?, ?)";
		String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
		Connection conn = null;
		PreparedStatement updatestmt = null;
		PreparedStatement stmt = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, post.getUserId());
			stmt.setInt(2, post.getOriginalPostId());
			stmt.setString(3, "REPLY");
			stmt.setString(4, post.getContent());
            stmt.setString(5, post.getImageUrl());

			int rowsAffected = stmt.executeUpdate();
			if (rowsAffected > 0) {
				updatestmt = conn.prepareStatement(updateCountSql);
				updatestmt.setInt(1, post.getUserId());
				updatestmt.executeUpdate();
			}
			return rowsAffected > 0;
		} catch (SQLException e) {
			System.err.println("Error adding reply: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.close(conn, stmt);
		}
	}

	/**
	 * 특정 게시글에 달린 답글 목록을 조회합니다.
	 * 
	 * @param postId 게시글 ID
	 * @param currentUserId 현재 로그인한 사용자 ID
	 * @return 답글 List
	 */
	public List<Post> getReplies(int postId, int currentUserId) {
		List<Post> posts = new ArrayList<>();
		String sql = "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
				+ "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
				+ "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
				+ "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
				+ "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
				+ "FROM posts p "
				+ "JOIN users u ON p.user_id = u.id "
				+ "LEFT JOIN posts op ON p.original_post_id = op.id "
				+ "LEFT JOIN users ou ON op.user_id = ou.id "
				+ "WHERE p.original_post_id = ? AND p.type = 'REPLY' "
				+ "ORDER BY p.created_at ASC"; 
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, currentUserId);
			stmt.setInt(2, currentUserId);
			stmt.setInt(3, postId);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Post post = new Post();
				post.setId(rs.getInt("id"));
				post.setUserId(rs.getInt("user_id"));
				post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
				post.setCreatedAt(rs.getTimestamp("created_at"));
				post.setLikeCount(rs.getInt("like_count"));
				post.setNickname(rs.getString("nickname"));
				post.setLiked(rs.getBoolean("is_liked"));
				post.setType(rs.getString("type"));
				post.setOriginalPostId(rs.getInt("original_post_id")); // should be the original post we requested replues for
				post.setReplyCount(rs.getInt("reply_count")); // SQL 서브쿼리 별칭
				
				// If the original post details are needed, they would be set on a nested Post object
				// For example:
				// if (rs.getInt("op_id") != 0) {
				//     Post originalPost = new Post();
				//     originalPost.setId(rs.getInt("op_id"));
				//     originalPost.setUserId(rs.getInt("op_user_id"));
				//     originalPost.setContent(rs.getString("op_content"));
				//     originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
				//     originalPost.setLikeCount(rs.getInt("op_like_count"));
				//     originalPost.setType(rs.getString("op_type"));
				//     originalPost.setNickname(rs.getString("op_nickname"));
				//     originalPost.setLiked(rs.getBoolean("op_is_liked"));
				//     originalPost.setReplyCount(rs.getInt("op_reply_count"));
				//     post.setOriginalPost(originalPost); // Assuming Post has a setOriginalPost method
				// }
				
				posts.add(post);
			}
		} catch (SQLException e) {
			System.err.println("Error getting replies: " + e.getMessage());
			e.printStackTrace();
		} finally {
			DBUtil.close(conn, stmt, rs);
		}
		return posts;
	}

    /**
     * 재귀 쿼리(CTE)를 사용하여 특정 게시글의 모든 상위 게시글(스레드)을 조회합니다.
     * 
     * @param postId 조회할 게시글 ID
     * @param currentUserId 현재 사용자 ID
     * @return 상위 게시글 리스트 (최상위 -> 하위 순서)
     */
    public List<Post> getAncestors(int postId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        // 재귀 CTE를 사용하여 상위 게시글 조회
        // 수정: 자식 게시글(ap)의 타입이 'REPLY'인 경우에만 부모를 찾아 올라가도록 조건 추가
        String sql = "WITH RECURSIVE ancestor_posts AS ( "
                + "  SELECT id, original_post_id, type, 0 as depth FROM posts WHERE id = ? "
                + "  UNION ALL "
                + "  SELECT p.id, p.original_post_id, p.type, ap.depth + 1 "
                + "  FROM posts p "
                + "  INNER JOIN ancestor_posts ap ON p.id = ap.original_post_id "
                + "  WHERE ap.type = 'REPLY' " 
                + ") "
                + "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
                + "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
                + "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count, "
                + "ap.depth "
                + "FROM posts p "
                + "JOIN ancestor_posts ap ON p.id = ap.id "
                + "JOIN users u ON p.user_id = u.id "
                + "LEFT JOIN posts op ON p.original_post_id = op.id "
                + "LEFT JOIN users ou ON op.user_id = ou.id "
                + "WHERE p.id != ? "
                + "ORDER BY ap.depth DESC"; // 깊이가 깊은 것(최상위)부터 정렬

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postId); // Anchor CTE
            stmt.setInt(2, currentUserId); // is_liked check
            stmt.setInt(3, currentUserId); // op_is_liked check
            stmt.setInt(4, postId); // Exclude current post
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
                post.setReplyCount(rs.getInt("reply_count"));

                if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
                    int opId = rs.getInt("op_id");
                    if (opId > 0) {
                        Post originalPost = new Post();
                        originalPost.setId(opId);
                        originalPost.setUserId(rs.getInt("op_user_id"));
                        originalPost.setContent(rs.getString("op_content"));
                        originalPost.setImageUrl(rs.getString("op_image_url"));
                        originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
                        originalPost.setLikeCount(rs.getInt("op_like_count"));
                        originalPost.setType(rs.getString("op_type"));
                        originalPost.setNickname(rs.getString("op_nickname"));
                        originalPost.setLiked(rs.getBoolean("op_is_liked"));
                        originalPost.setReplyCount(rs.getInt("op_reply_count"));
                        post.setOriginalPost(originalPost);
                    }
                }
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("Error getting ancestors: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return posts;
    }

    /**
     * 재귀 쿼리(CTE)를 사용하여 특정 게시글의 모든 하위 게시글(답글)을 조회합니다.
     * 
     * @param postId 부모 게시글 ID
     * @param currentUserId 현재 사용자 ID
     * @return 하위 게시글 리스트 (답글의 답글 포함)
     */
    public List<Post> getDescendants(int postId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "WITH RECURSIVE descendant_posts AS ( "
                + "  SELECT id, original_post_id, type, 1 as depth FROM posts WHERE original_post_id = ? AND type = 'REPLY' "
                + "  UNION ALL "
                + "  SELECT p.id, p.original_post_id, p.type, dp.depth + 1 "
                + "  FROM posts p "
                + "  INNER JOIN descendant_posts dp ON p.original_post_id = dp.id "
                + "  WHERE p.type = 'REPLY' "
                + ") "
                + "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
                + "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
                + "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count, "
                + "dp.depth "
                + "FROM posts p "
                + "JOIN descendant_posts dp ON p.id = dp.id "
                + "JOIN users u ON p.user_id = u.id "
                + "LEFT JOIN posts op ON p.original_post_id = op.id "
                + "LEFT JOIN users ou ON op.user_id = ou.id "
                + "ORDER BY dp.depth ASC, p.created_at ASC"; 

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, postId); // Anchor CTE start
            stmt.setInt(2, currentUserId); // is_liked check
            stmt.setInt(3, currentUserId); // op_is_liked check
            rs = stmt.executeQuery();

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                post.setLikeCount(rs.getInt("like_count"));
                post.setNickname(rs.getString("nickname"));
                post.setLiked(rs.getBoolean("is_liked"));
                post.setType(rs.getString("type"));
                post.setOriginalPostId(rs.getInt("original_post_id"));
                post.setReplyCount(rs.getInt("reply_count"));

                if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
                    int opId = rs.getInt("op_id");
                    if (opId > 0) {
                        Post originalPost = new Post();
                        originalPost.setId(opId);
                        originalPost.setUserId(rs.getInt("op_user_id"));
                        originalPost.setContent(rs.getString("op_content"));
                        originalPost.setImageUrl(rs.getString("op_image_url"));
                        originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
                        originalPost.setLikeCount(rs.getInt("op_like_count"));
                        originalPost.setType(rs.getString("op_type"));
                        originalPost.setNickname(rs.getString("op_nickname"));
                        originalPost.setLiked(rs.getBoolean("op_is_liked"));
                        originalPost.setReplyCount(rs.getInt("op_reply_count"));
                        post.setOriginalPost(originalPost);
                    }
                }
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("Error getting descendants: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return posts;
    }

    /**
     * 특정 사용자가 팔로우하는 사용자들의 게시글과 자기 자신의 게시글을 조회합니다 (팔로우 타임라인).
     * 
     * @param userId 팔로우 타임라인을 조회할 사용자 ID
     * @param currentUserId 현재 로그인한 사용자 ID (좋아요 상태 확인용, 0이면 로그인 안 함)
     * @return 팔로우한 사용자들과 자기 자신의 Post 객체 리스트
     */
    public List<Post> getFollowingTimeline(int userId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        // 팔로우한 사용자들의 게시글과 자기 자신의 게시글 조회 (답글 제외)
        String sql = "SELECT DISTINCT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
                + "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
                + "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
                + "FROM posts p "
                + "JOIN users u ON p.user_id = u.id "
                + "LEFT JOIN follows f ON p.user_id = f.following_id AND f.follower_id = ? "
                + "LEFT JOIN posts op ON p.original_post_id = op.id "
                + "LEFT JOIN users ou ON op.user_id = ou.id "
                + "WHERE (f.follower_id = ? OR p.user_id = ?) AND p.type != 'REPLY' "
                + "ORDER BY p.created_at DESC";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserId(rs.getInt("user_id"));
                post.setContent(rs.getString("content"));
                post.setImageUrl(rs.getString("image_url"));
                post.setCreatedAt(rs.getTimestamp("created_at"));
                post.setLikeCount(rs.getInt("like_count"));
                post.setNickname(rs.getString("nickname"));
                post.setLiked(rs.getBoolean("is_liked"));
                post.setType(rs.getString("type"));
                post.setOriginalPostId(rs.getInt("original_post_id"));
                
                // 리포스트나 인용인 경우 원본 게시글 정보 설정
                if (("REPOST".equals(post.getType()) || "QUOTE".equals(post.getType())) && post.getOriginalPostId() > 0) {
                    int opId = rs.getInt("op_id");
                    if (opId > 0) {
                        Post originalPost = new Post();
                        originalPost.setId(opId);
                        originalPost.setUserId(rs.getInt("op_user_id"));
                        originalPost.setContent(rs.getString("op_content"));
                        originalPost.setImageUrl(rs.getString("op_image_url"));
                        originalPost.setCreatedAt(rs.getTimestamp("op_created_at"));
                        originalPost.setLikeCount(rs.getInt("op_like_count"));
                        originalPost.setType(rs.getString("op_type"));
                        originalPost.setNickname(rs.getString("op_nickname"));
                        originalPost.setLiked(rs.getBoolean("op_is_liked"));
                        originalPost.setReplyCount(rs.getInt("op_reply_count"));
                        post.setOriginalPost(originalPost);
                    }
                }
                
                post.setReplyCount(rs.getInt("reply_count"));
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("Error getting following timeline: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.close(conn, stmt, rs);
        }
        return posts;
    }
}
