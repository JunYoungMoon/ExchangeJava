package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CompletedOrderReader implements ItemReader<CoinOrderDTO> {

    private final RedisService redisService;
    private final CoinInfoService coinInfoService;

    @Autowired
    public CompletedOrderReader(RedisService redisService, CoinInfoService coinInfoService) {
        this.redisService = redisService;
        this.coinInfoService = coinInfoService;
    }

    @Override
    public CoinOrderDTO read() throws Exception {
        List<String> keys = coinInfoService.getCoinMarketKeys();

        int currentKeyIndex = 0; // 현재 키 인덱스

        // 키 리스트가 비어있다면, CoinInfoService를 통해 키를 초기화
        if (keys == null) {
            keys = coinInfoService.getCoinMarketKeys();
        }

        // 현재 키를 사용하여 Cursor 생성
        while (currentKeyIndex < keys.size()) {
            String key = keys.get(currentKeyIndex);
            try (Cursor<Map.Entry<String, String>> cursor = redisService.scanCursor(key)) {
                // Cursor를 통해 데이터를 읽음
                while (cursor.hasNext()) {
                    Map.Entry<String, String> entry = cursor.next();

                    CoinOrderDTO order = redisService.convertStringToObject(entry.getValue(), CoinOrderDTO.class);

                    if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus().name())) {
                        return order; // orderStatus가 COMPLETED인 경우 반환
                    }
                }
            } // try-with-resources를 사용하여 Cursor 자동 닫기

            // 현재 키의 Cursor가 끝나면 다음 키로 이동
            currentKeyIndex++; // 다음 키 인덱스로 이동
        }

        return null; // 더 이상 데이터가 없을 경우 null 반환
    }
}