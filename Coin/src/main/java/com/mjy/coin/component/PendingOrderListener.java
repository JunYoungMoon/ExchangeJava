package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;


@Component
public class PendingOrderListener implements MessageListener<String, CoinOrderDTO> {

    private final OrderProcessor orderProcessor;

    @Autowired
    public PendingOrderListener(OrderProcessor orderProcessor) {
        this.orderProcessor = orderProcessor;
    }

    @Override
    public void onMessage(ConsumerRecord<String, CoinOrderDTO> record) {
        CoinOrderDTO order = record.value();

        // 미체결 주문 처리
        orderProcessor.processOrder(order);
        System.out.println("Pending order processed: " + order);
    }
}