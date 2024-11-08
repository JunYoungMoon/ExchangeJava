package com.mjy.websocket.component;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;

@Component
public class OrderErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        // 예외에 따라 처리할 에러 메시지를 동적으로 변경
        if (ex instanceof MalformedInputException) {
            return handleMalformedInputException(clientMessage, (MalformedInputException) ex);
        }

        if (ex instanceof ExpiredJwtException) {
            return handleExpiredJwtException(clientMessage, (ExpiredJwtException) ex);
        }

        if (ex instanceof MessageDeliveryException) {
            return handleMessageDeliveryException(clientMessage, (MessageDeliveryException) ex);
        }

        // 그 외 예외는 기본 처리
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    private Message<byte[]> handleMalformedInputException(Message<byte[]> clientMessage, MalformedInputException ex) {
        // 잘못된 입력에 대한 처리를 한다
        return prepareErrorMessage(clientMessage, "Malformed input, invalid format.", "400");
    }

    private Message<byte[]> handleExpiredJwtException(Message<byte[]> clientMessage, ExpiredJwtException ex) {
        // JWT 토큰 만료에 대한 처리를 한다
        return prepareErrorMessage(clientMessage, "JWT token expired.", "401");
    }

    private Message<byte[]> handleMessageDeliveryException(Message<byte[]> clientMessage, MessageDeliveryException ex) {
        // 메시지 전달 오류에 대한 처리를 한다
        return prepareErrorMessage(clientMessage, "Message delivery failed.", "500");
    }

    private Message<byte[]> prepareErrorMessage(Message<byte[]> clientMessage, String message, String errorCode) {
        // 에러 메시지 및 상태 코드를 설정하여 클라이언트에게 전달
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);

        accessor.setMessage(errorCode); // errorCode 설정
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(message.getBytes(StandardCharsets.UTF_8), accessor.getMessageHeaders());
    }
}
