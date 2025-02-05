//package com.mjy.coin.batch;
//
//import com.mjy.coin.dto.CoinOrderDTO;
//import com.mjy.coin.service.RedisService;
//import org.springframework.batch.core.StepExecution;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.scope.context.StepSynchronizationManager;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//import java.time.LocalDate;
//
//@Component
//@StepScope
//public class CoinOrderWriter implements ItemWriter<CoinOrderDTO> {
//
//    private final RedisService redisService;
//    private final String coinName;
//    private final LocalDate yesterday;
//
//    public CoinOrderWriter(RedisService redisService,
//                           @Value("#{stepExecutionContext['coinName']}") String coinName,
//                           @Value("#{stepExecutionContext['yesterday']}") LocalDate yesterday) {
//        this.redisService = redisService;
//        this.coinName = coinName;
//        this.yesterday = yesterday;
//    }
//
//    @Override
//    public void write(Chunk<? extends CoinOrderDTO> items) {
//        System.out.println("CoinOrderWriter.write" + items);
////        BigDecimal totalPrice = BigDecimal.ZERO;
////        BigDecimal totalVolume = BigDecimal.ZERO;
////
////        for (CoinOrderDTO item : items) {
////            totalPrice = totalPrice.add(item.getExecutionPrice().multiply(item.getCoinAmount()));
////            totalVolume = totalVolume.add(item.getCoinAmount());
////        }
////
////        // Redis에 쓰레드별 데이터 저장
////        String key = yesterday + ":" + coinName + ":partition:" + Thread.currentThread().getName();
////        redisService.incrementHashValue(key, "totalPrice", totalPrice.doubleValue(), Duration.ofDays(1));
////        redisService.incrementHashValue(key, "totalVolume", totalVolume.doubleValue(), Duration.ofDays(1));
//    }
//}