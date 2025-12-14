package com.Saesori.controller;

import com.Saesori.dao.UserDAO;
import com.Saesori.dto.User;
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

@WebServlet("/api/users/register")
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            User user = objectMapper.readValue(request.getReader(), User.class);
            if (userDAO.getUserByHandle(user.getHandle()) != null) {
                sendError(response, HttpServletResponse.SC_CONFLICT, "Handle already exists.");
                return;
            }
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            if (userDAO.addUser(user)) {
                sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "User registered successfully.");
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to register user.");
            }
        } catch (IOException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user data.");
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
}
