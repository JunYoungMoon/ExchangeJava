package com.mjy.coin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CoinInfoService {
    private final RedisService redisService;

    public List<String> getCoinMarketKeys() {
        // COIN_TYPE 환경 변수 가져오기
        String coinTypeEnv = System.getenv("COIN_TYPE");
        if (coinTypeEnv == null) {
            coinTypeEnv = "MAJOR"; // 기본값 설정
        }

        Set<String> redisKeys = redisService.getKeys(coinTypeEnv + ":COIN:*");

        List<String> keys = new ArrayList<>();

        for (String redisKey : redisKeys) {
            String[] parts = redisKey.split(":");
            keys.add(parts[2]);
        }

        return keys;
    }
}
