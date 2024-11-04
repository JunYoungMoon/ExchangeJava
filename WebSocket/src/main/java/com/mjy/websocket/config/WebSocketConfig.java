package com.mjy.websocket.config;

import com.mjy.websocket.component.OrderPreHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final OrderPreHandler orderPreHandler;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/user"); // 클라이언트에게 브로드캐스트할 주제
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 발행하는 접두사
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 권한이 필요한 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // 권한이 필요 없는 엔드포인트
        registry.addEndpoint("/public-ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(orderPreHandler);
    }
}
