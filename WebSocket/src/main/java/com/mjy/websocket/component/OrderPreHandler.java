package com.mjy.websocket.component;

import com.mjy.websocket.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

//         예: 특정 주제에 대해서만 동작
        if (headerAccessor.getDestination() != null && headerAccessor.getDestination().startsWith("/topic/coin/BTC-KRW/order")) {
            List<String> authorizationHeaders = headerAccessor.getNativeHeader("Authorization");

            String authorizationHeader = Objects.requireNonNull(authorizationHeaders).get(0);

            if(authorizationHeader == null || authorizationHeader.equals("null")){
                throw new MessageDeliveryException("메세지 예외");
            }

            String token = authorizationHeader.substring(BEARER_PREFIX.length());

            jwtTokenService.validateToken(token);
            Claims claims = jwtTokenService.parseClaims(token);
            String tokenType = claims.get("tokenType", String.class);

            // 세션에 사용자 ID나 필요한 정보를 저장
            // 또는 사용자 ID
            headerAccessor.setUser(claims::getSubject);

            return ChannelInterceptor.super.preSend(message, channel);
        }

        return message;

    }
}
