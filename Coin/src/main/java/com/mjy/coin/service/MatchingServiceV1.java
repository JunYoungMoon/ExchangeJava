package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;

@Service
public class MatchingServiceV1 implements MatchingService {
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final OrderQueueService orderQueueService;
    private final RedisService redisService;
    private final KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    private final KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    public MatchingServiceV1(MasterCoinOrderRepository masterCoinOrderRepository,
                             OrderQueueService orderQueueService,
                             OrderBookService orderBookService,
                             RedisService redisService,
                             @Qualifier("matchListKafkaTemplate") KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate,
                             @Qualifier("priceVolumeMapKafkaTemplate") KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.orderQueueService = orderQueueService;
        this.redisService = redisService;
        this.matchListKafkaTemplate = matchListKafkaTemplate;
        this.priceVolumeMapKafkaTemplate = priceVolumeMapKafkaTemplate;
    }

    // 체결 로직
    @Override
//    @Transactional
    public void matchOrders(String symbol, CoinOrderDTO order) {
        // 반대 주문 가져오기 : 매수 주문이면 매도 큐를, 매도 주문이면 매수 큐를 가져온다.
        PriorityQueue<CoinOrderDTO> oppositeOrdersQueue = getOppositeOrdersQueue(order, symbol);

        while (!oppositeOrdersQueue.isEmpty()) {
            // 반대 주문의 최우선 데이터 가져오기
            CoinOrderDTO oppositeOrder = oppositeOrdersQueue.poll();

            //체결 조건 확인
            if (!canMatchOrders(order, oppositeOrder)) {
                oppositeOrdersQueue.add(oppositeOrder);
                break;
            }

            //남은 수량
            BigDecimal remainingQuantity = calculateRemainingQuantity(order, oppositeOrder);

            //실제 체결 되는 가격은 반대 주문 가격 설정
            BigDecimal executionPrice = getExecutionPrice(oppositeOrder);

            if (isCompleteMatch(remainingQuantity)) {
                processCompleteMatch(order, oppositeOrder, executionPrice);
            } else if (isOversizeMatch(remainingQuantity)) {
                processOversizeMatch(order, oppositeOrder, remainingQuantity, executionPrice);
            } else if (isUndersizedMatch(remainingQuantity)) {
                processUndersizedMatch(order, oppositeOrder, symbol, oppositeOrdersQueue, remainingQuantity, executionPrice);
                break; // 나의 주문은 더 이상 처리할 수 없으므로 종료
            }
        }

        //남은 주문 미체결 처리
        processNonMatchedOrder(symbol, order);


//        //반복하는 동안 쌓인 가격과 볼륨 리스트 kafka로 전달(실시간 차트에서 사용)
//        if (!priceVolumeList.isEmpty()) {
//            Map<String, List<PriceVolumeDTO>> priceVolumeMap = new HashMap<>();
//            priceVolumeMap.put(symbol, priceVolumeList);
//            priceVolumeMapKafkaTemplate.send("Price-Volume", priceVolumeMap);
//        }
//
//        //반복하는 동안 쌓인 완료 주문 리스트 kafka로 전달(웹소켓을 통해 완료 리스트를 사용자에게 전달하기 위함)
//        if (!matchList.isEmpty()) {
//            Map<String, List<CoinOrderDTO>> matchListeMap = new HashMap<>();
//            matchListeMap.put(symbol, matchList);
//            matchListKafkaTemplate.send("Match-List", matchListeMap);
//        }
    }

    // 반대 주문 큐 가져오기
    public PriorityQueue<CoinOrderDTO> getOppositeOrdersQueue(CoinOrderDTO order, String symbol) {
        return (order.getOrderType() == BUY)
                ? orderQueueService.getSellOrderQueue(symbol)
                : orderQueueService.getBuyOrderQueue(symbol);
    }

    // 체결 조건 확인
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

    // 나머지 수량 계산
    public BigDecimal calculateRemainingQuantity(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        return order.getQuantity().subtract(oppositeOrder.getQuantity());
    }

    // 체결가 반환
    public BigDecimal getExecutionPrice(CoinOrderDTO oppositeOrder) {
        return oppositeOrder.getOrderPrice();
    }

    //미체결 주문 kafka 전송
    public void sendPendingOrderToKafka(CoinOrderDTO orderDTO) {
        System.out.println("sendPendingOrderToKafka");
    }

    // 주문 수량이 완전 일치하는가?
    public boolean isCompleteMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) == 0;
    }

    // 주문 수량이 더 많은가?
    public boolean isOversizeMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    // 주문 수량이 더 적은가?
    public boolean isUndersizedMatch(BigDecimal remainingQuantity) {
        return remainingQuantity.compareTo(BigDecimal.ZERO) < 0;
    }

    // 미체결 주문인지 체크
    public void processNonMatchedOrder(String symbol, CoinOrderDTO order) {
        if (order.getOrderStatus() == PENDING && order.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
            addPendingOrder(symbol, order);
        }
    }

    // 체결 상태 변경
    public void updateOrderStatus(CoinOrderDTO order, CoinOrderDTO oppositeOrder,
                                  BigDecimal executionPrice, OrderStatus orderStatus,
                                  Long idx, BigDecimal quantity) {
        order.setIdx(idx);
        order.setOrderStatus(orderStatus);
        order.setExecutionPrice(executionPrice);
        order.setQuantity(quantity);
        order.setCreatedAt(LocalDateTime.now());

        if (orderStatus == COMPLETED) {
            order.setMatchedAt(LocalDateTime.now());
            order.setMatchIdx(oppositeOrder.getIdx());
        } else {
            order.setMatchIdx(null);
            order.setMatchedAt(null);
        }
    }

    // 미체결 : 매칭 가격아님
    @Transactional
    public void addPendingOrder(String symbol, CoinOrderDTO order) {
        System.out.println("미체결 주문 : " + order);

        CoinOrder orderEntity = CoinOrderMapper.toEntity(order);
        CoinOrder coinOrder = masterCoinOrderRepository.save(orderEntity);
        order = CoinOrderMapper.fromEntity(coinOrder);

        //addBuyOrder가 성공하고 updateOrderBook가 실패하게 되면 어떻게 rollback을 할까?
        //updateOrderBook은 중요도가 낮다. 따로 뺄지 고민중.
        if (order.getOrderType() == OrderType.BUY) {
            orderQueueService.addBuyOrder(symbol, order);
            orderBookService.updateOrderBook(symbol, order, true, true);
        } else if (order.getOrderType() == OrderType.SELL) {
            orderQueueService.addSellOrder(symbol, order);
            orderBookService.updateOrderBook(symbol, order, false, true);
        }

        sendPendingOrderToKafka(order);
    }

    // 완전 체결 : 매칭 가격이고 나의 주문 수량이 반대 주문 수량과 완전 일치
    @Transactional
    public void processCompleteMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, BigDecimal executionPrice) {
        System.out.println("완전체결 : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 주문과 반대주문 모두 체결 상태 변경
        updateOrderStatus(order, oppositeOrder, executionPrice, COMPLETED, order.getIdx(), order.getQuantity());
        updateOrderStatus(oppositeOrder, order, executionPrice, COMPLETED, oppositeOrder.getIdx(), oppositeOrder.getQuantity());

        // 주문과 반대주문 모두 DB 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(oppositeOrder));
    }

    // 부분 체결1 : 매칭 가격이고 나의 주문 수량이 반대 주문 수량보다 클때
    @Transactional
    public void processOversizeMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, BigDecimal remainingQuantity, BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 크다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 반대 주문 모두 체결 처리
        updateOrderStatus(oppositeOrder, order, executionPrice, COMPLETED, oppositeOrder.getIdx(), oppositeOrder.getQuantity());
        // 나의 주문 반대 주문 만큼 부분 체결 처리
        updateOrderStatus(order, oppositeOrder, executionPrice, COMPLETED, null, oppositeOrder.getQuantity());

        // 주문과 반대주문 모두 DB 저장
        //상대 주문 모두 업데이트 필요
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(oppositeOrder));

        // 나의 주문 남은 수량을 잔여 수량으로 설정
        updateOrderStatus(order, oppositeOrder, executionPrice, PENDING, null, remainingQuantity);
    }

    // 부분 체결2 : 매칭 가격이고 나의 주문 수량이 반대 주문 수량보다 작을때
    private void processUndersizedMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String symbol,
                                        PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity,
                                        BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 작다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

    }
}
