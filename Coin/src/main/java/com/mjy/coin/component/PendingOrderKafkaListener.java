package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.PendingOrderProcessorService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;


@Component
public class PendingOrderKafkaListener implements MessageListener<String, CoinOrderDTO> {

    private final PendingOrderProcessorService pendingOrderProcessorService;

    @Autowired
    public PendingOrderKafkaListener(PendingOrderProcessorService pendingOrderProcessorService) {
        this.pendingOrderProcessorService = pendingOrderProcessorService;
    }

    @Override
    public void onMessage(ConsumerRecord<String, CoinOrderDTO> record) {
        pendingOrderProcessorService.processOrder(record.value());
    }
}