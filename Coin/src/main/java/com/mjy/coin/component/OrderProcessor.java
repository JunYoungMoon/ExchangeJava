package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessor {

    private final OrderMatcher priorityQueueManager;
    private final OrderBookManager orderBookManager;
    private final MasterCoinOrderRepository masterCoinOrderRepository;

    @Autowired
    public OrderProcessor(OrderMatcher priorityQueueManager, MasterCoinOrderRepository masterCoinOrderRepository, OrderBookManager orderBookManager) {
        this.priorityQueueManager = priorityQueueManager;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookManager = orderBookManager;
    }

    public void processOrder(CoinOrderDTO order) {
        String key = order.getCoinName() + "-" + order.getMarketName();
        CoinOrder orderEntity = CoinOrderMapper.toEntity(order);

        try {
            // DB에 저장 (저장된 엔티티 반환)
            CoinOrder savedOrderEntity = masterCoinOrderRepository.save(orderEntity);

            // 저장된 엔티티에서 idx 가져와서 DTO에 설정
            order.setIdx(savedOrderEntity.getIdx());

            // 로그: 주문이 DB에 저장된 후
            System.out.println("Order saved: " + savedOrderEntity);

            // 저장이 성공했으므로 매수/매도 큐에 추가
            // 호가 리스트도 추가
            if (order.getOrderType() == OrderType.BUY) {
                System.out.println("Adding buy order to queue: " + order);
                priorityQueueManager.addBuyOrder(key, order);
                orderBookManager.updateOrderBook(key, order, true, true);
            } else if (order.getOrderType() == OrderType.SELL) {
                System.out.println("Adding sell order to queue: " + order);
                priorityQueueManager.addSellOrder(key, order);
                orderBookManager.updateOrderBook(key, order, false, true);
            }

            // 주문 체결 시도
            priorityQueueManager.matchOrders(key);

            //호가 리스트 출력
            orderBookManager.printOrderBook(key);
        } catch (Exception e) {
            // 예외 처리: 로그를 기록하거나 필요한 조치를 수행
            System.err.println("Failed to save order: " + e.getMessage());
        }
    }
}
