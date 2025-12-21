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

/**
 * 사용자 회원가입 처리를 담당하는 서블릿 컨트롤러입니다.
 */
@WebServlet("/api/users/register")
public class RegisterController extends BaseController {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        // UserDAO 초기화
        userDAO = new UserDAO();
    }

    /**
     * 회원가입 요청을 처리합니다.
     * JSON 형식의 사용자 데이터를 받아 검증 후 데이터베이스에 저장합니다.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 요청 본문에서 사용자 정보 읽기
            User user = objectMapper.readValue(request.getReader(), User.class);

            // 기존에 존재하는 handle(아이디)인지 확인
            if (userDAO.getUserByHandle(user.getHandle()) != null) {
                sendError(response, HttpServletResponse.SC_CONFLICT, "이미 존재하는 아이디입니다.");
                return;
            }

            // 비밀번호 해싱 처리
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));

            // 사용자 추가 수행
            if (userDAO.addUser(user)) {
                sendJsonSuccess(response, HttpServletResponse.SC_CREATED, "회원가입이 완료되었습니다.");
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "회원가입 처리 중 오류가 발생했습니다.");
            }
        } catch (IOException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 사용자 데이터입니다.");
        } catch (Exception e) {
            // 기타 예외 처리
            handleException(response, e);
        }
    }
}
