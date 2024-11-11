package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.mjy.coin.enums.OrderStatus.PENDING;

@Component
public class PendingOrderProcessorService {

    private final PendingOrderMatcherService priorityQueueManager;
    private final OrderBookService orderBookService;
    private final OrderService orderService;
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final RedisService redisService;

    @Autowired
    public PendingOrderProcessorService(PendingOrderMatcherService priorityQueueManager,
                                        MasterCoinOrderRepository masterCoinOrderRepository,
                                        SlaveCoinOrderRepository slaveCoinOrderRepository,
                                        OrderService orderService,
                                        OrderBookService orderBookService,
                                        RedisService redisService) {
        this.priorityQueueManager = priorityQueueManager;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.orderService = orderService;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.redisService = redisService;
    }

    public synchronized void processOrder(CoinOrderDTO order) {
        String key = order.getCoinName() + "-" + order.getMarketName();

        // Order ID 생성: UUID를 사용하여 고유한 주문 ID 생성
        String uuid = order.getMemberIdx() + "_" + UUID.randomUUID();

        order.setUuid(uuid);

        try {
            // Redis에서 해당 orderId가 존재하는지 확인
            String existingOrder = redisService.getHashOps("PENDING:ORDER:" + key, uuid);

            // 주문이 존재하지 않을 경우에만 저장
            if (existingOrder.isEmpty()) {
                redisService.insertOrderInRedis(key, PENDING, order);

                if (order.getOrderType() == OrderType.BUY) {
                    System.out.println("Adding buy order to queue: " + order);
                    orderService.addBuyOrder(key, order);
                    orderBookService.updateOrderBook(key, order, true, true);
                } else if (order.getOrderType() == OrderType.SELL) {
                    System.out.println("Adding sell order to queue: " + order);
                    orderService.addSellOrder(key, order);
                    orderBookService.updateOrderBook(key, order, false, true);
                }

                // 주문 체결 시도
                priorityQueueManager.matchOrders(key);

            }
//
//            CoinOrder orderEntity = CoinOrderMapper.toEntity(order);
//
//            // DB에 이미 존재하는 주문인지 확인
//            Optional<CoinOrder> existingOrder = slaveCoinOrderRepository.findByMarketNameAndCoinNameAndCreatedAt(
//                    orderEntity.getMarketName(),
//                    orderEntity.getCoinName(),
//                    orderEntity.getCreatedAt()
//            );
//
//            if (existingOrder.isPresent()) {
//                System.out.println("Order already exists: " + existingOrder.get());
//                return; // 이미 존재하는 경우, 메서드 종료
//            }
//
//            // DB에 저장 (저장된 엔티티 반환)
//            CoinOrder savedOrderEntity = masterCoinOrderRepository.save(orderEntity);
//
//            // 저장된 엔티티에서 idx 가져와서 DTO에 설정
//            order.setIdx(savedOrderEntity.getIdx());
//
//            // 로그: 주문이 DB에 저장된 후
//            System.out.println("Order saved: " + savedOrderEntity);
//
////             저장이 성공했으므로 매수/매도 큐에 추가
////             호가 리스트도 추가
//            if (order.getOrderType() == OrderType.BUY) {
//                System.out.println("Adding buy order to queue: " + order);
//                priorityQueueManager.addBuyOrder(key, order);
//                orderBookService.updateOrderBook(key, order, true, true);
//            } else if (order.getOrderType() == OrderType.SELL) {
//                System.out.println("Adding sell order to queue: " + order);
//                priorityQueueManager.addSellOrder(key, order);
//                orderBookService.updateOrderBook(key, order, false, true);
//            }
//
//            // 주문 체결 시도
//            priorityQueueManager.matchOrders(key);

            //호가 리스트 출력
//            orderBookService.printOrderBook(key);
        } catch (Exception e) {
            // 예외 처리: 로그를 기록하거나 필요한 조치를 수행
            System.err.println("Failed to save order: " + e.getMessage());
        }
    }
}
