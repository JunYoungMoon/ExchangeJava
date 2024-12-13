package com.mjy.exchange.service;

import com.mjy.exchange.dto.CoinOrder;
import com.mjy.exchange.enums.OrderStatus;
import com.mjy.exchange.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Random;

import static com.mjy.exchange.enums.OrderType.*;
import static com.mjy.exchange.enums.OrderStatus.*;

@SpringBootTest
public class CoinOrderProducerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, CoinOrder> kafkaTemplate;

    @Test
    public void testSendCoinOrder() throws InterruptedException {

//        CoinOrder coinOrder1 = new CoinOrder();
//        coinOrder1.setMemberUuid("cfccbb28-f07d-4e7c-8bd2-4cbd720aceab");
//        coinOrder1.setMarketName("KRW");
//        coinOrder1.setCoinName("BTC");
//        coinOrder1.setCoinAmount(new BigDecimal("0.06")); // 랜덤 금액 추가
//        coinOrder1.setOrderPrice(new BigDecimal("5300")); // 랜덤 가격 추가
//        coinOrder1.setOrderType(SELL);
//        coinOrder1.setOrderStatus(PENDING);
//        coinOrder1.setFee(new BigDecimal("0.03"));   //수수료
//        coinOrder1.setCreatedAt(LocalDateTime.now());
//        kafkaTemplate.send("BTC-KRW", coinOrder1);
//        System.out.println("Sent BTC order: " + coinOrder1);
//
        CoinOrder coinOrder2 = new CoinOrder();
        coinOrder2.setMemberUuid("2b005552-ee2b-4851-8857-6e595800395d");
        coinOrder2.setMarketName("KRW");
        coinOrder2.setCoinName("BTC");
        coinOrder2.setCoinAmount(new BigDecimal("0.04")); // 랜덤 금액 추가
        coinOrder2.setOrderPrice(new BigDecimal("5400")); // 랜덤 가격 추가
        coinOrder2.setOrderType(BUY);
        coinOrder2.setOrderStatus(PENDING);
        coinOrder2.setFee(new BigDecimal("0.01"));   //수수료
        coinOrder2.setCreatedAt(LocalDateTime.now());
        kafkaTemplate.send("BTC-KRW", coinOrder2);
        System.out.println("Sent BTC order: " + coinOrder2);

//        CoinOrder coinOrder3 = new CoinOrder();
//        coinOrder3.setMemberId(1L);
//        coinOrder3.setMarketName("KRW");
//        coinOrder3.setCoinName("BTC");
//        coinOrder3.setCoinAmount(new BigDecimal("0.03")); // 랜덤 금액 추가
//        coinOrder3.setOrderPrice(new BigDecimal("5300")); // 랜덤 가격 추가
//        coinOrder3.setOrderType(BUY);
//        coinOrder3.setOrderStatus(PENDING);
//        coinOrder3.setFee(new BigDecimal("0.01"));   //수수료
//        coinOrder3.setCreatedAt(LocalDateTime.now());
//        kafkaTemplate.send("BTC-KRW", coinOrder3);
//        System.out.println("Sent BTC order: " + coinOrder3);
//
//        CoinOrder coinOrder = new CoinOrder();
//        coinOrder.setMemberId(1L);
//        coinOrder.setMarketName("KRW");
//        coinOrder.setCoinName("BTC");
//        coinOrder.setCoinAmount(new BigDecimal("0.08")); // 랜덤 금액 추가
//        coinOrder.setOrderPrice(new BigDecimal("5000")); // 랜덤 가격 추가
//        coinOrder.setOrderType(SELL);
//        coinOrder.setOrderStatus(PENDING);
//        coinOrder.setFee(new BigDecimal("0.01"));   //수수료
//        coinOrder.setCreatedAt(LocalDateTime.now());
//        kafkaTemplate.send("BTC-KRW", coinOrder);
//        System.out.println("Sent BTC order: " + coinOrder);

//
//        CoinOrder coinOrder5 = new CoinOrder();
//        coinOrder5.setMemberId(2L);
//        coinOrder5.setMarketName("KRW");
//        coinOrder5.setCoinName("BTC");
//        coinOrder5.setCoinAmount(new BigDecimal("0.05")); // 랜덤 금액 추가
//        coinOrder5.setOrderPrice(new BigDecimal("5500")); // 랜덤 가격 추가
//        coinOrder5.setOrderType(BUY);
//        coinOrder5.setOrderStatus(PENDING);
//        coinOrder5.setFee(new BigDecimal("0.01"));  //수수료
//        coinOrder5.setCreatedAt(LocalDateTime.now());
//        kafkaTemplate.send("BTC-KRW", coinOrder5);
//        System.out.println("Sent BTC order: " + coinOrder5);

//        Random random = new Random();
//        int orderCount = 5000; // 생성할 주문 수
//
//        for (int i = 0; i < orderCount; i++) {
//            // BTC 주문 생성
//            CoinOrder coinOrder4 = new CoinOrder();
//
//
//            boolean nextBoolean = random.nextBoolean();
//            coinOrder4.setMemberIdx(nextBoolean ? 1L : 2L);
//            coinOrder4.setMemberUuid(nextBoolean ? "2b005552-ee2b-4851-8857-6e595800395d" : "cfccbb28-f07d-4e7c-8bd2-4cbd720aceab");
//            coinOrder4.setMarketName("KRW");
//
////            String randomCoinName = random.nextBoolean() ? "BTC" : "ETH";
//            String randomCoinName = "BTC";
//
//            coinOrder4.setCoinName(randomCoinName);
//
//            // 0.01 ~ 0.1 범위의 랜덤 금액
//            BigDecimal randomAmount = new BigDecimal(0.01 + (0.09 * random.nextDouble())).setScale(2, RoundingMode.DOWN);
//            coinOrder4.setCoinAmount(new BigDecimal(String.valueOf(randomAmount)));
//
//            // 5000 ~ 6000 범위에서 100원 단위로 랜덤 가격 생성
//            int randomPrice = 5000 + (random.nextInt(11) * 100); // 5000에서 6000까지 100원 단위 (5000 + 100*0~10)
//            coinOrder4.setOrderPrice(new BigDecimal(String.valueOf(randomPrice)));
//
//            // BUY 또는 SELL 중 랜덤 타입 선택
//            OrderType randomOrderType = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;
//            coinOrder4.setOrderType(randomOrderType);
//
//            // 주문 상태는 PENDING으로 고정
//            coinOrder4.setOrderStatus(OrderStatus.PENDING);
//
//            // 수수료는 0.01로 고정
//            coinOrder4.setFee(new BigDecimal("0.01"));
//
//            // 생성 시간 설정
//            coinOrder4.setCreatedAt(LocalDateTime.now());
//
//            // Kafka로 전송
//            kafkaTemplate.send("BTC-KRW", coinOrder4);
//        }
    }
}