package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class OrderQueueService {
    private final Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
    private final Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

    // 초기 매수 주문 큐 생성 메서드
    public void initializeBuyOrder(String symbol) {
        buyOrderQueues.putIfAbsent(symbol, new PriorityQueue<>(11,
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
    }

    // 초기 매도 주문 큐 생성 메서드
    public void initializeSellOrder(String symbol) {
        sellOrderQueues.putIfAbsent(symbol, new PriorityQueue<>(11,
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
    }

    // 매수 주문 추가
    public void addBuyOrder(String symbol, CoinOrderDTO order) {
        buyOrderQueues.get(symbol).add(order);
    }

    // 매도 주문 추가
    public void addSellOrder(String symbol, CoinOrderDTO order) {
        sellOrderQueues.get(symbol).add(order);
    }

    // 매수 주문 큐 조회 메서드
    public PriorityQueue<CoinOrderDTO> getBuyOrderQueue(String symbol) {
        return buyOrderQueues.getOrDefault(symbol, new PriorityQueue<>());
    }

    // 매도 주문 큐 조회 메서드
    public PriorityQueue<CoinOrderDTO> getSellOrderQueue(String symbol) {
        return sellOrderQueues.getOrDefault(symbol, new PriorityQueue<>());
    }
}
