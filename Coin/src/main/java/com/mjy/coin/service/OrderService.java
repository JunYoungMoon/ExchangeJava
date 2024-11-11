package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrderService {
    private final Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
    private final Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

    // 초기 매수 주문 큐 생성 메서드
    public void initializeBuyOrder(String key) {
        buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
    }

    // 초기 매도 주문 큐 생성 메서드
    public void initializeSellOrder(String key) {
        sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
    }

    // 매수 주문 추가
    public void addBuyOrder(String key, CoinOrderDTO order) {
        buyOrderQueues.get(key).add(order);
    }

    // 매도 주문 추가
    public void addSellOrder(String key, CoinOrderDTO order) {
        sellOrderQueues.get(key).add(order);
    }

    // 매수 주문 큐 조회 메서드
    public PriorityQueue<CoinOrderDTO> getBuyOrderQueue(String key) {
        return buyOrderQueues.getOrDefault(key, new PriorityQueue<>());
    }

    // 매도 주문 큐 조회 메서드
    public PriorityQueue<CoinOrderDTO> getSellOrderQueue(String key) {
        return sellOrderQueues.getOrDefault(key, new PriorityQueue<>());
    }
}
