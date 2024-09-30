package com.mjy.coin.component;

import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import com.mjy.coin.dto.CoinOrderMapper;

@Component
public class KafkaTemplateListener implements MessageListener<String, CoinOrderDTO> {

    private final OrderMatcher priorityQueueManager;
    private final MasterCoinOrderRepository masterCoinOrderRepository;

    @Autowired
    public KafkaTemplateListener(OrderMatcher priorityQueueManager, MasterCoinOrderRepository masterCoinOrderRepository) {
        this.priorityQueueManager = priorityQueueManager;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
    }

    @Override
    public void onMessage(ConsumerRecord<String, CoinOrderDTO> record) {
        String key = record.value().getCoinName() + "-" + record.value().getMarketName();
        CoinOrderDTO order = record.value();

        // DTO -> Entity 변환
        CoinOrder orderEntity = CoinOrderMapper.toEntity(order);

        try {
            // DB에 저장 (저장된 엔티티 반환)
            CoinOrder savedOrderEntity = masterCoinOrderRepository.save(orderEntity);

            // 저장된 엔티티에서 idx 가져와서 DTO에 설정
            order.setIdx(savedOrderEntity.getIdx());

            // 저장이 성공했으므로 매수/매도 큐에 추가
            if (order.getOrderType() == OrderType.BUY) {
                priorityQueueManager.addBuyOrder(key, order);
            } else if (order.getOrderType() == OrderType.SELL) {
                priorityQueueManager.addSellOrder(key, order);
            }

            // 주문 체결 시도
            priorityQueueManager.matchOrders(key);
        } catch (Exception e) {
            // 예외 처리: 로그를 기록하거나 필요한 조치를 수행
            System.err.println("Failed to save order: " + e.getMessage());
        }
    }
}