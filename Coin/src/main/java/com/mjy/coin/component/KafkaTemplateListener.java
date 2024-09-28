package com.mjy.coin.component;

import com.mjy.coin.entity.coin.CoinOrder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaTemplateListener implements MessageListener<String, CoinOrder> {

    private final PriorityQueueManager priorityQueueManager;

    @Autowired
    public KafkaTemplateListener(PriorityQueueManager priorityQueueManager) {
        this.priorityQueueManager = priorityQueueManager;
    }

    @Override
    public void onMessage(ConsumerRecord<String, CoinOrder> record) {
        String key = record.value().getCoinName() + "-" + record.value().getMarketName();
        CoinOrder order = record.value();

        // 매수/매도 여부에 따라 해당 큐에 주문 추가
        if (order.getOrderType() == CoinOrder.OrderType.BUY) {
            priorityQueueManager.addBuyOrder(key, order);
        } else if (order.getOrderType() == CoinOrder.OrderType.SELL) {
            priorityQueueManager.addSellOrder(key, order);
        }

        // 주문 체결 시도
        priorityQueueManager.matchOrders(key);
    }
}