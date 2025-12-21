package com.Saesori.controller;

import com.Saesori.dao.UserDAO;
import com.Saesori.dao.UserBirdDAO;
import com.Saesori.dto.User;
import com.Saesori.dto.Bird;
import com.Saesori.util.PasswordUtil;
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

@WebServlet("/api/users/*")
public class UserController extends BaseController {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private UserBirdDAO userBirdDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        userBirdDAO = new UserBirdDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid API endpoint.");
                return;
            }

            String[] pathParts = pathInfo.split("/");

            // /api/users/{id} - 사용자 ID로 조회
            if (pathParts.length == 2) {
                try {
                    int userId = Integer.parseInt(pathParts[1]);
                    User user = userDAO.getUserById(userId);
                    if (user != null) {
                        user.setPassword(null);
                        sendJsonResponse(response, user);
                    } else {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                    }
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            }
            // /api/users/{userId}/birds - 사용자 보유 새 조회
            else if (pathParts.length == 3 && pathParts[2].equals("birds")) {
                try {
                    int userId = Integer.parseInt(pathParts[1]);
                    List<Bird> birds = userBirdDAO.getUserBirds(userId);
                    sendJsonResponse(response, birds);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user endpoint.");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid API endpoint.");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            // /api/users/{id} - 사용자 정보 수정
            if (pathParts.length == 2) {
                try {
                    int userId = Integer.parseInt(pathParts[1]);
                    User existing = userDAO.getUserById(userId);
                    if (existing == null) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                        return;
                    }

                    // JSON 요청 본문 파싱
                    User input = objectMapper.readValue(request.getReader(), User.class);
                    if (input.getHandle() == null && input.getNickname() == null) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No fields to update.");
                        return;
                    }

                    // 필드 업데이트 적용
                    if (input.getHandle() != null)
                        existing.setHandle(input.getHandle());
                    if (input.getNickname() != null)
                        existing.setNickname(input.getNickname());
                    if (input.getBio() != null)
                        existing.setBio(input.getBio());
                    if (input.getProfileImageUrl() != null)
                        existing.setProfileImageUrl(input.getProfileImageUrl());

                    boolean ok = userDAO.updateUser(existing);
                    if (ok) {
                        existing.setPassword(null);
                        sendJsonResponse(response, existing);
                    } else {
                        sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update user.");
                    }
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user endpoint.");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "POST not allowed on this endpoint. Use /api/users/login or /api/users/register.");
    }
}
