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

    public CoinInfoInitializer(SlaveCoinOrderRepository slaveCoinOrderRepository, OrderMatcher priorityQueueManager) {
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.priorityQueueManager = priorityQueueManager;
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
        System.out.println("Buy/Sell queues initialized with pending orders.");
    }
}
//
//    @PostConstruct
//    public void init() throws JsonProcessingException {
//
//        this.coinTypeEnv = System.getenv("COIN_TYPE");
//
//        if (coinTypeEnv == null) {
//            coinTypeEnv = "MAJOR"; // 기본값 설정
//        }
//
//        String jsonData = redisService.getValues(coinTypeEnv);
//
//        // JSON 문자열을 List<CoinInfo>로 변환
//        CoinInfo[] coinInfoList = new ObjectMapper().readValue(jsonData, CoinInfo[].class);
//
//        Map<String, Map<String, Map<String, List<Object>>>> orderData = new HashMap<>();
//
//        for (CoinInfo coinInfo : coinInfoList) {
//            String coinType = coinInfo.getCoinName();
//            String marketName = coinInfo.getMarketName();
//
//            // Buy 배열 초기화
//            orderData.putIfAbsent("buy", new HashMap<>());
//            orderData.get("buy").putIfAbsent(coinType, new HashMap<>());
//            orderData.get("buy").get(coinType).putIfAbsent(marketName, new ArrayList<>());
//
//            // Sell 배열 초기화
//            orderData.putIfAbsent("sell", new HashMap<>());
//            orderData.get("sell").putIfAbsent(coinType, new HashMap<>());
//            orderData.get("sell").get(coinType).putIfAbsent(marketName, new ArrayList<>());
//        }
//    }


        // 주문 데이터 초기화
//        //코인 정보 redis 저장
//        List<CoinInfo> coinInfoList = slaveCoinInfoRepository.findAll();
//
//        redisService.setValues("CoinInfo ", new ObjectMapper().writeValueAsString(coinInfoList));
//
//        System.out.println("CoinInfo list saved to Redis on startup.");
//
//        // 주문 데이터 초기화
//        Map<String, Map<String, Map<String, List<Object>>>> orderData = new HashMap<>();
//
//        for (CoinInfo coinInfo : coinInfoList) {
//            String coinType = coinInfo.getCoinName();
//            String marketName = coinInfo.getMarketName();
//
//            // Buy 배열 초기화
//            orderData.putIfAbsent("buy", new HashMap<>());
//            orderData.get("buy").putIfAbsent(coinType, new HashMap<>());
//            orderData.get("buy").get(coinType).putIfAbsent(marketName, new ArrayList<>());
//
//            // Sell 배열 초기화
//            orderData.putIfAbsent("sell", new HashMap<>());
//            orderData.get("sell").putIfAbsent(coinType, new HashMap<>());
//            orderData.get("sell").get(coinType).putIfAbsent(marketName, new ArrayList<>());
//
////            // SQL 쿼리로 주문 데이터 조회
////            List<CoinOrderKRWBTC> buyOrders = slaveCoinOrderRepository.findOrdersByTypeAndState(coinType, marketName, "BUY", "PENDING");
////            List<CoinOrderKRWBTC> sellOrders = slaveCoinOrderRepository.findOrdersByTypeAndState(coinType, marketName, "SELL", "PENDING");
////
////            // 결과를 orderData에 추가
////            orderData.get("buy").get(coinType).get(marketName).addAll(buyOrders);
////            orderData.get("sell").get(coinType).get(marketName).addAll(sellOrders);
//        }
//
//        System.out.println(orderData);