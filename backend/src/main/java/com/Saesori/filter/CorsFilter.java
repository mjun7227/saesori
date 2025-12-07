package com.Saesori.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@WebFilter("/*")
public class CorsFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        System.out.println("request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());

        // ... (CORS 헤더 설정 로직은 동일) ...
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // React 개발 서버 주소
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, User-Id");
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