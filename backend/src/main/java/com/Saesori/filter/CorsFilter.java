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

		// cors 필터
		//httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
		/*httpResponse.setHeader("Access-Control-Allow-Origin", "https://saesori.vercel.app");
		httpResponse.setHeader("Access-Control-Allow-Origin", "https://saesori.vercel.app/");
		httpResponse.setHeader("Access-Control-Allow-Origin", "https://saesori.shop");
		httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		httpResponse.setHeader("Access-Control-Allow-Headers",
				"Content-Type, Authorization, X-Requested-With, Accept, User-Id");

		//httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

		if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			return;
		}*/


		chain.doFilter(request, response);
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

	public void destroy() {
	}
}