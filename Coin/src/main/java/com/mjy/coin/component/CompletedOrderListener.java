package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CompletedOrderProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CompletedOrderListener {

    private final CompletedOrderProcessorService completedOrderProcessorService;

    @Autowired
    public CompletedOrderListener(CompletedOrderProcessorService completedOrderProcessorService) {
        this.completedOrderProcessorService = completedOrderProcessorService;
    }

    @KafkaListener(topics = "Order-Completed", groupId = "coinOrderGroup", concurrency = "4")
    public void listen(CoinOrderDTO order) {
        completedOrderProcessorService.completedProcessorOrder(order);
    }
}
