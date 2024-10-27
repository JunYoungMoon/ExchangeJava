package com.mjy.coin.component;

import com.mjy.coin.dto.PriceVolumeDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceVolumeKafkaListener {
    private final SimpMessagingTemplate messagingTemplate;

    public PriceVolumeKafkaListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "Price-Volume", groupId = "coinOrderGroup", containerFactory = "priceVolumeListKafkaListenerContainerFactory")
    public void listen(List<PriceVolumeDTO> priceVolumeList) {
        String key = "BTC-KRW"; //임시로 지정 전달받는 값으로 설정필요

        System.out.println(priceVolumeList);
        messagingTemplate.convertAndSend("/topic/coin/" + key + "/chart", priceVolumeList);
    }
}
