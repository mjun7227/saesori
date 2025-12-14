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
public class UserController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private UserDAO userDAO;
    private UserBirdDAO userBirdDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        userDAO = new UserDAO();
        userBirdDAO = new UserBirdDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
                        objectMapper.writeValue(response.getWriter(), user);
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
                    objectMapper.writeValue(response.getWriter(), birds);
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user endpoint.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid API endpoint.");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            // /api/users/{id} - update user
            if (pathParts.length == 2) {
                try {
                    int userId = Integer.parseInt(pathParts[1]);
                    User existing = userDAO.getUserById(userId);
                    if (existing == null) {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                        return;
                    }

                    // parse JSON body
                    User input = objectMapper.readValue(request.getReader(), User.class);
                    if (input.getHandle() == null && input.getNickname() == null) {
                        sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No fields to update.");
                        return;
                    }

                    // apply fields
                    if (input.getHandle() != null) existing.setHandle(input.getHandle());
                    if (input.getNickname() != null) existing.setNickname(input.getNickname());

                    boolean ok = userDAO.updateUser(existing);
                    if (ok) {
                        existing.setPassword(null);
                        objectMapper.writeValue(response.getWriter(), existing);
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
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST not allowed on this endpoint. Use /api/users/login or /api/users/register.");
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
