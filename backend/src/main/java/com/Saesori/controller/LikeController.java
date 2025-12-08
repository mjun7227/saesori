package com.Saesori.controller;

import com.Saesori.dao.LikeDAO;
import com.Saesori.service.BirdService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.Saesori.dto.User;

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
public class LikeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private LikeDAO likeDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        likeDAO = new LikeDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required to like a post.");
                return;
            }

            // Expect /api/likes/{postId}
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
                // Potential for gamification/birds here
                sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "Like added.", postId);
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to add like.");
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid postId format.");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required to unlike a post.");
                return;
            }

            // Expect /api/likes/{postId}
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
                sendJsonSuccess(response, HttpServletResponse.SC_OK, "Like removed.", postId);
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "Like not found or failed to remove.");
            }

        } catch (NumberFormatException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid postId format.");
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

    private void sendJsonSuccess(HttpServletResponse response, int statusCode, String message, int postId)
            throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.println(String.format("{\"message\": \"%s\", \"postId\": %d}", message, postId));
        out.flush();
    }

    private static class LikeRequest {
        private int postId;

        public int getPostId() {
            return postId;
        }

        public void setPostId(int postId) {
            this.postId = postId;
        }
    }
}
