package com.mjy.coin.service;

import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CoinOrderService {

    private final MasterCoinOrderRepository masterCoinOrderRepository;

    @Transactional
    public void generateCoinOrders(int totalOrders) {
        Random random = new Random();
        List<CoinOrder> batch = new ArrayList<>();
        int batchSize = 1000; // 한 번에 저장할 배치 사이즈

        for (int i = 0; i < 100000; i++) {
            CoinOrder coinOrder = new CoinOrder();
            coinOrder.setMemberId((long) random.nextInt(10000)); // 1만 명의 사용자 중 무작위 선택
            coinOrder.setMarketName("KRW");
            coinOrder.setCoinName(random.nextBoolean() ? "BTC" : "ETH"); // BTC 또는 ETH 선택
            coinOrder.setOrderType(random.nextBoolean() ? CoinOrder.OrderType.BUY : CoinOrder.OrderType.SELL); // 매수 또는 매도
            coinOrder.setCoinAmount(BigDecimal.valueOf(0.1 + (random.nextDouble() * 10))); // 0.1 ~ 10.0 코인
            coinOrder.setOrderPrice(BigDecimal.valueOf(1000 + (random.nextInt(9000)))); // 1000 ~ 10000 금액
            coinOrder.setOrderStatus(CoinOrder.OrderStatus.PENDING); // 미체결로 기본 설정
            coinOrder.setFee(BigDecimal.valueOf(0.01)); // 수수료 1%
            coinOrder.setCreatedAt(LocalDateTime.now());

            batch.add(coinOrder);

            // 일정 배치 크기마다 저장
            if (batch.size() == batchSize) {
                masterCoinOrderRepository.saveAll(batch);
                batch.clear();
            }
        }

        // 남은 데이터를 저장
        if (!batch.isEmpty()) {
            masterCoinOrderRepository.saveAll(batch);
        }
    }
}