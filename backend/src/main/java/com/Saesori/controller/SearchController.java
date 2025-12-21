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
public class SearchController extends BaseController {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private PostDAO postDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
        postDAO = new PostDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String q = request.getParameter("q");
            String type = request.getParameter("type"); // "user" 또는 "post" 타입 기대

            if (isEmpty(q)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'q' is required.");
                return;
            }
            if (isEmpty(type)) {
                // 타입이 제공되지 않은 경우 기본적으로 'post' 검색 수행
                type = "post";
            }

            // 현재 사용자 ID (게시글의 좋아요 상태 확인용)
            int currentUserId = getOptionalUserId(request);

            if ("user".equalsIgnoreCase(type)) {
                List<User> users = userDAO.searchUsers(q);
                sendJsonResponse(response, users);
            } else if ("post".equalsIgnoreCase(type)) {
                List<Post> posts = postDAO.searchPosts(q, currentUserId);
                sendJsonResponse(response, posts);
            } else if ("all".equalsIgnoreCase(type)) {
                List<User> users = userDAO.searchUsers(q);
                List<Post> posts = postDAO.searchPosts(q, currentUserId);
                sendJsonResponse(response, java.util.Map.of("users", users, "posts", posts));
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid type parameter. Use 'user', 'post' or 'all'.");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }
}
