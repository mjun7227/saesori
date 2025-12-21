package com.Saesori.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

/**
 * 업로드된 파일을 서빙하기 위한 컨트롤러입니다.
 * /uploads/* 경로로 요청된 파일을 실제 서버 파일 시스템에서 찾아 응답합니다.
 */
@WebServlet("/uploads/*")
public class FileController extends BaseController {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "uploads";

    /**
     * 파일 다운로드/보기 요청을 처리합니다.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 요청된 파일 경로 추출
        String requestedFile = request.getPathInfo();
        if (requestedFile == null || requestedFile.equals("/")) {
            sendError(response, HttpServletResponse.SC_NOT_FOUND, "파일을 찾을 수 없습니다.");
            return;
        }

        try {
            // URL 인코딩된 파일명 디코딩
            String fileName = URLDecoder.decode(requestedFile, "UTF-8");
            String appPath = request.getServletContext().getRealPath("");
            // 실제 저장된 파일 경로 생성
            File file = new File(appPath + File.separator + UPLOAD_DIR, fileName);

            // 파일 존재 여부 확인
            if (!file.exists()) {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "서버에서 파일을 찾을 수 없습니다.");
                return;
            }

            // 파일의 MIME 타입 결정
            String contentType = getServletContext().getMimeType(file.getName());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            response.setContentType(contentType);
            response.setContentLength((int) file.length());

            // 파일을 데이터 스트림으로 전송
            try (FileInputStream in = new FileInputStream(file);
                    OutputStream out = response.getOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            // 예외 발생 시 공통 핸들러 호출
            handleException(response, e);
        }
    }
}
