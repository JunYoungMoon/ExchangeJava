package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;

@Component
public class PendingOrderMatcherServiceV2 implements PendingOrderMatcherService {
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final OrderService orderService;
    private final RedisService redisService;
    private final KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    private final KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    public PendingOrderMatcherServiceV2(MasterCoinOrderRepository masterCoinOrderRepository, OrderService orderService,
                                        OrderBookService orderBookService, RedisService redisService,
                                        @Qualifier("matchListKafkaTemplate") KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate,
                                        @Qualifier("priceVolumeMapKafkaTemplate") KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.orderService = orderService;
        this.redisService = redisService;
        this.matchListKafkaTemplate = matchListKafkaTemplate;
        this.priceVolumeMapKafkaTemplate = priceVolumeMapKafkaTemplate;
    }

    @Override
    public void matchOrders(CoinOrderDTO order) {
        String key = order.getCoinName() + "-" + order.getMarketName();

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

            if (isCompleteMatch(remainingQuantity)) {
                processCompleteMatch(order, oppositeOrder, key, oppositeOrdersQueue);
            } else if (isOversizeMatch(remainingQuantity)) {
                processOversizeMatch(order, oppositeOrder, key, oppositeOrdersQueue, remainingQuantity);
            } else if (isUndersizedMatch(remainingQuantity)) {
                processUndersizedMatch(order, oppositeOrder, key, oppositeOrdersQueue, remainingQuantity);
                break; // 나의 주문은 더 이상 처리할 수 없으므로 종료
            }
        }

        // 남은 주문 정보 그대로 미체결 입력
        if (order.getOrderStatus() == PENDING && order.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
            redisService.insertOrderInRedis(key, PENDING, order);

            //미체결 주문 kafka 전송
            sendPendingOrderToKafka(order);

            if (order.getOrderType() == OrderType.BUY) {
                System.out.println("Adding buy order to queue: " + order);
                orderService.addBuyOrder(key, order);
                orderBookService.updateOrderBook(key, order, true, true);
            } else if (order.getOrderType() == OrderType.SELL) {
                System.out.println("Adding sell order to queue: " + order);
                orderService.addSellOrder(key, order);
                orderBookService.updateOrderBook(key, order, false, true);
            }
        }
    }

    public BigDecimal calculateRemainingQuantity(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        return order.getCoinAmount().subtract(oppositeOrder.getCoinAmount());
    }

    private boolean isCompleteMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isOversizeMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isUndersizedMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) < 0;
    }

    private boolean canMatchOrders(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        BigDecimal currentOrderPrice = order.getOrderPrice();
        BigDecimal oppositeOrderPrice = oppositeOrder.getOrderPrice();

        // 매수 가격이 매도 가격보다 크거나 같으면 true, 매도 가격이 매수 가격보다 작거나 같으면 true
        boolean isPriceMatching =
                (order.getOrderType() == BUY && currentOrderPrice.compareTo(oppositeOrderPrice) >= 0) ||
                        (order.getOrderType() == SELL && currentOrderPrice.compareTo(oppositeOrderPrice) <= 0);

        // 주문 수량이 0보다 작거나 같고 반대 주문과 가격이 맞지 않을때 벗어난다.
        return isPriceMatching && order.getCoinAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private PriorityQueue<CoinOrderDTO> getOppositeOrdersQueue(CoinOrderDTO order, String key) {
        return (order.getOrderType() == BUY)
                ? orderService.getSellOrderQueue(key)
                : orderService.getBuyOrderQueue(key);
    }

    private void updateOrderWithMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, BigDecimal executionPrice) {
        order.setOrderStatus(COMPLETED);
        order.setMatchedAt(LocalDateTime.now());
        order.setExecutionPrice(executionPrice);
        order.setMatchIdx(order.getUuid() + "|" + oppositeOrder.getUuid());
    }

    private void processCompleteMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key, PriorityQueue<CoinOrderDTO> queue) {
        System.out.println("완전체결 : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        //실제 체결 되는 가격은 반대 주문 가격 설정
        BigDecimal executionPrice = oppositeOrder.getOrderPrice();

        // 주문과 반대주문 모두 체결로 처리
        // 주문건은 redis에 바로 넣으면 되고 반대 주문은 redis에서 미체결 제거후 체결 데이터로 전환
        updateOrderWithMatch(order, oppositeOrder, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, order);

        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);

        redisService.deleteHashOps(PENDING + ":ORDER:" + key, oppositeOrder.getUuid());

        // 상대는 우선순위큐 poll
        queue.poll();

        // 체결 되었으니 반대 주문 호가 리스트 제거
        orderBookService.updateOrderBook(key, oppositeOrder, oppositeOrder.getOrderType() == BUY, false);
    }

    private void processUndersizedMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key, PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity) {
        System.out.println("부분체결 (주문이 반대 주문보다 작다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        //실제 체결 되는 가격은 반대 주문 가격 설정
        BigDecimal executionPrice = oppositeOrder.getOrderPrice();

        // 나의 주문 모두 체결 처리
        updateOrderWithMatch(order, oppositeOrder, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, order);

        // 반대 주문 부분 체결 처리
        String previousUUID = oppositeOrder.getUuid();

        oppositeOrder.setUuid(generateUniqueKey("Order"));

        oppositeOrder.setCoinAmount(order.getCoinAmount());

        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);

        // 남은 수량을 잔여 수량으로 설정
        oppositeOrder.setOrderStatus(PENDING);
        oppositeOrder.setUuid(previousUUID);
        oppositeOrder.setCoinAmount(remainingQuantity);
        oppositeOrder.setExecutionPrice(null);
        oppositeOrder.setMatchIdx("");

        redisService.deleteHashOps(PENDING + ":ORDER:" + key, previousUUID);
        redisService.insertOrderInRedis(key, PENDING, oppositeOrder);

        //미체결 주문 kafka 전송
        sendPendingOrderToKafka(oppositeOrder);
    }

    private void processOversizeMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key, PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity) {
        System.out.println("부분체결 (주문이 반대 주문보다 크다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        //실제 체결 되는 가격은 반대 주문 가격 설정
        BigDecimal executionPrice = oppositeOrder.getOrderPrice();

        // 반대 주문 모두 체결 처리
        updateOrderWithMatch(oppositeOrder, order, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);
        redisService.deleteHashOps(PENDING + ":ORDER:" + key, oppositeOrder.getUuid());

        // 나의 주문 부분 체결 처리
        String previousUUID = order.getUuid();

        order.setUuid(generateUniqueKey("Order"));

        order.setCoinAmount(oppositeOrder.getCoinAmount());

        updateOrderWithMatch(order, oppositeOrder, executionPrice);

        redisService.insertOrderInRedis(key, COMPLETED, order);

        // 남은 수량을 잔여 수량으로 설정
        order.setOrderStatus(PENDING);
        order.setUuid(previousUUID);
        order.setCoinAmount(remainingQuantity);
        order.setExecutionPrice(null);
        // 상대는 우선순위큐 poll
        queue.poll();
    }

    //미체결 주문 kafka 전송
    private void sendPendingOrderToKafka(CoinOrderDTO orderDTO) {
        //
    }
}
