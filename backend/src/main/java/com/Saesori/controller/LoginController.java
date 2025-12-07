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

@WebServlet("/api/users/login")
public class LoginController extends HttpServlet {
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
            User loginAttempt = objectMapper.readValue(request.getReader(), User.class);
            User storedUser = userDAO.getUserByUsername(loginAttempt.getUsername());

            if (storedUser != null && PasswordUtil.checkPassword(loginAttempt.getPassword(), storedUser.getPassword())) {
                storedUser.setPassword(null); // 비밀번호 해시는 전송하지 않음
                
                // 세션 생성 및 사용자 정보 저장
                jakarta.servlet.http.HttpSession session = request.getSession();
                session.setAttribute("user", storedUser);
                
                objectMapper.writeValue(response.getWriter(), storedUser);
            } else {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
            }
        } catch (IOException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid login data.");
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
}
