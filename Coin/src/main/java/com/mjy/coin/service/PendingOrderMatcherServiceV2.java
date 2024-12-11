package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;

@Component
public class PendingOrderMatcherServiceV2 implements PendingOrderMatcherService {
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final OrderService orderService;
    private final RedisService redisService;
    private final KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    private final KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    public PendingOrderMatcherServiceV2(MasterCoinOrderRepository masterCoinOrderRepository,
                                        OrderService orderService,
                                        OrderBookService orderBookService,
                                        RedisService redisService,
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
        BigDecimal executionPrice;

        String key = order.getCoinName() + "-" + order.getMarketName();

        // 반대 주문 가져오기 : 매수 주문이면 매도 큐를, 매도 주문이면 매수 큐를 가져온다.
        PriorityQueue<CoinOrderDTO> oppositeOrdersQueue =
                (order.getOrderType() == BUY)
                        ? orderService.getSellOrderQueue(key)
                        : orderService.getBuyOrderQueue(key);

        // 체결 처리 로직 시작
        while (!oppositeOrdersQueue.isEmpty()) {
            // 반대 주문의 최우선 데이터 가져오기
            CoinOrderDTO oppositeOrder = oppositeOrdersQueue.peek();

            // 현재 주문과 반대 주문의 가격 및 수량 정보
            BigDecimal currentOrderPrice = order.getOrderPrice();
            BigDecimal oppositeOrderPrice = oppositeOrder.getOrderPrice();
            BigDecimal remainingQuantity = order.getCoinAmount().subtract(oppositeOrder.getCoinAmount());

            // 매수 가격이 매도 가격보다 크거나 같으면 true, 매도 가격이 매수 가격보다 작거나 같으면 true
            boolean isPriceMatching =
                    (order.getOrderType() == BUY && currentOrderPrice.compareTo(oppositeOrderPrice) >= 0) ||
                    (order.getOrderType() == SELL && currentOrderPrice.compareTo(oppositeOrderPrice) <= 0);

            // 주문 수량이 0보다 작거나 같고 반대 주문과 가격이 맞지 않을때 벗어난다.
            boolean isOrderInvalid = order.getCoinAmount().compareTo(BigDecimal.ZERO) <= 0 || !isPriceMatching;

            if (isOrderInvalid) {
                break; // 매칭되지 않으면 더 이상 체결할 수 없으므로 종료
            }

            //체결진행
            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                // 완전 체결
                // 매수와 매도 주문이 동일한 수량으로 체결된 경우
                System.out.println("완전체결 : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

                //실제 체결 되는 가격은 반대 주문 가격 설정
                executionPrice = oppositeOrder.getOrderPrice();

                // 주문과 반대주문 모두 체결로 처리
                // 주문건은 redis에 바로 넣으면 되고 반대 주문은 redis에서 미체결 제거후 체결 데이터로 전환
                order.setOrderStatus(COMPLETED);
                order.setMatchedAt(LocalDateTime.now());
                order.setExecutionPrice(executionPrice);
                order.setMatchIdx(order.getUuid() + "|" + oppositeOrder.getUuid());
                redisService.insertOrderInRedis(key, COMPLETED, order);

                oppositeOrder.setOrderStatus(COMPLETED);
                oppositeOrder.setMatchedAt(LocalDateTime.now());
                oppositeOrder.setExecutionPrice(executionPrice);
                oppositeOrder.setMatchIdx(oppositeOrder.getUuid() + "|" + order.getUuid());
                redisService.deleteHashOps(PENDING + ":ORDER:" + key, oppositeOrder.getUuid());
                redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);

                // 상대는 우선순위큐 poll
                oppositeOrdersQueue.poll();

                // 체결 되었으니 반대 주문 호가 리스트 제거
                orderBookService.updateOrderBook(key, oppositeOrder, oppositeOrder.getOrderType() == BUY, false);
            } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                // 부분체결
                // 나의 주문 수량이 반대 주문수량 보다 클경우
                System.out.println("부분체결 (주문이 반대 주문보다 크다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

                // 반대 주문 체결가로 지정
                executionPrice = oppositeOrder.getOrderPrice();

                // 반대 주문 모두 체결 처리
                oppositeOrder.setOrderStatus(COMPLETED);
                oppositeOrder.setMatchedAt(LocalDateTime.now());
                oppositeOrder.setMatchIdx(oppositeOrder.getUuid() + "|" + order.getUuid());
                oppositeOrder.setExecutionPrice(executionPrice);

                redisService.deleteHashOps(PENDING + ":ORDER:" + key, oppositeOrder.getUuid());
                redisService.insertOrderInRedis(key, COMPLETED, oppositeOrder);

                // 나의 주문 부분 체결 처리
                String previousUUID = order.getUuid();
                String uuid = order.getMemberIdx() + "_" + UUID.randomUUID();

                order.setUuid(uuid);
                order.setOrderStatus(COMPLETED);
                order.setMatchedAt(LocalDateTime.now());
                order.setExecutionPrice(executionPrice);
                order.setMatchIdx(order.getUuid() + "|" + oppositeOrder.getUuid());
                order.setCoinAmount(oppositeOrder.getCoinAmount());

                redisService.insertOrderInRedis(key, COMPLETED, order);

                // 남은 수량을 잔여 수량으로 설정
                order.setOrderStatus(PENDING);
                order.setUuid(previousUUID);
                order.setCoinAmount(remainingQuantity);
                order.setExecutionPrice(null);
                // 상대는 우선순위큐 poll
                oppositeOrdersQueue.poll();
            } else {
                // 부분 체결
                // 나의 주문수량이 반대 주문수량 보다 작을 경우
                System.out.println("부분체결 (주문이 반대 주문보다 작다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

                // 반대 주문을 체결가로 지정
                executionPrice = oppositeOrder.getOrderPrice();
                
                // 나의 주문 모두 체결 처리
                order.setOrderStatus(COMPLETED);
                order.setMatchedAt(LocalDateTime.now());
                order.setMatchIdx(order.getUuid() + "|" + oppositeOrder.getUuid());
                order.setExecutionPrice(executionPrice);

                redisService.insertOrderInRedis(key, COMPLETED, order);

                // 반대 주문 부분 체결 처리
                String previousUUID = oppositeOrder.getUuid();
                String uuid = oppositeOrder.getMemberIdx() + "_" + UUID.randomUUID();

                oppositeOrder.setUuid(uuid);
                oppositeOrder.setOrderStatus(COMPLETED);
                oppositeOrder.setMatchedAt(LocalDateTime.now());
                oppositeOrder.setExecutionPrice(executionPrice);
                oppositeOrder.setMatchIdx(oppositeOrder.getUuid() + "|" + order.getUuid());
                oppositeOrder.setCoinAmount(order.getCoinAmount());

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

                // 나의 주문은 더 이상 처리할 수 없으므로 종료
                break;
            }
        }

        // 남은 주문 정보 그대로 미체결 입력
        if(order.getOrderStatus() == PENDING && order.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
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

    //미체결 주문 kafka 전송
    private void sendPendingOrderToKafka(CoinOrderDTO orderDTO) {
        //
    }

}
