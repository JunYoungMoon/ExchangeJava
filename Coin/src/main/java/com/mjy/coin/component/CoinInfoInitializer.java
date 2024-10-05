package com.mjy.coin.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.entity.exchange.CoinInfo;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class CoinInfoInitializer {

    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final OrderMatcher priorityQueueManager;
    private final OrderBookManager orderBookManager;
    private final RedisService redisService;

    public CoinInfoInitializer(SlaveCoinOrderRepository slaveCoinOrderRepository, RedisService redisService, OrderMatcher priorityQueueManager, OrderBookManager orderBookManager) {
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.priorityQueueManager = priorityQueueManager;
        this.orderBookManager = orderBookManager;
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        // COIN_TYPE 환경 변수 가져오기
        String coinTypeEnv = System.getenv("COIN_TYPE");
        if (coinTypeEnv == null) {
            coinTypeEnv = "MAJOR"; // 기본값 설정
        }

        // Redis에서 코인-마켓 JSON 데이터 가져오기 (예: BTC-KRW, ETH-KRW 등)
        String jsonData = redisService.getValues(coinTypeEnv);

        // JSON 문자열을 List<CoinInfo>로 변환
        CoinInfo[] coinInfoList = new ObjectMapper().readValue(jsonData, CoinInfo[].class);

        // 매수/매도 주문을 저장할 맵
        Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
        Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

        // Redis에서 가져온 코인-마켓 조합으로 키 생성
        for (CoinInfo coinInfo : coinInfoList) {
            String coinName = coinInfo.getCoinName();
            String marketName = coinInfo.getMarketName();
            String key = coinName + "-" + marketName;

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