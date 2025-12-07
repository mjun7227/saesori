package com.Saesori.controller;

import com.Saesori.dao.BirdDAO;
import com.Saesori.dto.Bird;
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

@WebServlet("/api/birds/*")
public class BirdController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private BirdDAO birdDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        birdDAO = new BirdDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // /api/birds - 모든 새 조회
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Bird> birds = birdDAO.getAllBirds();
                objectMapper.writeValue(response.getWriter(), birds);
                return;
            }

            // /api/birds/{birdId} - 새 ID로 조회
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                try {
                    int birdId = Integer.parseInt(pathParts[1]);
                    Bird bird = birdDAO.getBirdById(birdId);
                    if (bird != null) {
                        objectMapper.writeValue(response.getWriter(), bird);
                    } else {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "Bird not found.");
                    }
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bird ID format.");
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid bird endpoint.");
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
}
