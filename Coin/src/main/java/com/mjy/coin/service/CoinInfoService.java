package com.mjy.coin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.entity.exchange.CoinInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CoinInfoService {
    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getCoinMarketKeys() throws JsonProcessingException {
        // COIN_TYPE 환경 변수 가져오기
        String coinTypeEnv = System.getenv("COIN_TYPE");
        if (coinTypeEnv == null) {
            coinTypeEnv = "MAJOR"; // 기본값 설정
        }

        // Redis에서 코인-마켓 JSON 데이터 가져오기
        String jsonData = redisService.getValues(coinTypeEnv);

        // JSON 문자열을 List<CoinInfo>로 변환
        CoinInfo[] coinInfoList = objectMapper.readValue(jsonData, CoinInfo[].class);

        // Redis에서 가져온 코인-마켓 조합으로 키 생성
        List<String> keys = new ArrayList<>();
        for (CoinInfo coinInfo : coinInfoList) {
            String coinName = coinInfo.getCoinName();
            String marketName = coinInfo.getMarketName();
            keys.add(coinName + "-" + marketName); // 키를 리스트에 추가
        }

        return keys;
    }
}
