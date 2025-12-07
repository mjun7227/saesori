package com.Saesori.controller;

import com.Saesori.dao.PostDAO;
import com.Saesori.dao.BirdDAO;
import com.Saesori.dao.UserBirdDAO;
import com.Saesori.dto.Post;
import com.Saesori.dto.Bird;
import com.Saesori.dto.UserBird;
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

@WebServlet(urlPatterns = {"/api/posts", "/api/posts/*"})
public class PostController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private PostDAO postDAO;
    private BirdDAO birdDAO;
    private UserBirdDAO userBirdDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        postDAO = new PostDAO();
        birdDAO = new BirdDAO();
        userBirdDAO = new UserBirdDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        System.out.println("getpost");
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // /api/posts - 모든 게시글 조회
                List<Post> posts = postDAO.getAllPosts();
                objectMapper.writeValue(response.getWriter(), posts);
                return;
            }

            String[] pathParts = pathInfo.split("/");
            
            // /api/posts/user/{userId} - 특정 사용자 게시글 조회
            if (pathParts.length == 3 && pathParts[1].equals("user")) { 
                try {
                    int userId = Integer.parseInt(pathParts[2]);
                    List<Post> posts = postDAO.getPostsByUserId(userId);
                    objectMapper.writeValue(response.getWriter(), posts);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // /api/posts
            if (pathInfo == null || pathInfo.equals("/")) {
                try {
                    // 세션 확인
                    jakarta.servlet.http.HttpSession session = request.getSession(false);
                    com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user") : null;

                    if (user == null) {
                        sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
                        return;
                    }

                    Post post = objectMapper.readValue(request.getReader(), Post.class);
                    // 세션에서 사용자 ID 설정
                    post.setUserId(user.getId());

                    if (postDAO.addPost(post)) {
                        checkAndAwardBirds(post.getUserId(), "post_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "게시글이 성공적으로 작성되었습니다.");
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "게시글 작성에 실패했습니다.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 게시글 데이터입니다.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid post POST endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                        com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user") : null;
                        
                        if (user == null) {
                             sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to delete a post.");
                             return;
                        }
                        
                        int requestingUserId = user.getId();
                        Post post = postDAO.getPostById(postId);
                        
                        if (post == null) {
                            sendError(response, HttpServletResponse.SC_NOT_FOUND, "Post not found.");
                            return;
                        }
                        
                        if (post.getUserId() != requestingUserId) {
                             sendError(response, HttpServletResponse.SC_FORBIDDEN, "You are not authorized to delete this post.");
                             return;
                        }

                        if (postDAO.deletePost(postId)) {
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
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    private void checkAndAwardBirds(int userId, String changedConditionType) {
        int currentConditionValue = 0;
        if ("post_count".equals(changedConditionType)) {
            currentConditionValue = postDAO.getPostsByUserId(userId).size();
        } else {
            return; // 게시글 로직만 처리
        }

        List<Bird> eligibleBirds = birdDAO.getBirdsByCondition(changedConditionType, currentConditionValue);

        for (Bird bird : eligibleBirds) {
            if (!userBirdDAO.hasUserAcquiredBird(userId, bird.getId())) {
                UserBird userBird = new UserBird();
                userBird.setUserId(userId);
                userBird.setBirdId(bird.getId());
                if (userBirdDAO.addUserBird(userBird)) {
                    System.out.println("User " + userId + " acquired new bird: " + bird.getName());
                }
            }
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
