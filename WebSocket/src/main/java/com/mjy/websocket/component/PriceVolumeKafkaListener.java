package com.mjy.websocket.component;

import com.mjy.websocket.dto.PriceVolumeDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PriceVolumeKafkaListener {
    private final SimpMessagingTemplate messagingTemplate;

    public PriceVolumeKafkaListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "Price-Volume", groupId = "coinOrderGroup", containerFactory = "priceVolumeMapKafkaListenerContainerFactory")
    public void listen(Map<String, List<PriceVolumeDTO>> priceVolumeMap) {
        for (Map.Entry<String, List<PriceVolumeDTO>> entry : priceVolumeMap.entrySet()) {
            String key = entry.getKey();
            List<PriceVolumeDTO> priceVolumeList = entry.getValue();

            for (PriceVolumeDTO priceVolumeDTO : priceVolumeList) {
                System.out.println("Sending data for key: " + key + " - " + priceVolumeDTO);
                messagingTemplate.convertAndSend("/topic/coin/" + key + "/chart", priceVolumeDTO);
            }
        }
    }
}
