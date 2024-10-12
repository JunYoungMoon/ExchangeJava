package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTemplateListener implements MessageListener<String, CoinOrderDTO> {

    private final OrderProcessor orderProcessor;

    @Autowired
    public KafkaTemplateListener(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void onMessage(ConsumerRecord<String, CoinOrderDTO> record) {
        CoinOrderDTO order = record.value();

        // 주문 처리
        orderProcessor.processOrder(order);
    }

    @KafkaListener(topics = "Order-Completed", groupId = "coinOrderGroup", concurrency = "3")
    public void listen(CoinOrderDTO order) {
        System.out.println("---------------------------------------------------" + order);
    }
}