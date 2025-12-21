package com.Saesori.controller;

import com.Saesori.dao.LikeDAO;
import com.Saesori.dao.PostDAO;
import com.Saesori.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Saesori.dto.User;
import com.Saesori.dto.Post;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet("/api/likes/*")
public class LikeController extends BaseController {
    private static final long serialVersionUID = 1L;
    private LikeDAO likeDAO;
    private PostDAO postDAO;
    private BirdService birdService;

    @Override
    public void init() throws ServletException {
        super.init();
        likeDAO = new LikeDAO();
        postDAO = new PostDAO();
        birdService = new BirdService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getAuthenticatedUser(request, response);
            if (user == null)
                return;

            // /api/likes/{postId} 형식의 경로 기대
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "postId required in path (e.g. /api/likes/123).");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format.");
                return;
            }

            int postId = Integer.parseInt(pathParts[1]);

            if (likeDAO.isLiked(postId, user.getId())) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Already liked.");
                return;
            }

            if (likeDAO.addLike(postId, user.getId())) {
                // 좋아요를 누른 유저 본인에게 새 지급 조건 확인
                birdService.checkAndAwardBirds(user.getId(), "like_count");
                sendJsonResponse(response, java.util.Map.of("message", "Like added.", "postId", postId));
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add like.");
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid postId format.");
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            User user = getAuthenticatedUser(request, response);
            if (user == null)
                return;

            // /api/likes/{postId} 형식의 경로 기대
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "postId required in path (e.g. /api/likes/123).");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path format.");
                return;
            }

            int postId = Integer.parseInt(pathParts[1]);

            if (likeDAO.removeLike(postId, user.getId())) {
                sendJsonResponse(response, java.util.Map.of("message", "Like removed.", "postId", postId));
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Like not found or failed to remove.");
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid postId format.");
        } catch (Exception e) {
            handleException(response, e);
        }
    }
}
