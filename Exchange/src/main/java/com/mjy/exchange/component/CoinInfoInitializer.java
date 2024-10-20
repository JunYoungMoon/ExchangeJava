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

    public CoinInfoInitializer(SlaveCoinInfoRepository slaveCoinInfoRepository,
                               RedisService redisService) {
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.redisService = redisService;
    }

    //모든 빈이 초기화된 후 실행 CoinInfoInitializer 실행
    //MasterDataSourceConfig에서 초기 데이터를 넣고 난뒤에 실행되어야 함
    @Override
    public void afterSingletonsInstantiated() {
        //코인 정보 redis 저장
        List<CoinInfo> coinInfoList = slaveCoinInfoRepository.findAll();

        // 코인 정보를 MAJOR:BTC-KRW 와 같이 해시 테이블 생성
        for (CoinInfo coinInfo : coinInfoList) {
            Map<String, String> coinInfoMap = new HashMap<>();

            coinInfoMap.put("coinName", String.valueOf(coinInfo.getCoinName()));
            coinInfoMap.put("coinType", String.valueOf(coinInfo.getCoinType()));
            coinInfoMap.put("marketName", String.valueOf(coinInfo.getMarketName()));
            coinInfoMap.put("feeRate", String.valueOf(coinInfo.getFeeRate()));

            redisService.setHashOps(coinInfo.getCoinType() + ":COIN:" + coinInfo.getCoinName() + "-" + coinInfo.getMarketName(), coinInfoMap);
        }
    }
}
