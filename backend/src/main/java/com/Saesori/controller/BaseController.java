package com.Saesori.controller;

import com.Saesori.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * 중복되는 응답 처리 및 사용자 인증 로직을 담은 기본 컨트롤러 클래스
 */
public abstract class BaseController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * 데이터를 JSON 형식으로 응답합니다.
     */
    protected void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        objectMapper.writeValue(resp.getWriter(), data);
    }

    /**
     * 성공 메시지를 JSON 형식으로 응답합니다.
     */
    protected void sendJsonSuccess(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        sendJsonResponse(resp, Map.of("message", msg));
    }

    /**
     * 에러 메시지를 JSON 형식으로 응답합니다.
     */
    protected void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        sendJsonResponse(resp, Map.of("error", msg));
    }

    /**
     * 예외 발생 시 에러 메시지를 응답합니다.
     */
    protected void handleException(HttpServletResponse resp, Exception e) throws IOException {
        e.printStackTrace();
        sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * 인증된 사용자 정보를 가져오거나, 없으면 에러를 보냅니다.
     */
    protected User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return user;
    }

    /**
     * 로그인 중인 경우 사용자 ID를, 아니면 0을 반환합니다.
     */
    protected int getOptionalUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            return (user != null) ? user.getId() : 0;
        }
        return 0;
    }

    /**
     * 문자열이 비어있는지 확인합니다.
     */
    protected boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
