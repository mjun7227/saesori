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

/**
 * 새 정보를 다루는 API 컨트롤러입니다.
 */
@WebServlet("/api/birds/*")
public class BirdController extends BaseController {
    private static final long serialVersionUID = 1L;
    private BirdDAO birdDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        birdDAO = new BirdDAO();
    }

    /**
     * 새 목록 또는 특정 새 상세 정보를 조회합니다.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo(); // 요청 경로 정보 가져오기

        try {
            // /api/birds - 모든 새 종류 조회
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Bird> birds = birdDAO.getAllBirds(); // 모든 새 목록 조회
                sendJsonResponse(response, birds); // JSON 응답 전송
                return;
            }

            // /api/birds/{birdId} - 특정 ID의 새 정보 조회
            String[] pathParts = pathInfo.split("/"); // 경로를 '/' 기준으로 분할
            if (pathParts.length == 2) { // 경로가 "/{birdId}" 형식인 경우
                try {
                    int birdId = Integer.parseInt(pathParts[1]); // birdId 추출 및 정수 변환
                    Bird bird = birdDAO.getBirdById(birdId); // ID로 새 정보 조회
                    if (bird != null) {
                        sendJsonResponse(response, bird); // 새 정보가 있으면 JSON 응답 전송
                    } else {
                        sendError(response, HttpServletResponse.SC_NOT_FOUND, "해당 새를 찾을 수 없습니다."); // 새를 찾을 수 없는 경우 404
                                                                                                   // 에러
                    }
                } catch (NumberFormatException e) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "ID 형식이 올바르지 않습니다."); // ID 형식이 잘못된 경우 400
                                                                                                  // 에러
                }
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "잘못된 API 경로입니다.");
            }

        } catch (Exception e) {
            handleException(response, e);
        }
    }
}
