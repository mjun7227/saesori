package com.Saesori.controller;

import com.Saesori.dao.FollowDAO;
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
import java.util.stream.Collectors;

@WebServlet("/api/follows/*")
public class FollowController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private FollowDAO followDAO;
    private BirdService birdService;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        followDAO = new FollowDAO();
        birdService = new BirdService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // /api/follows/check?followerId={}&followingId={}
            if (pathInfo != null && pathInfo.equals("/check")) {
                String followerIdParam = request.getParameter("followerId");
                String followingIdParam = request.getParameter("followingId");

                if (followerIdParam == null || followingIdParam == null) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Both followerId and followingId parameters are required.");
                    return;
                }

                try {
                    int followerId = Integer.parseInt(followerIdParam);
                    int followingId = Integer.parseInt(followingIdParam);

                    boolean isFollowing = followDAO.isFollowing(followerId, followingId);
                    
                    PrintWriter out = response.getWriter();
                    out.println(String.format("{\"isFollowing\": %b}", isFollowing));
                    out.flush();

                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid follow GET endpoint.");
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
            // /api/follows - 팔로우 추가
            if (pathInfo == null || pathInfo.equals("/")) {
                 try {
                    String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                    FollowRequest followRequest = objectMapper.readValue(requestBody, FollowRequest.class);

                    if (followRequest.getFollowerId() == 0 || followRequest.getFollowingId() == 0) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Both followerId and followingId are required.");
                        return;
                    }

                    if (followDAO.addFollow(followRequest.getFollowerId(), followRequest.getFollowingId())) {
                        birdService.checkAndAwardBirds(followRequest.getFollowerId(), "friend_count");
                        sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "Follow relationship added.");
                    } else {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Failed to add follow relationship (possibly already following).");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid follow data.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid follow POST endpoint.");
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
             // /api/follows - 팔로우 삭제
            if (pathInfo == null || pathInfo.equals("/")) {
                try {
                    String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                    FollowRequest followRequest = objectMapper.readValue(requestBody, FollowRequest.class);

                    if (followRequest.getFollowerId() == 0 || followRequest.getFollowingId() == 0) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Both followerId and followingId are required.");
                        return;
                    }

                    if (followDAO.removeFollow(followRequest.getFollowerId(), followRequest.getFollowingId())) {
                        sendJsonSuccess(response, HttpServletResponse.SC_OK, "Follow relationship removed.");
                    } else {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Follow relationship not found or failed to remove.");
                    }
                } catch (IOException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid unfollow data.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid follow DELETE endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
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

    private static class FollowRequest {
        public int followerId;
        public int followingId;

        public int getFollowerId() { return followerId; }
        public void setFollowerId(int followerId) { this.followerId = followerId; }
        public int getFollowingId() { return followingId; }
        public void setFollowingId(int followingId) { this.followingId = followingId; }
    }
}

