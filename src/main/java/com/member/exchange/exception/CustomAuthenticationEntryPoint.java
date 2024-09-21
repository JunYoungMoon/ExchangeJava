package com.member.exchange.exception;

import com.member.exchange.dto.ApiResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 인증이 실패하거나 권한이 없는 경우, 클라이언트에 메시지 전달
        String errorMessage = "Authentication failed or insufficient permissions.";

        ApiResponse apiResponse = ApiResponse.builder()
                .status("fail")
                .csrfToken(((CsrfToken) request.getAttribute(CsrfToken.class.getName())).getToken())
                .msg(errorMessage)
                .build();

        // JSON 형식의 응답 전송
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }
}
