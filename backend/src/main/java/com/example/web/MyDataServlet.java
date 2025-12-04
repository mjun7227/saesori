// 예시: MyDataServlet.java
package com.example.web;

import java.io.IOException;
import java.io.PrintWriter;
// 💡 Jakarta EE 패키지 사용
import jakarta.servlet.ServletException; 
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 서블릿 URL 매핑
@WebServlet("/api/data") 
public class MyDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    	System.out.println("요청 메서드: " + request.getMethod() + " / 시간: " + System.currentTimeMillis());
        // 1. CORS 헤더 설정 (React 개발 서버 접근 허용)
        // **⚠️ 주의: 이 서블릿에만 적용됩니다. 전역 설정이 필요하면 필터 사용을 권장합니다.**
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); 
        response.setHeader("Access-Control-Allow-Methods", "GET"); // GET만 허용
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        // 2. 응답 인코딩 및 Content-Type 설정 (JSON 형식)
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 3. 쿼리 파라미터 받기 (예: /api/data?name=React)
        String name = request.getParameter("name"); 
        
        // 4. 응답할 JSON 데이터 구성
        String data = String.format(
            "{\"message\": \"Jakarta 서블릿에서 %s님께 응답합니다.\", \"status\": \"ok\"}", 
            (name != null ? name : "손님")
        );

        // 5. 응답 전송
        PrintWriter out = response.getWriter();
        out.print(data);
        out.flush();
    }
}