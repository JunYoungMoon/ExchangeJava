package com.mjy.exchange.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.repository.slave.SlaveCoinInfoRepository;
import com.mjy.exchange.service.RedisService;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoinInfoInitializer implements SmartInitializingSingleton {

    private final SlaveCoinInfoRepository slaveCoinInfoRepository;
    private final RedisService redisService;

    public CoinInfoInitializer(SlaveCoinInfoRepository slaveCoinInfoRepository, RedisService redisService) {
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.redisService = redisService;
    }

    //모든 빈이 초기화된 후 실행 CoinInfoInitializer 실행
    //MasterDataSourceConfig에서 초기 데이터를 넣고 난뒤에 실행되어야 함
    @Override
    public void afterSingletonsInstantiated() {
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
            String jsonValue = null; // JSON 문자열로 변환
            try {
                jsonValue = new ObjectMapper().writeValueAsString(entry.getValue());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            redisService.setValues(key, jsonValue); // Redis에 저장
            System.out.println(key + " list saved to Redis: " + jsonValue);
        }
    }
}
