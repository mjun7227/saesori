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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/api/posts", "/api/posts/*" })
public class PostController extends BaseController {
    private static final long serialVersionUID = 1L;
    private PostDAO postDAO;
    private BirdService birdService;

    @Override
    public void init() throws ServletException {
        super.init();
        postDAO = new PostDAO();
        birdService = new BirdService();
    }

    // --- 핵심 API 메서드 ---

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = (pathInfo == null || pathInfo.equals("/")) ? new String[0] : pathInfo.split("/");

        int currentUserId = getOptionalUserId(req);

        try {
            // 1. /api/posts - 전체 조회
            if (pathParts.length == 0) {
                sendJsonResponse(resp, postDAO.getAllPosts(currentUserId));
                return;
            }

            // 2. /api/posts/following - 팔로우 타임라인
            if (pathParts.length == 2 && "following".equals(pathParts[1])) {
                User user = getAuthenticatedUser(req, resp);
                if (user == null)
                    return;
                sendJsonResponse(resp, postDAO.getFollowingTimeline(currentUserId));
                return;
            }

            // 3. /api/posts/user/{userId} - 특정 유저 게시글
            if (pathParts.length == 3 && "user".equals(pathParts[1])) {
                int userId = Integer.parseInt(pathParts[2]);
                sendJsonResponse(resp, postDAO.getPostsByUserId(userId, currentUserId));
                return;
            }

            // 4. /api/posts/{postId}/... 서브 리소스 처리
            if (pathParts.length == 3) {
                handleSubResource(resp, pathParts[1], pathParts[2], currentUserId);
                return;
            }

            // 5. /api/posts/{postId} - 단일 조회
            if (pathParts.length == 2) {
                int postId = Integer.parseInt(pathParts[1]);
                Post post = postDAO.getPostById(postId, currentUserId);
                if (post == null) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "게시글을 찾을 수 없습니다.");
                } else {
                    sendJsonResponse(resp, post);
                }
                return;
            }

            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "잘못된 요청 경로입니다.");
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "ID 형식이 올바르지 않습니다.");
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getAuthenticatedUser(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        try {
            Post post = objectMapper.readValue(req.getReader(), Post.class);
            post.setUserId(user.getId());

            // 1. 일반 게시글 작성
            if (pathInfo == null || "/".equals(pathInfo)) {
                if (isEmpty(post.getContent())) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "내용을 입력해주세요.");
                    return;
                }
                processPost(resp, postDAO.addPost(post), post.getUserId(), "작성");
            }
            // 2. 리트윗
            else if (pathInfo.endsWith("/repost")) {
                handleRepost(resp, post, user.getId());
            }
            // 3. 인용 및 답글 (유사 로직 통합)
            else if (pathInfo.endsWith("/quote") || pathInfo.endsWith("/reply")) {
                if (isEmpty(post.getContent())) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "내용을 입력해주세요.");
                    return;
                }
                String type = pathInfo.endsWith("/quote") ? "QUOTE" : "REPLY";
                post.setType(type);
                boolean success = type.equals("QUOTE") ? postDAO.addQuote(post) : postDAO.addReply(post);
                processPost(resp, success, post.getUserId(), type + " 작성");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = getAuthenticatedUser(req, resp);
        if (user == null)
            return;

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.split("/").length != 2) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "삭제할 게시글 ID가 필요합니다.");
            return;
        }

        try {
            int postId = Integer.parseInt(pathInfo.split("/")[1]);
            Post post = postDAO.getPostById(postId);

            if (post == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "게시글이 존재하지 않습니다.");
            } else if (post.getUserId() != user.getId()) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "삭제 권한이 없습니다.");
            } else {
                boolean success = postDAO.deletePost(postId, user.getId());
                processPost(resp, success, user.getId(), "삭제");
            }
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    // --- 내부 보조 메서드 ---

    private void handleSubResource(HttpServletResponse resp, String idStr, String action, int currentUserId)
            throws IOException {
        int postId = Integer.parseInt(idStr);
        switch (action) {
            case "likes" -> sendJsonResponse(resp, postDAO.getLikedUsers(postId));
            case "reposts" -> sendJsonResponse(resp, postDAO.getRepostedUsers(postId));
            case "replies" -> sendJsonResponse(resp, postDAO.getDescendants(postId, currentUserId));
            case "ancestors" -> sendJsonResponse(resp, postDAO.getAncestors(postId, currentUserId));
            default -> sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
        }
    }

    private void handleRepost(HttpServletResponse resp, Post post, int userId) throws IOException {
        Post original = postDAO.getPostById(post.getOriginalPostId());
        if (original == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "원본을 찾을 수 없습니다.");
            return;
        }

        // 원본의 원본 찾기 (리포스트의 리포스트 방지)
        int targetId = ("REPOST".equals(original.getType()) && original.getOriginalPostId() > 0)
                ? original.getOriginalPostId()
                : post.getOriginalPostId();

        if (postDAO.hasUserReposted(userId, targetId)) {
            sendError(resp, HttpServletResponse.SC_CONFLICT, "이미 리포스트했습니다.");
            return;
        }

        post.setType("REPOST");
        post.setOriginalPostId(targetId);
        processPost(resp, postDAO.rePost(post), userId, "리포스트");
    }

    private void processPost(HttpServletResponse resp, boolean success, int userId, String actionName)
            throws IOException {
        if (success) {
            birdService.checkAndAwardBirds(userId, "post_count");
            sendJsonSuccess(resp, HttpServletResponse.SC_OK, actionName + " 성공");
        } else {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, actionName + " 실패");
        }
    }

}