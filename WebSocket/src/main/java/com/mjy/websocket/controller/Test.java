package com.mjy.websocket.controller;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {

    private final SimpMessagingTemplate messagingTemplate;

    public Test(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/test")
    public String test() {
        messagingTemplate.convertAndSendToUser("cfccbb28-f07d-4e7c-8bd2-4cbd720aceab", "/topic/coin/BTC-KRW/order", "test");
        return "test";
    }
}
