package com.mjy.websocket.component;

import com.mjy.websocket.dto.CoinOrderDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchListKafkaListener {

    @KafkaListener(topics = "Match-List", groupId = "coinOrderGroup", concurrency = "4", containerFactory = "matchListKafkaListenerContainerFactory")
    public void listen(List<CoinOrderDTO> order) {
        System.out.println(order);
    }
}
