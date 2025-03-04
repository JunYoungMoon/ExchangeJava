package com.mjy.coin.component;

import com.mjy.coin.dto.CoinInfoDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class CoinInfoInitializer {

    private final OrderQueueService orderQueueService;
    private final OrderBookService orderBookService;
    private final CoinInfoService coinInfoService;
    private final RedisService redisService;
    private final ConvertService convertService;

    public CoinInfoInitializer(OrderQueueService orderQueueService,
                               OrderBookService orderBookService,
                               CoinInfoService coinInfoService,
                               RedisService redisService,
                               ConvertService convertService) {
        this.orderQueueService = orderQueueService;
        this.orderBookService = orderBookService;
        this.coinInfoService = coinInfoService;
        this.redisService = redisService;
        this.convertService = convertService;
    }

    @PostConstruct
    public void init() {
        List<String> keys = coinInfoService.getCoinMarketKeys();

        if (keys.isEmpty()) {
            List<CoinInfoDTO> coinInfoList = Arrays.asList(
                    new CoinInfoDTO("KRW","BTC", new BigDecimal("0.01"), "MAJOR"),
                    new CoinInfoDTO("KRW","ETH", new BigDecimal("0.02"), "MAJOR"),
                    new CoinInfoDTO("KRW","EGX", new BigDecimal("0.03"), "MINOR")
            );

            // 코인 정보를 MAJOR:BTC-KRW 와 같이 해시 테이블 생성
            for (CoinInfoDTO coinInfo : coinInfoList) {
                Map<String, String> coinInfoMap = new HashMap<>();

                coinInfoMap.put("coinName", String.valueOf(coinInfo.getCoinName()));
                coinInfoMap.put("coinType", String.valueOf(coinInfo.getCoinType()));
                coinInfoMap.put("marketName", String.valueOf(coinInfo.getMarketName()));
                coinInfoMap.put("feeRate", String.valueOf(coinInfo.getFeeRate()));

                redisService.setHashOps(coinInfo.getCoinType() + ":COIN:" + coinInfo.getCoinName() + "-" + coinInfo.getMarketName(), coinInfoMap);
            }
        }

        for (String key : keys) {
            // 주문 우선순위큐, 호가 트리 자료구조 생성
            orderQueueService.initializeBuyOrder(key);
            orderQueueService.initializeSellOrder(key);
            orderBookService.initializeBuyOrderBook(key);
            orderBookService.initializeSellOrderBook(key);

            // Redis에서 해당 코인-마켓 조합의 모든 데이터를 조회
            Map<String, String> redisOrders = redisService.getAllHashOps("PENDING:ORDER:" + key);

            // Redis에서 가져온 데이터를 CoinOrderDTO로 변환 후 처리
            for (String orderData : redisOrders.values()) {
                // JSON 문자열을 CoinOrderDTO 객체로 변환
                CoinOrderDTO orderDTO = convertService.convertStringToObject(orderData, CoinOrderDTO.class);

                // 매수 주문일 경우
                if (orderDTO.getOrderType() == OrderType.BUY) {
                    orderQueueService.addBuyOrder(key, orderDTO);
                    orderBookService.addBuyOrderBook(key, orderDTO);
                }
                // 매도 주문일 경우
                else if (orderDTO.getOrderType() == OrderType.SELL) {
                    orderQueueService.addSellOrder(key, orderDTO);
                    orderBookService.addSellOrderBook(key, orderDTO);
                }
            }
        }

        System.out.println("Buy/Sell queues initialized with Redis keys and DB pending orders.");
    }
}