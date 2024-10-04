package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class OrderBookManager {

    // 코인-마켓별로 독립적인 매수/매도 호가 리스트를 관리하는 맵
    private final Map<String, TreeMap<BigDecimal, BigDecimal>> buyOrderBooks = new HashMap<>();  // 매수: 높은 가격 우선
    private final Map<String, TreeMap<BigDecimal, BigDecimal>> sellOrderBooks = new HashMap<>(); // 매도: 낮은 가격 우선

    // 최초에 전체 호가 리스트를 초기화하는 메서드
    public void initializeOrderBook(Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues,
                                    Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues) {
        // 매수 주문 초기화
        for (Map.Entry<String, PriorityQueue<CoinOrderDTO>> entry : buyOrderQueues.entrySet()) {
            String key = entry.getKey();
            PriorityQueue<CoinOrderDTO> buyOrders = entry.getValue();
            buyOrderBooks.putIfAbsent(key, new TreeMap<>(Comparator.reverseOrder()));
            for (CoinOrderDTO buyOrder : buyOrders) {
                addOrderToBook(buyOrderBooks.get(key), buyOrder.getOrderPrice(), buyOrder.getCoinAmount());
            }
        }

        // 매도 주문 초기화
        for (Map.Entry<String, PriorityQueue<CoinOrderDTO>> entry : sellOrderQueues.entrySet()) {
            String key = entry.getKey();
            PriorityQueue<CoinOrderDTO> sellOrders = entry.getValue();
            sellOrderBooks.putIfAbsent(key, new TreeMap<>());
            for (CoinOrderDTO sellOrder : sellOrders) {
                addOrderToBook(sellOrderBooks.get(key), sellOrder.getOrderPrice(), sellOrder.getCoinAmount());
            }
        }
    }

    // 주문을 추가/업데이트하는 메서드
    private void addOrderToBook(TreeMap<BigDecimal, BigDecimal> orderBook, BigDecimal price, BigDecimal amount) {
        orderBook.merge(price, amount, BigDecimal::add);
    }

    // 주문이 체결되거나 취소될 때 수량을 감소시키는 메서드
    private void subtractOrderFromBook(TreeMap<BigDecimal, BigDecimal> orderBook, BigDecimal price, BigDecimal amount) {
        orderBook.merge(price, amount.negate(), BigDecimal::add);

        // 수량이 0인 경우 해당 가격대를 제거
        if (orderBook.get(price).compareTo(BigDecimal.ZERO) <= 0) {
            orderBook.remove(price);
        }
    }

    // 주문 체결 시 수량 업데이트
    public void updateOrderBook(String key, CoinOrderDTO order, boolean isBuy, boolean isAdd) {
        if (isBuy) {
            if (isAdd) {
                addOrderToBook(buyOrderBooks.get(key), order.getOrderPrice(), order.getCoinAmount());
            } else {
                subtractOrderFromBook(buyOrderBooks.get(key), order.getOrderPrice(), order.getCoinAmount());
            }
        } else {
            if (isAdd) {
                addOrderToBook(sellOrderBooks.get(key), order.getOrderPrice(), order.getCoinAmount());
            } else {
                subtractOrderFromBook(sellOrderBooks.get(key), order.getOrderPrice(), order.getCoinAmount());
            }
        }
    }

    // 호가 리스트 조회 (예: 상위 10개)
    public Map<BigDecimal, BigDecimal> getTopNBuyOrders(String key, int n) {
        return buyOrderBooks.getOrDefault(key, new TreeMap<>()).entrySet().stream()
                .limit(n)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<BigDecimal, BigDecimal> getTopNSellOrders(String key, int n) {
        return sellOrderBooks.getOrDefault(key, new TreeMap<>()).entrySet().stream()
                .limit(n)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void printOrderBook(String key) {
        Map<BigDecimal, BigDecimal> topBuyOrders = getTopNBuyOrders(key, 10);
        Map<BigDecimal, BigDecimal> topSellOrders = getTopNSellOrders(key, 10);

        // Sell Orders: 작은 값이 아래로, 큰 값이 위로 (기본적으로 TreeMap은 오름차순이므로 그대로 사용)
        System.out.println("------- Sell Orders (Top 10) -------");
        System.out.printf("%-15s | %-15s%n", "Price", "Quantity");
        System.out.println("-----------------------------------");
        topSellOrders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 오름차순 정렬 (작은 값이 아래로)
                .forEach(entry -> System.out.printf("%-15s | %-15s%n", entry.getKey(), entry.getValue()));

        // Buy Orders: 큰 값이 위로, 작은 값이 아래로 (내림차순)
        System.out.println("\n------- Buy Orders (Top 10) -------");
        System.out.printf("%-15s | %-15s%n", "Price", "Quantity");
        System.out.println("-----------------------------------");
        topBuyOrders.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder())) // 내림차순 정렬 (큰 값이 위로)
                .forEach(entry -> System.out.printf("%-15s | %-15s%n", entry.getKey(), entry.getValue()));
    }
}

