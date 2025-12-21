package com.Saesori.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@WebServlet("/api/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, // 1 MB: 메모리 내 보존 임계값
        maxFileSize = 1024 * 1024 * 10, // 10 MB: 최대 파일 크기
        maxRequestSize = 1024 * 1024 * 100 // 100 MB: 최대 요청 크기
)
public class UploadController extends BaseController {
    private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String appPath = request.getServletContext().getRealPath("");
        String savePath = appPath + File.separator + UPLOAD_DIR;

        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        try {
            Part filePart = request.getPart("file");
            if (filePart == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "No file content");
                return;
            }

            String fileName = getSubmittedFileName(filePart);
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }

            // 보안을 위해 파일명 랜덤 생성
            String savedFileName = UUID.randomUUID().toString() + extension;
            String fullPath = savePath + File.separator + savedFileName;

            filePart.write(fullPath);

            // 클라이언트에 반환할 URL (예: /uploads/abc.jpg)
            // 컨텍스트 경로 포함하여 반환
            String fileUrl = request.getContextPath() + "/" + UPLOAD_DIR + "/" + savedFileName;

            sendJsonResponse(response, java.util.Map.of("url", fileUrl));

        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null)
            return null;
        for (String cd : contentDisp.split(";")) {
            if (cd.trim().startsWith("filename")) {
                String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE
                                                                                                                    // 브라우저
                                                                                                                    // 대응
            }
        }
        return null;
    }
}
