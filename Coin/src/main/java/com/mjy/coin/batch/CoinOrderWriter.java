package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CoinOrderWriter implements ItemWriter<CoinOrderDTO> {

    private final RedisTemplate<String, Object> redisTemplate;

    public CoinOrderWriter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void write(Chunk<? extends CoinOrderDTO> items) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        // 파티션별로 계산
        for (CoinOrderDTO item : items) {
            totalPrice = totalPrice.add(item.getExecutionPrice().multiply(item.getCoinAmount()));
            totalVolume = totalVolume.add(item.getCoinAmount());
        }

        StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();
        String yesterday = stepExecution.getJobExecution().getExecutionContext().getString("yesterday");

        // Redis에 파티션별 데이터 저장
        String key = yesterday + ":partition:" + Thread.currentThread().getName();
        redisTemplate.opsForHash().increment(key, "totalPrice", totalPrice.doubleValue());
        redisTemplate.opsForHash().increment(key, "totalVolume", totalVolume.doubleValue());
    }
}