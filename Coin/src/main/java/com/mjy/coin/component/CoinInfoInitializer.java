package com.mjy.coin.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.entity.exchange.CoinInfo;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.repository.exchange.slave.SlaveCoinInfoRepository;
import com.mjy.coin.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class CoinInfoInitializer {

    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final RedisService redisService;

    public CoinInfoInitializer(RedisService redisService, SlaveCoinOrderRepository slaveCoinOrderRepository) {
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.redisService = redisService;
    }

    private String coinTypeEnv;

    @PostConstruct
    public void init() {
        // 각 코인과 마켓별 우선순위 큐를 초기화
        List<CoinOrder> pendingOrders = slaveCoinOrderRepository.findPendingOrders();
        Map<String, Map<String, PriorityQueue<CoinOrder>>> buyOrderQueues = new HashMap<>();
        Map<String, Map<String, PriorityQueue<CoinOrder>>> sellOrderQueues = new HashMap<>();

        for (CoinOrder order : pendingOrders) {
            String coinName = order.getCoinName();
            String marketName = order.getMarketName ();

            // 매수 주문일 경우
            if (order.getOrderType() == CoinOrder.OrderType.BUY) {
                buyOrderQueues.putIfAbsent(coinName, new HashMap<>());
                buyOrderQueues.get(coinName).putIfAbsent(marketName, new PriorityQueue<>(
                        Comparator.comparing(CoinOrder::getOrderPrice).reversed()
                                .thenComparing(CoinOrder::getCreatedAt)
                ));
                buyOrderQueues.get(coinName).get(marketName).add(order);

                // 매도 주문일 경우
            } else if (order.getOrderType() == CoinOrder.OrderType.SELL) {
                sellOrderQueues.putIfAbsent(coinName, new HashMap<>());
                sellOrderQueues.get(coinName).putIfAbsent(marketName, new PriorityQueue<>(
                        Comparator.comparing(CoinOrder::getOrderPrice)
                                .thenComparing(CoinOrder::getCreatedAt)
                ));
                sellOrderQueues.get(coinName).get(marketName).add(order);
            }
        }

        // 큐 초기화 완료 메시지 출력
        System.out.println("Buy/Sell queues initialized with pending orders.");
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


}
