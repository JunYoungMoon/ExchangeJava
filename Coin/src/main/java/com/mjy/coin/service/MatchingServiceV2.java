package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;

@Service
public class MatchingServiceV2 implements MatchingService {
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final OrderQueueService orderQueueService;
    private final RedisService redisService;
    private final KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    private final KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    public MatchingServiceV2(MasterCoinOrderRepository masterCoinOrderRepository, OrderQueueService orderQueueService,
                             OrderBookService orderBookService, RedisService redisService,
                             @Qualifier("matchListKafkaTemplate") KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate,
                             @Qualifier("priceVolumeMapKafkaTemplate") KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.orderQueueService = orderQueueService;
        this.redisService = redisService;
        this.matchListKafkaTemplate = matchListKafkaTemplate;
        this.priceVolumeMapKafkaTemplate = priceVolumeMapKafkaTemplate;
    }

    @Override
    public void matchOrders(String key, CoinOrderDTO order) {
        // 반대 주문 가져오기 : 매수 주문이면 매도 큐를, 매도 주문이면 매수 큐를 가져온다.
        PriorityQueue<CoinOrderDTO> oppositeOrdersQueue = getOppositeOrdersQueue(order, key);

        // 체결 처리 로직 시작
        while (!oppositeOrdersQueue.isEmpty()) {
            // 반대 주문의 최우선 데이터 가져오기
            CoinOrderDTO oppositeOrder = oppositeOrdersQueue.peek();

            // 현재 주문과 반대 주문의 가격 및 수량 정보
            if (!canMatchOrders(order, oppositeOrder)) break;

            //남은 수량
            BigDecimal remainingQuantity = calculateRemainingQuantity(order, oppositeOrder);

            //실제 체결 되는 가격은 반대 주문 가격 설정
            BigDecimal executionPrice = getExecutionPrice(oppositeOrder);

            if (isCompleteMatch(remainingQuantity)) {
                processCompleteMatch(order, oppositeOrder, key, oppositeOrdersQueue, executionPrice);
            } else if (isOversizeMatch(remainingQuantity)) {
                processOversizeMatch(order, oppositeOrder, key, oppositeOrdersQueue, remainingQuantity, executionPrice);
            } else if (isUndersizedMatch(remainingQuantity)) {
                processUndersizedMatch(order, oppositeOrder, key, oppositeOrdersQueue, remainingQuantity, executionPrice);
                break; // 나의 주문은 더 이상 처리할 수 없으므로 종료
            }
        }

        //남은 주문 미체결 처리
        processNonMatchedOrder(key, order);
    }

    public void addPendingOrder(String key, CoinOrderDTO order) {
        // Redis에 미체결 주문 저장
        redisService.insertOrderInRedis(key, PENDING, order);

        // 미체결 주문 Kafka 전송
        sendPendingOrderToKafka(order);

        // 주문 추가 및 주문 호가 갱신
        if (order.getOrderType() == OrderType.BUY) {
            orderQueueService.addBuyOrder(key, order);
            orderBookService.updateOrderBook(key, order, true, true);
        } else if (order.getOrderType() == OrderType.SELL) {
            orderQueueService.addSellOrder(key, order);
            orderBookService.updateOrderBook(key, order, false, true);
        }
    }

    public void processNonMatchedOrder(String key, CoinOrderDTO order){
        if (order.getOrderStatus() == PENDING && order.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
            addPendingOrder(key, order);
        }
    }

    public BigDecimal getExecutionPrice(CoinOrderDTO oppositeOrder) {
        return oppositeOrder.getOrderPrice();
    }

    public BigDecimal calculateRemainingQuantity(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        return order.getQuantity().subtract(oppositeOrder.getQuantity());
    }

    public boolean isCompleteMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isOversizeMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isUndersizedMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean canMatchOrders(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        BigDecimal currentOrderPrice = order.getOrderPrice();
        BigDecimal oppositeOrderPrice = oppositeOrder.getOrderPrice();

        // 매수 가격이 매도 가격보다 크거나 같으면 true, 매도 가격이 매수 가격보다 작거나 같으면 true
        boolean isPriceMatching =
                (order.getOrderType() == BUY && currentOrderPrice.compareTo(oppositeOrderPrice) >= 0) ||
                        (order.getOrderType() == SELL && currentOrderPrice.compareTo(oppositeOrderPrice) <= 0);

        // 주문 수량이 0보다 작거나 같고 반대 주문과 가격이 맞지 않을때 벗어난다.
        return isPriceMatching && order.getQuantity().compareTo(BigDecimal.ZERO) > 0;
    }

    public PriorityQueue<CoinOrderDTO> getOppositeOrdersQueue(CoinOrderDTO order, String key) {
        return (order.getOrderType() == BUY)
                ? orderQueueService.getSellOrderQueue(key)
                : orderQueueService.getBuyOrderQueue(key);
    }

    public void updateOrderWithMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, BigDecimal executionPrice) {
        order.setOrderStatus(COMPLETED);
        order.setMatchedAt(LocalDateTime.now());
        order.setExecutionPrice(executionPrice);
        order.setMatchIdx(oppositeOrder.getIdx());
    }

    private void completeOrders(String key, CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        redisService.insertOrderInRedis(key, COMPLETED, order);
        redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);
    }

    private void removeOppositePendingOrder(String key, String uuid, PriorityQueue<CoinOrderDTO> queue) {
        redisService.deleteHashOps(PENDING + ":ORDER:" + key, uuid);
        queue.poll();
    }

    public void processCompleteMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key,
                                     PriorityQueue<CoinOrderDTO> queue, BigDecimal executionPrice) {
        System.out.println("완전체결 : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 주문과 반대주문 모두 체결로 처리
        updateOrderWithMatch(order, oppositeOrder, executionPrice);
        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        // 두 주문 모두 체결 주문으로 변경
        completeOrders(key, order, oppositeOrder);

        // 반대 미체결 주문 제거
        removeOppositePendingOrder(key, oppositeOrder.getUuid(), queue);
    }

    private void processUndersizedMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key,
                                        PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity,
                                        BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 작다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 나의 주문 모두 체결 처리
        updateOrderWithMatch(order, oppositeOrder, executionPrice);

        // 반대 주문 부분 체결 처리
        String previousUUID = oppositeOrder.getUuid();
        oppositeOrder.setUuid(generateUniqueKey(oppositeOrder));
        oppositeOrder.setQuantity(order.getQuantity());

        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        // 두 주문 모두 체결 주문으로 변경
        completeOrders(key, order, oppositeOrder);

        // 반대 미체결 주문 제거
        removeOppositePendingOrder(key, previousUUID, queue);

        // 남은 수량을 잔여 수량으로 설정
        oppositeOrder.setOrderStatus(PENDING);
        oppositeOrder.setUuid(previousUUID);
        oppositeOrder.setQuantity(remainingQuantity);
        oppositeOrder.setExecutionPrice(null);
        oppositeOrder.setMatchIdx(order.getIdx());

        // 수정된 주문 다시 추가
        redisService.insertOrderInRedis(key, PENDING, oppositeOrder);
        queue.offer(oppositeOrder);

        //미체결 주문 kafka 전송
        sendPendingOrderToKafka(oppositeOrder);
    }

    private void processOversizeMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key,
                                      PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity,
                                      BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 크다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 반대 주문 모두 체결 처리
        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        // 나의 주문 부분 체결 처리
        String previousUUID = order.getUuid();
        order.setUuid(generateUniqueKey(order));
        order.setQuantity(oppositeOrder.getQuantity());

        updateOrderWithMatch(order, oppositeOrder, executionPrice);

        // 두 주문 모두 체결 주문으로 변경
        completeOrders(key, order, oppositeOrder);

        // 나의 주문 남은 수량을 잔여 수량으로 설정
        order.setOrderStatus(PENDING);
        order.setUuid(previousUUID);
        order.setQuantity(remainingQuantity);
        order.setExecutionPrice(null);

        // 반대 미체결 주문 제거
        removeOppositePendingOrder(key, oppositeOrder.getUuid(), queue);
    }

    //미체결 주문 kafka 전송
    public void sendPendingOrderToKafka(CoinOrderDTO orderDTO) {

        System.out.println("sendPendingOrderToKafka");
        //
    }
}
