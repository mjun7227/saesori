package com.Saesori.controller;

import com.Saesori.dao.PostDAO;
import com.Saesori.dto.Post;
import com.Saesori.dto.User;
import com.Saesori.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = { "/api/posts", "/api/posts/*" })
public class PostController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private PostDAO postDAO;
    private BirdService birdService;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        postDAO = new PostDAO();
        birdService = new BirdService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            // Get current user for like status check
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;
            int currentUserId = (user != null) ? user.getId() : 0;

            if (pathInfo == null || pathInfo.equals("/")) {
                // /api/posts - 모든 게시글 조회
                List<Post> posts = postDAO.getAllPosts(currentUserId);
                objectMapper.writeValue(response.getWriter(), posts);
                return;
            }

            String[] pathParts = pathInfo.split("/");

            // /api/posts/following - 팔로우 타임라인 조회
            if (pathParts.length == 2 && pathParts[1].equals("following")) {
                if (user == null) {
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                    return;
                }
                try {
                    List<Post> posts = postDAO.getFollowingTimeline(user.getId(), currentUserId);
                    objectMapper.writeValue(response.getWriter(), posts);
                } catch (Exception e) {
                    sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "팔로우 타임라인 조회에 실패했습니다.");
                }
                return;
            }

            // /api/posts/user/{userId} - 특정 사용자 게시글 조회
            if (pathParts.length == 3 && pathParts[1].equals("user")) {
                try {
                    int userId = Integer.parseInt(pathParts[2]);
                    List<Post> posts = postDAO.getPostsByUserId(userId, currentUserId);
                    objectMapper.writeValue(response.getWriter(), posts);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            }
            // /api/posts/{postId}/likes - 좋아요 누른 사용자 목록 조회
            else if (pathParts.length == 3 && pathParts[2].equals("likes")) {
                try {
                    int postId = Integer.parseInt(pathParts[1]);
                    List<User> users = postDAO.getLikedUsers(postId);
                    objectMapper.writeValue(response.getWriter(), users);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
                }
            }
            // /api/posts/{postId}/reposts - 리트윗한 사용자 목록 조회
            else if (pathParts.length == 3 && pathParts[2].equals("reposts")) {
                try {
                    int postId = Integer.parseInt(pathParts[1]);
                    List<User> users = postDAO.getRepostedUsers(postId);
                    objectMapper.writeValue(response.getWriter(), users);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
                }
            }
            // /api/posts/{postId}/replies - 답글 목록 조회
            else if (pathParts.length == 3 && pathParts[2].equals("replies")) {
                try {
                    int postId = Integer.parseInt(pathParts[1]);
                    // 답글 조회 시 현재 사용자의 좋아요 상태도 확인할 수 있어야 하므로 currentUserId 전달
                    // 재귀적으로 모든 하위 답글(descendants)을 조회하도록 변경
                    List<Post> replies = postDAO.getDescendants(postId, currentUserId);
                    objectMapper.writeValue(response.getWriter(), replies);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
                }
            }
            // /api/posts/{postId} - 단일 게시글 조회
            else if (pathParts.length == 2) {
                try {
                    int postId = Integer.parseInt(pathParts[1]);
                    Post post = postDAO.getPostById(postId, currentUserId);
                    if (post == null) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");
                        return;
                    }
                    objectMapper.writeValue(response.getWriter(), post);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
                }
            }
            // /api/posts/{postId}/ancestors - 상위 스레드 조회
            else if (pathParts.length == 3 && pathParts[2].equals("ancestors")) {
                try {
                    int postId = Integer.parseInt(pathParts[1]);
                    List<Post> ancestors = postDAO.getAncestors(postId, currentUserId);
                    objectMapper.writeValue(response.getWriter(), ancestors);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID format.");
                }
            }
            else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // /api/posts
            if (pathInfo == null || pathInfo.equals("/")) {
                try {
                    // 세션 확인
                    jakarta.servlet.http.HttpSession session = request.getSession(false);
                    User user = (session != null) ? (User) session.getAttribute("user")
                            : null;

                    if (user == null) {
                        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        return;
                    }
         
                    Post post = objectMapper.readValue(request.getReader(), Post.class);
                    if (post.getContent() ==null || post.getContent().trim().isEmpty()) {
                    	sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시글 내용이 비어있습니다.");
                    	return;
                    }
                    // 세션에서 사용자 ID 설정
                    post.setUserId(user.getId());

                    if (postDAO.addPost(post)) {
                        // BirdService를 통해 로직 수행
                        birdService.checkAndAwardBirds(post.getUserId(), "post_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "게시글이 성공적으로 작성되었습니다.");
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시글 작성에 실패했습니다.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 데이터입니다.");
                }
            } 
            // 리트윗
            else if (pathInfo.endsWith("/repost")) {
            	try {
                    // 세션 확인
                    jakarta.servlet.http.HttpSession session = request.getSession(false);
                    com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user")
                            : null;

                    if (user == null) {
                        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        return;
                    }
         
                    Post post = objectMapper.readValue(request.getReader(), Post.class);
                    
                    // 리포스트는 content가 비어있어야 함
                    if (post.getContent() != null && !post.getContent().trim().isEmpty()) {
                    	sendError(response, HttpServletResponse.SC_BAD_REQUEST, "재게시는 내용을 포함할 수 없습니다.");
                    	return;
                    }
                    
                    // 원본 게시글 확인
                    Post originalPost = postDAO.getPostById(post.getOriginalPostId());
                    if (originalPost == null) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "원본 게시글을 찾을 수 없습니다.");
                        return;
                    }
                    
                    // 리포스트의 리포스트인 경우, 원본 게시글 ID를 사용
                    int targetOriginalId = post.getOriginalPostId();
                    if ("REPOST".equals(originalPost.getType()) && originalPost.getOriginalPostId() > 0) {
                        targetOriginalId = originalPost.getOriginalPostId();
                    }
                    
                    // 중복 리포스트 체크
                    if (postDAO.hasUserReposted(user.getId(), targetOriginalId)) {
                        sendError(response, HttpServletResponse.SC_CONFLICT, "이미 리트윗한 게시글입니다.");
                        return;
                    }
                    
                    // 세션에서 사용자 ID 설정
                    post.setUserId(user.getId());
                    post.setType("REPOST");
                    post.setOriginalPostId(targetOriginalId);
                    
                    if (postDAO.rePost(post)) {
                        // BirdService를 통해 로직 수행
                        birdService.checkAndAwardBirds(post.getUserId(), "post_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "게시글이 성공적으로 재게시되었습니다.");
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "재게시에 실패했습니다.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 데이터입니다.");
                }
            }
            //인용
            else if (pathInfo.endsWith("/quote")) {
            	try {
                    // 세션 확인
                    jakarta.servlet.http.HttpSession session = request.getSession(false);
                    com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user")
                            : null;

                    if (user == null) {
                        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        return;
                    }
         
                    Post post = objectMapper.readValue(request.getReader(), Post.class);
                    
                    if (post.getContent() ==null || post.getContent().trim().isEmpty()) {
                    	sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "잘못된 게시글 요청입니다.");
                    	return;
                    }
                    // 세션에서 사용자 ID 설정
                    post.setUserId(user.getId());
                    post.setType("QUOTE");
                    if (postDAO.addQuote(post)) {
                        // BirdService를 통해 로직 수행
                        birdService.checkAndAwardBirds(post.getUserId(), "post_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "게시글이 성공적으로 작성되었습니다.");
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시에 실패했습니다.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 데이터입니다.");
                }
            }
            // 답글
            else if (pathInfo.endsWith("/reply")) {
                try {
                    // 세션 확인
                    jakarta.servlet.http.HttpSession session = request.getSession(false);
                    com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user")
                            : null;

                    if (user == null) {
                        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        return;
                    }

                    Post post = objectMapper.readValue(request.getReader(), Post.class);
                    
                    if (post.getContent() == null || post.getContent().trim().isEmpty()) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "답글 내용을 입력해주세요.");
                        return;
                    }
                    
                    if (post.getOriginalPostId() <= 0) {
                         sendError(response, HttpServletResponse.SC_BAD_REQUEST, "원본 게시글 ID가 필요합니다.");
                         return;
                    }
                    
                    // 세션에서 사용자 ID 설정
                    post.setUserId(user.getId());
                    post.setType("REPLY");
                    
                    if (postDAO.addReply(post)) {
                        // BirdService 로직 (답글도 활동으로 인정한다면)
                        birdService.checkAndAwardBirds(post.getUserId(), "post_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "답글이 성공적으로 작성되었습니다.");
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "답글 작성에 실패했습니다.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청 데이터입니다.");
                }
            }
            else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post POST endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // /api/posts/{postId}
            if (pathInfo != null && !pathInfo.equals("/")) {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    try {
                        int postId = Integer.parseInt(pathParts[1]);

                        // 세션을 통한 인증/인가 확인
                        jakarta.servlet.http.HttpSession session = request.getSession(false);
                        com.Saesori.dto.User user = (session != null)
                                ? (com.Saesori.dto.User) session.getAttribute("user")
                                : null;

                        if (user == null) {
                            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                    "You must be logged in to delete a post.");
                            return;
                        }

                        int requestingUserId = user.getId();
                        Post post = postDAO.getPostById(postId);

                        if (post == null) {
                            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Post not found.");
                            return;
                        }

                        if (post.getUserId() != requestingUserId) {
                            sendError(response, HttpServletResponse.SC_FORBIDDEN,
                                    "You are not authorized to delete this post.");
                            return;
                        }

                        if (postDAO.deletePost(postId,requestingUserId)) {
                            sendJsonSuccess(response, HttpServletResponse.SC_OK, "Post deleted successfully.");
                        } else {
                            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete post.");
                        }
                    } catch (NumberFormatException e) { 
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post ID or User ID format.");
                    }
                } else {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post DELETE endpoint.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post DELETE endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.println(String.format("{\"error\": \"%s\"}", message));
        out.flush();
    }

    private void sendJsonSuccess(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.println(String.format("{\"message\": \"%s\"}", message));
        out.flush();
    }
}
