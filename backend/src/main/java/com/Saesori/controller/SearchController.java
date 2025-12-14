package com.Saesori.controller;

import com.Saesori.dao.PostDAO;
import com.Saesori.dao.UserDAO;
import com.Saesori.dto.Post;
import com.Saesori.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = { "/api/search" })
public class SearchController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ObjectMapper objectMapper;
    private UserDAO userDAO;
    private PostDAO postDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        userDAO = new UserDAO();
        postDAO = new PostDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String q = request.getParameter("q");
            String type = request.getParameter("type"); // expected: "user" or "post"

            if (q == null || q.trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'q' is required.");
                return;
            }
            if (type == null || type.trim().isEmpty()) {
                // default to post search if not provided
                type = "post";
            }

            // Current user (for like status in posts)
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            com.Saesori.dto.User user = (session != null) ? (com.Saesori.dto.User) session.getAttribute("user") : null;
            int currentUserId = (user != null) ? user.getId() : 0;

            if ("user".equalsIgnoreCase(type)) {
                List<User> users = userDAO.searchUsers(q);
                objectMapper.writeValue(response.getWriter(), users);
                return;
            } else if ("post".equalsIgnoreCase(type)) {
                List<Post> posts = postDAO.searchPosts(q, currentUserId);
                objectMapper.writeValue(response.getWriter(), posts);
                return;
            } else if ("all".equalsIgnoreCase(type)) {
                List<User> users = userDAO.searchUsers(q);
                List<Post> posts = postDAO.searchPosts(q, currentUserId);
                objectMapper.writeValue(response.getWriter(), java.util.Map.of("users", users, "posts", posts));
                return;
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid type parameter. Use 'user', 'post' or 'all'.");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), java.util.Collections.singletonMap("error", message));
    }
}
