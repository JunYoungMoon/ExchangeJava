package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.service.AsyncOrderService;
import com.mjy.coin.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class CompletedOrderListener {

    private final RedisService redisService;
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final AsyncOrderService asyncOrderService;
    private final KafkaTemplate<String, CoinOrderDTO> kafkaTemplate;

    @Autowired
    public CompletedOrderListener(RedisService redisService,
                                  MasterCoinOrderRepository masterCoinOrderRepository,
                                  SlaveCoinOrderRepository slaveCoinOrderRepository,
                                  AsyncOrderService asyncOrderService,
                                  KafkaTemplate<String, CoinOrderDTO> kafkaTemplate) {
        this.redisService = redisService;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.asyncOrderService = asyncOrderService;
        this.kafkaTemplate = kafkaTemplate;
    }

//    @KafkaListener(topics = "Order-Completed", groupId = "coinOrderGroup", concurrency = "10")
//    public void listen(CoinOrderDTO order) {
//        if (slaveCoinOrderRepository.findByUuid(order.getUuid()).isEmpty()) {
//            asyncOrderService.saveOrderAsync(order, masterCoinOrderRepository, redisService)
//                    .exceptionally(e -> {
//                        System.err.println("Error processing completed order asynchronously: " + e.getMessage());
//                        kafkaTemplate.send("Order-Completed", order.getCoinName() + "-" + order.getMarketName(), order);
//                        return null;
//                    });
//        }
//    }

//    @Async
//    public CompletableFuture<Void> saveOrderAsync(CoinOrderDTO order, MasterCoinOrderRepository masterRepo, RedisService redisService) {
//        try {
//            // MySQL에 비동기로 저장
//            masterRepo.save(CoinOrderMapper.toEntity(order));
//
//            // Redis에서 UUID 삭제
//            redisService.deleteHashOps(order.getCoinName() + "-" + order.getMarketName(), order.getUuid());
//
//            return CompletableFuture.completedFuture(null);
//        } catch (Exception e) {
//            // 예외 처리 로직
//            return CompletableFuture.failedFuture(e);
//        }
//    }

    @KafkaListener(topics = "Order-Completed", groupId = "coinOrderGroup", concurrency = "4")
    public void listen(CoinOrderDTO order) {
        if (slaveCoinOrderRepository.findByUuid(order.getUuid()).isEmpty()) {
            // 동기적으로 주문 저장
            try {
                saveOrderSync(order);
            } catch (Exception e) {
                System.err.println("Error processing completed order: " + e.getMessage());
                kafkaTemplate.send("Order-Completed", order.getCoinName() + "-" + order.getMarketName(), order);
            }
        }
    }

    private void saveOrderSync(CoinOrderDTO order) {
        // MySQL에 동기로 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));

        // Redis에서 UUID 삭제
        redisService.deleteHashOps(order.getCoinName() + "-" + order.getMarketName(), order.getUuid());
    }
}
