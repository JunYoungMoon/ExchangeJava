package com.mjy.websocket.component;

import com.mjy.websocket.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

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

        // 헤더 토큰 얻기
        String authorizationHeader = String.valueOf(headerAccessor.getNativeHeader("Authorization"));

        if(authorizationHeader == null || authorizationHeader.equals("null")){
            throw new MessageDeliveryException("메세지 예외");
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        jwtTokenService.validateToken(token);
        Claims claims = jwtTokenService.parseClaims(token);
        String tokenType = claims.get("tokenType", String.class);

        return ChannelInterceptor.super.preSend(message, channel);
    }
}
