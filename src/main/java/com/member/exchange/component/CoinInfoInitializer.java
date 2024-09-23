package com.member.exchange.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.member.exchange.entity.CoinInfo;
import com.member.exchange.repository.slave.SlaveCoinInfoRepository;
import com.member.exchange.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class CoinInfoInitializer {

    private final SlaveCoinInfoRepository slaveCoinInfoRepository;
    private final RedisService redisService;

    public CoinInfoInitializer(SlaveCoinInfoRepository slaveCoinInfoRepository, RedisService redisService) {
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        //코인 정보 redis 저장
        List<CoinInfo> coinInfoList = slaveCoinInfoRepository.findAll();

        redisService.setValues("CoinInfo ", new ObjectMapper().writeValueAsString(coinInfoList));

        System.out.println("CoinInfo list saved to Redis on startup.");

        // 주문 데이터 초기화
        Map<String, Map<String, Map<String, List<Object>>>> orderData = new HashMap<>();

        for (CoinInfo coinInfo : coinInfoList) {
            String coinType = coinInfo.getCoinName();
            String marketName = coinInfo.getMarketName();

            // Buy 배열 초기화
            orderData.putIfAbsent("buy", new HashMap<>());
            orderData.get("buy").putIfAbsent(coinType, new HashMap<>());
            orderData.get("buy").get(coinType).putIfAbsent(marketName, new ArrayList<>());

            // Sell 배열 초기화
            orderData.putIfAbsent("sell", new HashMap<>());
            orderData.get("sell").putIfAbsent(coinType, new HashMap<>());
            orderData.get("sell").get(coinType).putIfAbsent(marketName, new ArrayList<>());

//            // SQL 쿼리로 주문 데이터 조회
//            List<CoinOrderKRWBTC> buyOrders = slaveCoinOrderRepository.findOrdersByTypeAndState(coinType, marketName, "BUY", "PENDING");
//            List<CoinOrderKRWBTC> sellOrders = slaveCoinOrderRepository.findOrdersByTypeAndState(coinType, marketName, "SELL", "PENDING");
//
//            // 결과를 orderData에 추가
//            orderData.get("buy").get(coinType).get(marketName).addAll(buyOrders);
//            orderData.get("sell").get(coinType).get(marketName).addAll(sellOrders);
        }

        System.out.println(orderData);
    }
}
