package com.mjy.coin.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CoinInfoInitializer {

    private final PendingOrderMatcherService priorityQueueManager;
    private final OrderBookService orderBookService;
    private final CoinInfoService coinInfoService;
    private final RedisService redisService;
    private final ConvertService convertService;

    public CoinInfoInitializer(PendingOrderMatcherService priorityQueueManager,
                               OrderBookService orderBookService,
                               CoinInfoService coinInfoService,
                               RedisService redisService,
                               ConvertService convertService) {
        this.priorityQueueManager = priorityQueueManager;
        this.orderBookService = orderBookService;
        this.coinInfoService = coinInfoService;
        this.redisService = redisService;
        this.convertService = convertService;
    }

    @PostConstruct
    public void init() {
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

            // Redis에서 해당 코인-마켓 조합의 모든 데이터를 조회
            Map<String, String> redisOrders = redisService.getAllHashOps("PENDING:ORDER:" + key);

            // Redis에서 가져온 데이터를 CoinOrderDTO로 변환 후 처리
            for (String orderData : redisOrders.values()) {
                // JSON 문자열을 CoinOrderDTO 객체로 변환
                CoinOrderDTO orderDTO = convertService.convertStringToObject(orderData, CoinOrderDTO.class);

                // 매수 주문일 경우
                if (orderDTO.getOrderType() == OrderType.BUY) {
                    buyOrderQueues.get(key).add(orderDTO);
                }
                // 매도 주문일 경우
                else if (orderDTO.getOrderType() == OrderType.SELL) {
                    sellOrderQueues.get(key).add(orderDTO);
                }
            }
        }

        // 초기화된 큐를 PriorityQueueManager에 전달
        priorityQueueManager.initializeQueues(buyOrderQueues, sellOrderQueues);

        // 초기화된 큐를 OrderBookManager에 전달하여 호가 리스트 초기화
        orderBookService.initializeOrderBook(buyOrderQueues, sellOrderQueues);

        System.out.println("Buy/Sell queues initialized with Redis keys and DB pending orders.");
    }
}