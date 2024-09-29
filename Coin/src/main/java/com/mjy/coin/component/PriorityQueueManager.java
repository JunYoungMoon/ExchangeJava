package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class PriorityQueueManager {

    private Map<String, PriorityQueue<CoinOrderDTO>> buyOrderQueues = new HashMap<>();
    private Map<String, PriorityQueue<CoinOrderDTO>> sellOrderQueues = new HashMap<>();

    // 큐 초기화
    public void initializeQueues(Map<String, PriorityQueue<CoinOrderDTO>> buyQueues, Map<String, PriorityQueue<CoinOrderDTO>> sellQueues) {
        this.buyOrderQueues = buyQueues;
        this.sellOrderQueues = sellQueues;
    }

    // 매수 주문 추가
    public void addBuyOrder(String key, CoinOrderDTO order) {
        buyOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
        buyOrderQueues.get(key).add(order);
    }

    // 매도 주문 추가
    public void addSellOrder(String key, CoinOrderDTO order) {
        sellOrderQueues.putIfAbsent(key, new PriorityQueue<>(
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt)
        ));
        sellOrderQueues.get(key).add(order);
    }

    // 매수 주문 조회
    public PriorityQueue<CoinOrderDTO> getBuyOrders(String key) {
        return buyOrderQueues.get(key);
    }

    // 매도 주문 조회
    public PriorityQueue<CoinOrderDTO> getSellOrders(String key) {
        return sellOrderQueues.get(key);
    }

    // 체결 로직
    public void matchOrders(String key) {
        PriorityQueue<CoinOrderDTO> buyOrders = buyOrderQueues.get(key);
        PriorityQueue<CoinOrderDTO> sellOrders = sellOrderQueues.get(key);

        if (buyOrders != null && sellOrders != null) {
            while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
                CoinOrderDTO buyOrder = buyOrders.peek();
                CoinOrderDTO sellOrder = sellOrders.peek();

                // 체결 가능 조건 확인
                if (buyOrder.getOrderPrice().compareTo(sellOrder.getOrderPrice()) >= 0) {
                    // 체결 가능 시 처리
                    BigDecimal buyQuantity = buyOrder.getCoinAmount();
                    BigDecimal sellQuantity = sellOrder.getCoinAmount();
                    BigDecimal remainingQuantity = buyQuantity.subtract(sellQuantity).setScale(8, RoundingMode.DOWN);

                    // 완전체결
                    if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        // 두 주문 모두 체결된 경우
                        System.out.println("Matched completely: " + buyOrder + " with " + sellOrder);
                        buyOrders.poll(); // 매수 주문 제거
                        sellOrders.poll(); // 매도 주문 제거
                    } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        // 매수 주문이 더 많은 경우
                        System.out.println("Partially matched: " + buyOrder + " with " + sellOrder);
                        buyOrder.setCoinAmount(remainingQuantity); // 매수 주문 수정
                        sellOrders.poll(); // 매도 주문 제거
                    } else {
                        // 매도 주문이 더 많은 경우
                        System.out.println("Partially matched: " + buyOrder + " with " + sellOrder);
                        sellOrder.setCoinAmount(remainingQuantity.negate()); // 매도 주문 수정
                        buyOrders.poll(); // 매수 주문 제거
                    }
                } else {
                    break; // 더 이상 체결할 수 없으면 중단
                }
            }
        }
    }
}
