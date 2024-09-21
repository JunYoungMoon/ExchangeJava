package com.member.exchange.handler;

import com.member.exchange.dto.TokenInfo;
import com.member.exchange.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${client.app.url}")
    private String clientUrl;

//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
//        log.info("OAuth2 Login 성공");
//        //JWT 생성
//        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);
//
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        String jsonResponse = objectMapper.writeValueAsString(newTokenInfo);
//        response.getWriter().write(jsonResponse);
//    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // JWT 생성
        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);

        // JWT를 GET 파라미터로 전달할 URL 생성
        UriComponentsBuilder redirectUrlBuilder = UriComponentsBuilder.fromUriString(clientUrl + "/auth/callback");
        redirectUrlBuilder.queryParam("accessToken", newTokenInfo.getAccessToken());
        redirectUrlBuilder.queryParam("refreshToken", newTokenInfo.getRefreshToken());
        redirectUrlBuilder.queryParam("csrfToken", ((CsrfToken) request.getAttribute(CsrfToken.class.getName())).getToken());
        String redirectUrl = redirectUrlBuilder.toUriString();

        log.info("OAuth2 Login 성공");

        // 클라이언트로 리다이렉트
        response.sendRedirect(redirectUrl);
    }
}
