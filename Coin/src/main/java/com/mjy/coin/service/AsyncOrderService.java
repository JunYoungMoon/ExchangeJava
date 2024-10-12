package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncOrderService {

    @Async
    public CompletableFuture<Void> saveOrderAsync(CoinOrderDTO order, MasterCoinOrderRepository masterRepo, RedisService redisService) {
        try {
            // MySQL에 비동기로 저장
            masterRepo.save(CoinOrderMapper.toEntity(order));

            // Redis에서 UUID 삭제
            redisService.deleteHashOps(order.getCoinName() + "-" + order.getMarketName(), order.getUuid());

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            // 예외 처리 로직
            return CompletableFuture.failedFuture(e);
        }
    }
}