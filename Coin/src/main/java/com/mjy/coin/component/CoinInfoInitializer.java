package com.mjy.coin.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CoinInfoService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CoinInfoInitializer {

    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final OrderMatcher priorityQueueManager;
    private final OrderBookManager orderBookManager;
    private final CoinInfoService coinInfoService;

    public CoinInfoInitializer(SlaveCoinOrderRepository slaveCoinOrderRepository, OrderMatcher priorityQueueManager, OrderBookManager orderBookManager, CoinInfoService coinInfoService) {
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.priorityQueueManager = priorityQueueManager;
        this.orderBookManager = orderBookManager;
        this.coinInfoService = coinInfoService;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        List<String> keys = coinInfoService.getCoinMarketKeys();

        // 매수/매도 주문을 저장할 맵
        Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
        Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

        for (String key : keys) {
            // 각 코인-마켓 조합에 대해 매수/매도 큐를 미리 초기화
            buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                    Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                            .thenComparing(CoinOrderDTO::getCreatedAt)
            ));
            sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                    Comparator.comparing(CoinOrderDTO::getOrderPrice)
                            .thenComparing(CoinOrderDTO::getCreatedAt)
            ));
        }

        // DB에서 모든 미체결 주문을 조회
        List<CoinOrder> pendingOrders = slaveCoinOrderRepository.findPendingOrders();

        // 미체결 주문을 해당 코인-마켓 조합에 추가
        for (CoinOrder order : pendingOrders) {
            CoinOrderDTO orderDTO = CoinOrderDTO.fromEntity(order);
            String key = orderDTO.getCoinName() + "-" + orderDTO.getMarketName();

            // 매수 주문일 경우
            if (orderDTO.getOrderType() == OrderType.BUY) {
                buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                        Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                                .thenComparing(CoinOrderDTO::getCreatedAt)
                ));
                buyOrderQueues.get(key).add(orderDTO);
            }

            // 매도 주문일 경우
            else if (orderDTO.getOrderType() == OrderType.SELL) {
                sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                        Comparator.comparing(CoinOrderDTO::getOrderPrice)
                                .thenComparing(CoinOrderDTO::getCreatedAt)
                ));
                sellOrderQueues.get(key).add(orderDTO);
            }
        }

        // 초기화된 큐를 PriorityQueueManager에 전달
        priorityQueueManager.initializeQueues(buyOrderQueues, sellOrderQueues);

        // 초기화된 큐를 OrderBookManager에 전달하여 호가 리스트 초기화
        orderBookManager.initializeOrderBook(buyOrderQueues, sellOrderQueues);

        System.out.println("Buy/Sell queues initialized with Redis keys and DB pending orders.");
    }
}