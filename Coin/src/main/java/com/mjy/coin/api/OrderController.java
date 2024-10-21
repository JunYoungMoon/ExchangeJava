package com.mjy.coin.api;

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
    public String sendOrder() {

//        messagingTemplate.convertAndSend("/coin/" + key + "/order", priceMessage);

        // 주문 메시지를 구독한 클라이언트에게 전송
        return "Order message received for symbol: BTC-KRW";
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
