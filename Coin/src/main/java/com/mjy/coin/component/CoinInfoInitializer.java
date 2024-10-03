package com.mjy.coin.component;

import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.dto.CoinOrderDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class CoinInfoInitializer {

    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final OrderMatcher priorityQueueManager;
    private final OrderBookManager orderBookManager;

    public CoinInfoInitializer(SlaveCoinOrderRepository slaveCoinOrderRepository, OrderMatcher priorityQueueManager, OrderBookManager orderBookManager) {
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.priorityQueueManager = priorityQueueManager;
        this.orderBookManager = orderBookManager;
    }

    @PostConstruct
    public void init() {
        // 각 코인과 마켓별 우선순위 큐를 초기화
        List<CoinOrder> pendingOrders = slaveCoinOrderRepository.findPendingOrders();

        Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
        Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

        // CoinOrder 엔티티 -> CoinOrderDTO 변환
        List<CoinOrderDTO> pendingOrderVOs = pendingOrders.stream()
                .map(CoinOrderDTO::fromEntity) // CoinOrderEntity에서 CoinOrderDTO로 변환하는 메서드
                .collect(Collectors.toList());

        for (CoinOrderDTO order : pendingOrderVOs) {
            String coinName = order.getCoinName();
            String marketName = order.getMarketName();

            String key = coinName + "-" + marketName;

            // 매수 주문일 경우
            if (order.getOrderType() == OrderType.BUY) {
                buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                        Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                                .thenComparing(CoinOrderDTO::getCreatedAt)
                ));
                buyOrderQueues.get(key).add(order);
            }

            // 매도 주문일 경우
            else if (order.getOrderType() == OrderType.SELL) {
                sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                        Comparator.comparing(CoinOrderDTO::getOrderPrice)
                                .thenComparing(CoinOrderDTO::getCreatedAt)
                ));
                sellOrderQueues.get(key).add(order);
            }
        }

        // 초기화된 큐를 PriorityQueueManager에 전달
        priorityQueueManager.initializeQueues(buyOrderQueues, sellOrderQueues);

        // 초기화된 큐를 OrderBookManager에 전달하여 호가 리스트 초기화
        orderBookManager.initializeOrderBook(buyOrderQueues, sellOrderQueues);

        System.out.println("Buy/Sell queues and order books initialized with pending orders.");
    }
}