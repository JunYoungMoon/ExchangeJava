package com.mjy.exchange.service;

import com.mjy.exchange.dto.CoinOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mjy.exchange.enums.OrderType.*;
import static com.mjy.exchange.enums.OrderStatus.*;

@SpringBootTest
public class CoinOrderProducerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, CoinOrder> kafkaTemplate;

    @Test
    public void testSendCoinOrder() throws InterruptedException {
        CoinOrder coinOrder = new CoinOrder();
        coinOrder.setMemberId(2L);
        coinOrder.setMarketName("KRW");
        coinOrder.setCoinName("BTC");
        coinOrder.setCoinAmount(new BigDecimal("0.001")); // 랜덤 금액 추가
        coinOrder.setOrderPrice(new BigDecimal("50000")); // 랜덤 가격 추가
        coinOrder.setOrderType(BUY);
        coinOrder.setOrderStatus(PENDING);
        coinOrder.setFee(new BigDecimal("0.01"));
        coinOrder.setCreatedAt(LocalDateTime.now());
        kafkaTemplate.send("BTC-KRW", coinOrder);
        System.out.println("Sent BTC order: " + coinOrder);
//
//        // ETH 주문 생성
//        CoinOrder coinOrder2 = new CoinOrder();
//        coinOrder2.setMemberId(1L);
//        coinOrder2.setMarketName("KRW");
//        coinOrder2.setCoinName("ETH");
//        coinOrder2.setCoinAmount(new BigDecimal("0.02")); // 랜덤 금액 추가
//        coinOrder2.setOrderPrice(new BigDecimal("52340")); // 랜덤 가격 추가
//        coinOrder2.setOrderType(SELL);
//        coinOrder2.setCreatedAt(LocalDateTime.now());
//        kafkaTemplate.send("ETH-KRW", coinOrder2);
//        System.out.println("Sent ETH order: " + coinOrder2);
//
//        Random random = new Random();
//        long startTime = System.currentTimeMillis(); // 현재 시간 저장
//        long duration = 30000; // 30초
//
//        while (System.currentTimeMillis() - startTime < duration) {
//            // BTC 주문 생성
//            CoinOrder coinOrder = new CoinOrder();
//            coinOrder.setMemberId(1L);
//            coinOrder.setMarketName("KRW");
//            coinOrder.setCoinName("BTC");
//            coinOrder.setCoinAmount(new BigDecimal("0.01").add(new BigDecimal(random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))); // 랜덤 금액 추가
//            coinOrder.setOrderPrice(new BigDecimal("50000").add(new BigDecimal(random.nextInt(1000)))); // 랜덤 가격 추가
//            kafkaTemplate.send("BTC-KRW", coinOrder);
//            System.out.println("Sent BTC order: " + coinOrder);
//
//            // ETH 주문 생성
//            CoinOrder coinOrder2 = new CoinOrder();
//            coinOrder2.setMemberId(1L);
//            coinOrder2.setMarketName("KRW");
//            coinOrder2.setCoinName("ETH");
//            coinOrder2.setCoinAmount(new BigDecimal("0.02").add(new BigDecimal(random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))); // 랜덤 금액 추가
//            coinOrder2.setOrderPrice(new BigDecimal("52340").add(new BigDecimal(random.nextInt(1000)))); // 랜덤 가격 추가
//            kafkaTemplate.send("ETH-KRW", coinOrder2);
//            System.out.println("Sent ETH order: " + coinOrder2);
//        }
    }
}