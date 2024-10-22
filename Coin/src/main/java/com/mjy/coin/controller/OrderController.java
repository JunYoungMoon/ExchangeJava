package com.mjy.coin.controller;

import com.mjy.coin.dto.ChartMessage;
import com.mjy.coin.dto.OrderMessage;
import com.mjy.coin.dto.PriceMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class OrderController {

    private final SimpMessagingTemplate messagingTemplate;

    public OrderController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/coin/BTC-KRW/order")
    @SendTo("/topic/coin/BTC-KRW/order")
    public OrderMessage sendOrder(OrderMessage orderMessage) {
        // 실제로 전송할 메시지를 반환
        return orderMessage;
    }

    @MessageMapping("/coin/{symbol}/price")
    @SendTo("/topic/coin/{symbol}/price")
    public PriceMessage sendPrice(@DestinationVariable String symbol, PriceMessage price) {
        return price;
    }

    @MessageMapping("/coin/{symbol}/chart")
    @SendTo("/topic/coin/{symbol}/chart")
    public ChartMessage sendChart(@DestinationVariable String symbol, ChartMessage chart) {
        return chart;
    }

}
