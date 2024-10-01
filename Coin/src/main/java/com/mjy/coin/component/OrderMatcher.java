package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class OrderMatcher {

    private final MasterCoinOrderRepository masterCoinOrderRepository;

    @Autowired
    public OrderMatcher(MasterCoinOrderRepository masterCoinOrderRepository) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
    }

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

                    if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        // 매수/매도 양쪽 주문 체결 상태 업데이트
                        buyOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);

                        // 체결된 상대방의 회원 정보 저장
                        buyOrder.setMatchedMemberId(sellOrder.getMemberId());
                        sellOrder.setMatchedMemberId(buyOrder.getMemberId());

                        // DB 업데이트 (매수/매도 양쪽)
                        updateOrderStatus(buyOrder);
                        updateOrderStatus(sellOrder);

                        // 두 주문 모두 체결된 경우
                        System.out.println("Matched completely: " + buyOrder + " with " + sellOrder);
                        buyOrders.poll(); // 매수 주문 제거
                        sellOrders.poll(); // 매도 주문 제거
                    } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        // 매수 부분체결
                        // 부분체결 insert
                        // 나머지 가격 update
                        // 매수 주문이 더 많은 경우
                        System.out.println("Partially matched: " + buyOrder + " with " + sellOrder);
                        buyOrder.setCoinAmount(remainingQuantity); // 매수 주문 수정
                        sellOrders.poll(); // 매도 주문 제거
                    } else {
                        // 매도 부분체결
                        // 부분체결 insert
                        // 나머지 가격 update
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

    // 주문 상태와 체결 상대 정보를 업데이트하는 메소드
    private void updateOrderStatus(CoinOrderDTO order) {
        CoinOrder orderEntity = CoinOrderMapper.toEntity(order);
        masterCoinOrderRepository.save(orderEntity);
    }
}
