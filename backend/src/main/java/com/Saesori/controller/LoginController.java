package com.Saesori.controller;

import com.Saesori.dao.UserDAO;
import com.Saesori.dto.User;
import com.Saesori.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 사용자 로그인 처리를 담당하는 서블릿 컨트롤러입니다.
 */
@WebServlet("/api/users/login")
public class LoginController extends BaseController {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        // UserDAO 초기화
        userDAO = new UserDAO();
    }

    /**
     * 로그인 요청을 처리합니다.
     * 입력받은 handle과 비밀번호를 검증하고 세션을 생성합니다.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // 요청 본문에서 로그인 시도 정보 읽기
            User loginAttempt = objectMapper.readValue(request.getReader(), User.class);

            // 데이터베이스에서 해당 handle의 사용자 조회
            User storedUser = userDAO.getUserByHandle(loginAttempt.getHandle());

            // 사용자 존재 여부 및 비밀번호 일치 확인
            if (storedUser != null
                    && PasswordUtil.checkPassword(loginAttempt.getPassword(), storedUser.getPassword())) {
                storedUser.setPassword(null); // 보안을 위해 비밀번호 해시는 응답에서 제외

                // 세션 생성 및 사용자 객체 저장
                jakarta.servlet.http.HttpSession session = request.getSession();
                session.setAttribute("user", storedUser);

                // 성공 응답 전송
                sendJsonResponse(response, storedUser);
            } else {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
            }
        } catch (IOException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "로그인 데이터 형식이 올바르지 않습니다.");
        } catch (Exception e) {
            // 예외 발생 시 공통 핸들러 호출
            handleException(response, e);
        }
    }
}
