package com.mjy.exchange.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.repository.slave.SlaveCoinInfoRepository;
import com.mjy.exchange.service.RedisService;
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

        // coinType에 따라 분리하여 저장할 맵
        Map<String, List<CoinInfo>> groupedCoinInfo = new HashMap<>();

        // 코인 정보를 coinType에 따라 그룹화
        for (CoinInfo coinInfo : coinInfoList) {
            String coinType = coinInfo.getCoinType().name(); // MAJOR 또는 MINOR
            groupedCoinInfo.putIfAbsent(coinType, new ArrayList<>()); // 키가 없으면 새 리스트 생성
            groupedCoinInfo.get(coinType).add(coinInfo); // 해당 coinType 리스트에 추가
        }

        // Redis에 저장
        for (Map.Entry<String, List<CoinInfo>> entry : groupedCoinInfo.entrySet()) {
            String key = entry.getKey(); // MAJOR 또는 MINOR
            String jsonValue = new ObjectMapper().writeValueAsString(entry.getValue()); // JSON 문자열로 변환
            redisService.setValues(key, jsonValue); // Redis에 저장
            System.out.println(key + " list saved to Redis: " + jsonValue);
        }
    }
}
