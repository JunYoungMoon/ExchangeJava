package com.mjy.websocket.component;

import com.mjy.websocket.dto.CoinOrderDTO;
import com.mjy.websocket.dto.MatchOrderDTO;
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
                MatchOrderDTO matchOrderDTO = new MatchOrderDTO();

                matchOrderDTO.setMarketName(coinOrderDTO.getMarketName());
                matchOrderDTO.setCoinName(coinOrderDTO.getCoinName());
                matchOrderDTO.setQuantity(coinOrderDTO.getQuantity());
                matchOrderDTO.setExecutionPrice(coinOrderDTO.getExecutionPrice());
                matchOrderDTO.setOrderPrice(coinOrderDTO.getOrderPrice());
                matchOrderDTO.setOrderType(coinOrderDTO.getOrderType());
                matchOrderDTO.setCreatedAt(coinOrderDTO.getCreatedAt());
                matchOrderDTO.setMatchedAt(coinOrderDTO.getMatchedAt());

                messagingTemplate.convertAndSendToUser(coinOrderDTO.getMemberUuid(), "/topic/coin/" + key + "/order", matchOrderDTO);
            }
        }
    }
}
