package com.mjy.websocket.component;

import com.mjy.websocket.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrderPreHandler implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtTokenService jwtTokenService;

    public OrderPreHandler(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

        if (headerAccessor.getDestination() != null) {
            String destination = headerAccessor.getDestination();

            // "/user/{userId}/topic/coin/{symbol}/order" 패턴에서 {symbol} 부분만 추출
            Pattern pattern = Pattern.compile("/user/[^/]+/topic/coin/([^/]+)/order");
            Matcher matcher = pattern.matcher(destination);

            if (matcher.matches()) {
                // Authorization 헤더에서 토큰을 추출
                List<String> authorizationHeaders = headerAccessor.getNativeHeader("Authorization");

                if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
                    throw new MessageDeliveryException("Authorization header is missing.");
                }

                String authorizationHeader = authorizationHeaders.get(0);

                if (authorizationHeader == null || authorizationHeader.equals("null")) {
                    throw new MessageDeliveryException("Authorization token is invalid.");
                }

                String token = authorizationHeader.substring(BEARER_PREFIX.length());

                try {
                    // JWT 토큰 검증
                    jwtTokenService.validateToken(token);

                    // 토큰에서 claims 추출
                    Claims claims = jwtTokenService.parseClaims(token);

                    // 세션에 사용자 ID나 필요한 정보를 설정
                    headerAccessor.setUser(claims::getSubject);  // 사용자 ID 설정

                    // 인증이 완료되었으므로 메시지를 보내도록 허용
                    return ChannelInterceptor.super.preSend(message, channel);
                } catch (ExpiredJwtException e) {
                    throw new MessageDeliveryException("JWT token expired.");
                } catch (JwtException | IllegalArgumentException e) {
                    throw new MessageDeliveryException("Invalid JWT token.");
                } catch (SecurityException e) {
                    throw new MessageDeliveryException("Forbidden: Invalid token.");
                }
            }
        }

        // 특정 주제가 아니면 메시지를 그대로 전달
        return message;
    }

}
