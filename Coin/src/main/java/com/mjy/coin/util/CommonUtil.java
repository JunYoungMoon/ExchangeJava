package com.mjy.coin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.ApiResponse;
import com.mjy.coin.dto.CoinOrderDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommonUtil {
    // 모바일인지 체크하는 메서드
    public static boolean isMobile(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null && userAgent.contains("Mobile");
    }

    // IP 주소 가져오는 메서드
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    // 에러발생 예외처리 핸들링
    public static void handleException(HttpServletRequest servletRequest, HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");

        ApiResponse apiResponse = ApiResponse.builder()
                .status("fail")
                .msg(message)
                .build();

        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }

    public static String generateUniqueKey(CoinOrderDTO order) {
        if (order != null) {
            return UUID.randomUUID() + ":" + order.getCreatedAt();
        }
        return UUID.randomUUID() + ":" + LocalDateTime.now();
    }
}
