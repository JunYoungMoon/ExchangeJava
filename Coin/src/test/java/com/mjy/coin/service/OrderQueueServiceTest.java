package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class OrderQueueServiceTest {

    private OrderQueueService orderQueueService;

    @BeforeEach
    public void setUp() {
        // 테스트 전에 OrderQueueService 초기화
        orderQueueService = new OrderQueueService();
    }

    @Test
    public void memoryUsageTest() {
        // 메모리 사용량 측정을 위한 초기화
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Before creating orders, memory used: " + beforeMemory / (1024 * 1024) + " MB");

        // 임의의 CoinOrderDTO 생성 및 큐에 추가
        String marketKey = "BTC-KRW";
        orderQueueService.initializeBuyOrder(marketKey);

        for (int i = 1; i <= 4000000; i++) {
            CoinOrderDTO order = createRandomOrder(i);
            orderQueueService.addBuyOrder(marketKey, order);

            // 만 건 단위로 메모리 사용량 출력
            if (i % 10000 == 0) {
                long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                System.out.println("Orders added: " + i + ", Memory used: " + (currentMemory - beforeMemory) / (1024 * 1024) + " MB");
            }
        }

        // 전체 데이터 추가 후 메모리 사용량 출력
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("After creating orders, memory used: " + afterMemory / (1024 * 1024) + " MB");
    }


    // 임의의 CoinOrderDTO 생성 메서드
    private CoinOrderDTO createRandomOrder(int idx) {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setIdx((long) idx);
        order.setMemberIdx(new Random().nextLong());
        order.setMemberUuid("member-" + idx);
        order.setMarketName("KRW");
        order.setCoinName("BTC");
        order.setCoinAmount(BigDecimal.valueOf(new Random().nextInt(100) + 1)); // 1 ~ 100
        order.setOrderPrice(BigDecimal.valueOf(new Random().nextInt(100000) + 1000)); // 1000 ~ 101000
        order.setExecutionPrice(BigDecimal.valueOf(new Random().nextInt(100000) + 1000));
        order.setOrderType(idx % 2 == 0 ? OrderType.BUY : OrderType.SELL);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setFee(BigDecimal.valueOf(new Random().nextDouble() * 0.1));
        order.setCreatedAt(LocalDateTime.now());
        order.setMatchedAt(LocalDateTime.now());
        order.setMatchIdx("match-" + idx);
        order.setUuid("uuid-" + idx);
        return order;
    }

}