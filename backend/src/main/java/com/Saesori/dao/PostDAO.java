package com.Saesori.dao;

import com.Saesori.dto.Post;
import com.Saesori.dto.User;
import com.Saesori.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    // --- 공통 매핑 메서드 (코드 중복 제거 및 오류 방지) ---

    private Post mapPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getInt("id"));
        post.setUserId(rs.getInt("user_id"));
        post.setContent(rs.getString("content"));
        post.setImageUrl(rs.getString("image_url"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setLikeCount(rs.getInt("like_count"));
        post.setNickname(rs.getString("nickname"));
        post.setHandle(rs.getString("handle"));
        post.setLiked(rs.getBoolean("is_liked"));
        post.setType(rs.getString("type"));
        post.setOriginalPostId(rs.getInt("original_post_id"));
        post.setReplyCount(rs.getInt("reply_count"));
        post.setProfileImageUrl(rs.getString("profile_image_url"));
        return post;
    }

    private Post mapOriginalPost(ResultSet rs) throws SQLException {
        int opId = rs.getInt("op_id");
        if (opId <= 0)
            return null;

        Post op = new Post();
        op.setId(opId);
        op.setUserId(rs.getInt("op_user_id"));
        op.setContent(rs.getString("op_content"));
        op.setImageUrl(rs.getString("op_image_url"));
        op.setCreatedAt(rs.getTimestamp("op_created_at"));
        op.setLikeCount(rs.getInt("op_like_count"));
        op.setType(rs.getString("op_type"));
        op.setNickname(rs.getString("op_nickname"));
        op.setHandle(rs.getString("op_handle"));
        op.setLiked(rs.getBoolean("op_is_liked"));
        op.setReplyCount(rs.getInt("op_reply_count"));
        op.setProfileImageUrl(rs.getString("op_profile_image_url"));
        return op;
    }

    // --- 공통 SQL 조각 ---

    private final String BASE_SELECT = "SELECT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, u.handle, u.profile_image_url, "
            +
            "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, " +
            "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, " +
            "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
            +
            "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, ou.handle AS op_handle, ou.profile_image_url AS op_profile_image_url, "
            +
            "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, " +
            "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count ";

    private final String BASE_JOINS = "FROM posts p " +
            "JOIN users u ON p.user_id = u.id " +
            "LEFT JOIN posts op ON p.original_post_id = op.id " +
            "LEFT JOIN users ou ON op.user_id = ou.id ";

    // --- 게시글 쓰기/삭제 로직 ---

    public boolean addPost(Post post) {
        String sql = "INSERT INTO posts (user_id, content, image_url) VALUES (?, ?, ?)";
        String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    PreparedStatement updatestmt = conn.prepareStatement(updateCountSql)) {
                stmt.setInt(1, post.getUserId());
                stmt.setString(2, post.getContent());
                stmt.setString(3, post.getImageUrl());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    updatestmt.setInt(1, post.getUserId());
                    updatestmt.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePost(int postId, int userId) {
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";
        String updateCountSql = "UPDATE users SET posts_count = posts_count - 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    PreparedStatement updatestmt = conn.prepareStatement(updateCountSql)) {
                stmt.setInt(1, postId);
                stmt.setInt(2, userId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    updatestmt.setInt(1, userId);
                    updatestmt.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- 리포스트 / 인용 / 답글 추가 로직 ---

    public boolean rePost(Post post) {
        String sql = "INSERT INTO posts (user_id, type, original_post_id) VALUES (?, 'REPOST', ?)";
        String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    PreparedStatement updatestmt = conn.prepareStatement(updateCountSql)) {
                stmt.setInt(1, post.getUserId());
                stmt.setInt(2, post.getOriginalPostId());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    updatestmt.setInt(1, post.getUserId());
                    updatestmt.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addQuote(Post post) {
        String sql = "INSERT INTO posts (user_id, content, type, original_post_id) VALUES (?, ?, 'QUOTE', ?)";
        String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    PreparedStatement updatestmt = conn.prepareStatement(updateCountSql)) {
                stmt.setInt(1, post.getUserId());
                stmt.setString(2, post.getContent());
                stmt.setInt(3, post.getOriginalPostId());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    updatestmt.setInt(1, post.getUserId());
                    updatestmt.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addReply(Post post) {
        String sql = "INSERT INTO posts (user_id, content, type, original_post_id) VALUES (?, ?, 'REPLY', ?)";
        String updateCountSql = "UPDATE users SET posts_count = posts_count + 1 WHERE id = ?";
        try (Connection conn = DBUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                    PreparedStatement updatestmt = conn.prepareStatement(updateCountSql)) {
                stmt.setInt(1, post.getUserId());
                stmt.setString(2, post.getContent());
                stmt.setInt(3, post.getOriginalPostId());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    updatestmt.setInt(1, post.getUserId());
                    updatestmt.executeUpdate();
                }
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasUserReposted(int userId, int originalPostId) {
        String sql = "SELECT COUNT(*) FROM posts WHERE user_id = ? AND original_post_id = ? AND type = 'REPOST'";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, originalPostId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- 게시글 조회 로직 ---

    public List<Post> getAllPosts(int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = BASE_SELECT + BASE_JOINS + "WHERE p.type != 'REPLY' ORDER BY p.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public Post getPostById(int postId, int currentUserId) {
        String sql = BASE_SELECT + BASE_JOINS + "WHERE p.id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Post getPostById(int postId) {
        return getPostById(postId, 0);
    }

    public List<Post> getPostsByUserId(int userId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = BASE_SELECT + BASE_JOINS + "WHERE p.user_id = ? AND p.type != 'REPLY' ORDER BY p.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<Post> searchPosts(String q, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = BASE_SELECT + BASE_JOINS
                + "WHERE p.type != 'REPLY' AND p.content LIKE ? ORDER BY p.created_at DESC LIMIT 50";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setString(3, "%" + q + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public List<Post> getFollowingTimeline(int userId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id, p.user_id, p.content, p.image_url, p.created_at, p.like_count, p.type, p.original_post_id, u.nickname, u.handle, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = p.id AND user_id = ?) > 0 AS is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = p.id AND type = 'REPLY') AS reply_count, "
                + "op.id AS op_id, op.user_id AS op_user_id, op.content AS op_content, op.image_url AS op_image_url, op.created_at AS op_created_at, "
                + "op.like_count AS op_like_count, op.type AS op_type, ou.nickname AS op_nickname, ou.handle AS op_handle, "
                + "(SELECT COUNT(*) FROM likes WHERE post_id = op.id AND user_id = ?) > 0 AS op_is_liked, "
                + "(SELECT COUNT(*) FROM posts WHERE original_post_id = op.id AND type = 'REPLY') AS op_reply_count "
                + BASE_JOINS
                + "LEFT JOIN follows f ON p.user_id = f.following_id AND f.follower_id = ? "
                + "WHERE (f.follower_id = ? OR p.user_id = ?) AND p.type != 'REPLY' "
                + "ORDER BY p.created_at DESC";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            stmt.setInt(4, currentUserId);
            stmt.setInt(5, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    // --- 스레드 관련 재귀 쿼리 (Ancestors & Descendants) ---

    public List<Post> getAncestors(int postId, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "WITH RECURSIVE ancestor_posts AS ( "
                + "  SELECT id, original_post_id, type, 0 as depth FROM posts WHERE id = ? "
                + "  UNION ALL "
                + "  SELECT p.id, p.original_post_id, p.type, ap.depth + 1 "
                + "  FROM posts p "
                + "  INNER JOIN ancestor_posts ap ON p.id = ap.original_post_id "
                + "  WHERE ap.type = 'REPLY' "
                + ") "
                + BASE_SELECT
                + "FROM posts p "
                + "JOIN ancestor_posts ap ON p.id = ap.id "
                + "JOIN users u ON p.user_id = u.id "
                + "LEFT JOIN posts op ON p.original_post_id = op.id "
                + "LEFT JOIN users ou ON op.user_id = ou.id "
                + "WHERE p.id != ? "
                + "ORDER BY ap.depth DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            stmt.setInt(4, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

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
                + BASE_SELECT
                + "FROM posts p "
                + "JOIN descendant_posts dp ON p.id = dp.id "
                + "JOIN users u ON p.user_id = u.id "
                + "LEFT JOIN posts op ON p.original_post_id = op.id "
                + "LEFT JOIN users ou ON op.user_id = ou.id "
                + "ORDER BY dp.depth ASC, p.created_at ASC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Post post = mapPost(rs);
                    post.setOriginalPost(mapOriginalPost(rs));
                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    // --- 참여 유저 조회 ---

    public List<User> getLikedUsers(int postId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.nickname, u.handle, u.bio, u.profile_image_url FROM users u "
                + "JOIN likes l ON u.id = l.user_id WHERE l.post_id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNickname(rs.getString("nickname"));
                    user.setHandle(rs.getString("handle"));
                    user.setBio(rs.getString("bio"));
                    user.setProfileImageUrl(rs.getString("profile_image_url"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getRepostedUsers(int postId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.id, u.nickname, u.handle, u.bio, u.profile_image_url FROM users u "
                + "JOIN posts p ON u.id = p.user_id WHERE p.original_post_id = ? AND p.type = 'REPOST'";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNickname(rs.getString("nickname"));
                    user.setHandle(rs.getString("handle"));
                    user.setBio(rs.getString("bio"));
                    user.setProfileImageUrl(rs.getString("profile_image_url"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}