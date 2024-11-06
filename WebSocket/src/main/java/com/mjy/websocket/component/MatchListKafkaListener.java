package com.mjy.websocket.component;

import com.mjy.websocket.dto.CoinOrderDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MatchListKafkaListener {

    private final SimpMessagingTemplate messagingTemplate;

    public MatchListKafkaListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "Match-List", groupId = "coinOrderGroup", containerFactory = "matchListKafkaListenerContainerFactory")
    public void listen(Map<String, List<CoinOrderDTO>> matchListMap) {
        for (Map.Entry<String, List<CoinOrderDTO>> entry : matchListMap.entrySet()) {

            String key = entry.getKey();
            List<CoinOrderDTO> matchList = entry.getValue();

            for (CoinOrderDTO coinOrderDTO : matchList) {
                System.out.println("Match-List : " + coinOrderDTO);
                messagingTemplate.convertAndSendToUser(coinOrderDTO.getMemberUuid(), "/topic/coin/" + key + "/order", coinOrderDTO);
            }
        }
    }
}
