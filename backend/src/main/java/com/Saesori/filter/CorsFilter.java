// 예시: CorsFilter.java
package com.Saesori.filter;

import java.io.IOException;
// 💡 패키지 이름 변경: javax -> jakarta
import jakarta.servlet.Filter; 
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

// 💡 어노테이션도 jakarta 패키지 사용
@WebFilter("/*")
public class CorsFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // ... (CORS 헤더 설정 로직은 동일) ...
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // React 개발 서버 주소
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        if (httpRequest.getMethod().equals("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return; 
        }

        chain.doFilter(request, response);
    }

    // init과 destroy 메소드는 그대로 유지됩니다.
    public void init(FilterConfig fConfig) throws ServletException {}
    public void destroy() {}
}