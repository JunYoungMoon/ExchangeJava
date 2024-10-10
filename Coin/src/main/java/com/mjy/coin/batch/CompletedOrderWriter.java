package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompletedOrderWriter implements ItemWriter<CoinOrderDTO> {

    private final RedisService redisService;
    private final CoinInfoService coinInfoService;

    @Autowired
    public CompletedOrderWriter(RedisService redisService, CoinInfoService coinInfoService) {
        this.redisService = redisService;
        this.coinInfoService = coinInfoService;
    }

    @Override
    public void write(Chunk<? extends CoinOrderDTO> chunk) throws Exception {
        List<String> keys = coinInfoService.getCoinMarketKeys();

        for (String key : keys) {
            for (CoinOrderDTO order : chunk) {
                redisService.deleteHashOps(key, order.getUuid()); // Redis에서 삭제
            }
        }
    }
}