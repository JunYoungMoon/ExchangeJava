package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
public class OrderBookService {
    private final Map<String, TreeMap<BigDecimal, BigDecimal>> buyOrderBooks = new HashMap<>();  // 매수: 높은 가격 우선
    private final Map<String, TreeMap<BigDecimal, BigDecimal>> sellOrderBooks = new HashMap<>(); // 매도: 낮은 가격 우선

    // 초기 매수 주문 호가 트리 생성 메서드
    public void initializeBuyOrderBook(String key) {
        buyOrderBooks.putIfAbsent(key, new TreeMap<>(Comparator.reverseOrder()));
    }

    // 초기 매도 주문 호가 트리 생성 메서드
    public void initializeSellOrderBook(String key) {
        sellOrderBooks.putIfAbsent(key, new TreeMap<>());
    }

    // 초기 매수 주문 호가 추가
    public void addBuyOrderBook(String key, CoinOrderDTO order){
        BigDecimal price = processBigDecimal(order.getOrderPrice());
        BigDecimal amount = processBigDecimal(order.getCoinAmount());
        addOrderToBook(buyOrderBooks.get(key), price, amount);
    }

    // 초기 매도 주문 호가 추가
    public void addSellOrderBook(String key, CoinOrderDTO order){
        BigDecimal price = processBigDecimal(order.getOrderPrice());
        BigDecimal amount = processBigDecimal(order.getCoinAmount());
        addOrderToBook(sellOrderBooks.get(key), price, amount);
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

    // BigDecimal 처리 메서드
    private BigDecimal processBigDecimal(BigDecimal value) {
        if (value.scale() > 0) {
            // 소수점 아래가 있는 경우
            return new BigDecimal(value.stripTrailingZeros().toPlainString());
        } else {
            // 소수점 아래가 없는 경우 정수로 처리
            return new BigDecimal(value.toBigInteger().toString());
        }
    }

    public Map<String, Map<BigDecimal, BigDecimal>> getTopNOrders(String key, int n) {
        Map<String, Map<BigDecimal, BigDecimal>> topOrders = new HashMap<>();

        // 매수 주문
        Map<BigDecimal, BigDecimal> topBuyOrders = getTopNBuyOrders(key, n);
        // 매도 주문
        Map<BigDecimal, BigDecimal> topSellOrders = getTopNSellOrders(key, n);

        topOrders.put("buy", topBuyOrders);  // 매수 주문을 "buy" 키로 저장
        topOrders.put("sell", topSellOrders);  // 매도 주문을 "sell" 키로 저장

        return topOrders;  // 매수/매도 주문을 담은 Map 반환
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

