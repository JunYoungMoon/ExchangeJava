package com.mjy.coin.component;

import com.mjy.coin.entity.coin.CoinOrder;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class PriorityQueueManager {

    private Map<String, PriorityQueue<CoinOrder>> buyOrderQueues = new HashMap<>();
    private Map<String, PriorityQueue<CoinOrder>> sellOrderQueues = new HashMap<>();

    // 큐 초기화
    public void initializeQueues(Map<String, PriorityQueue<CoinOrder>> buyQueues, Map<String, PriorityQueue<CoinOrder>> sellQueues) {
        this.buyOrderQueues = buyQueues;
        this.sellOrderQueues = sellQueues;
    }

    // 매수 주문 추가
    public void addBuyOrder(String key, CoinOrder order) {
        buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrder::getOrderPrice).reversed()
                        .thenComparing(CoinOrder::getCreatedAt)
        ));
        buyOrderQueues.get(key).add(order);
    }

    // 매도 주문 추가
    public void addSellOrder(String key, CoinOrder order) {
        sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrder::getOrderPrice)
                        .thenComparing(CoinOrder::getCreatedAt)
        ));
        sellOrderQueues.get(key).add(order);
    }

    // 매수 주문 조회
    public PriorityQueue<CoinOrder> getBuyOrders(String key) {
        return buyOrderQueues.get(key);
    }

    // 매도 주문 조회
    public PriorityQueue<CoinOrder> getSellOrders(String key) {
        return sellOrderQueues.get(key);
    }

    // 체결 로직
    public void matchOrders(String key) {
        PriorityQueue<CoinOrder> buyOrders = buyOrderQueues.get(key);
        PriorityQueue<CoinOrder> sellOrders = sellOrderQueues.get(key);

        if (buyOrders != null && sellOrders != null) {
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                CoinOrder buyOrder = buyOrders.peek();
                CoinOrder sellOrder = sellOrders.peek();

                // 체결 가능 조건 확인
                if (buyOrder.getOrderPrice().compareTo(sellOrder.getOrderPrice()) >= 0) {
                    // 체결 처리 로직
                    System.out.println("Matched: " + buyOrder + " with " + sellOrder);

                    // 체결된 주문 제거
                    buyOrders.poll();
                    sellOrders.poll();
                } else {
                    break; // 더 이상 체결할 수 없으면 중단
                }
            }
        }
    }
}
