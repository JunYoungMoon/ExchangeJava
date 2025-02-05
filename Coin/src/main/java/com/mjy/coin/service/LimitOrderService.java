package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mjy.coin.util.CommonUtil.generateUniqueKey;

@Slf4j
@Service
public class LimitOrderService implements OrderService {

    private final PendingOrderMatcherService pendingOrderMatcherService;
    private final OrderBookService orderBookService;
    private final OrderQueueService orderQueueService;
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final RedisService redisService;

    @Autowired
    public LimitOrderService(@Qualifier("pendingOrderMatcherServiceV1") PendingOrderMatcherService pendingOrderMatcherService,
                             MasterCoinOrderRepository masterCoinOrderRepository,
                             SlaveCoinOrderRepository slaveCoinOrderRepository,
                             OrderQueueService orderQueueService,
                             OrderBookService orderBookService,
                             RedisService redisService) {
        this.pendingOrderMatcherService = pendingOrderMatcherService;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.orderQueueService = orderQueueService;
        this.redisService = redisService;
    }

    @Override
    public synchronized void processOrder(CoinOrderDTO order) {
        // 지정가 주문 처리 로직

        String key = order.getCoinName() + "-" + order.getMarketName();

        order.setUuid(generateUniqueKey(order));

        try {
//            // Redis에서 해당 order UUID가 존재하는지 확인
//            String existingOrder = redisService.getHashOps("PENDING:ORDER:" + key, order.getUuid());
//
//            // 주문이 존재하지 않을 경우에만 저장
//            if (existingOrder.isEmpty()) {
////                redisService.insertOrderInRedis(key, PENDING, order);
////
////                if (order.getOrderType() == OrderType.BUY) {
////                    System.out.println("Adding buy order to queue: " + order);
////                    orderService.addBuyOrder(key, order);
////                    orderBookService.updateOrderBook(key, order, true, true);
////                } else if (order.getOrderType() == OrderType.SELL) {
////                    System.out.println("Adding sell order to queue: " + order);
////                    orderService.addSellOrder(key, order);
////                    orderBookService.updateOrderBook(key, order, false, true);
////                }
//
//                // 주문 체결 시도
//                pendingOrderMatcherService.matchOrders(order);
//            }
//
            CoinOrder orderEntity = CoinOrderMapper.toEntity(order);

//            // DB에 이미 존재하는 주문인지 확인
//            Optional<Integer> existingOrder = slaveCoinOrderRepository.findByMarketNameAndCoinNameAndCreatedAt(
//                    orderEntity.getMarketName(),
//                    orderEntity.getCoinName(),
//                    orderEntity.getCreatedAt()
//            );
//
//            if (existingOrder.isPresent()) {
//                System.out.println("Order already exists: " + existingOrder.get());
//                return; // 이미 존재하는 경우, 메서드 종료
//            }

            // DB에 저장 (저장된 엔티티 반환)
            CoinOrder savedOrderEntity = slaveCoinOrderRepository.save(orderEntity);

            // 저장된 엔티티에서 idx 가져와서 DTO에 설정
            order.setIdx(savedOrderEntity.getIdx());

            // 로그: 주문이 DB에 저장된 후
            System.out.println("Order saved: " + savedOrderEntity);

//             저장이 성공했으므로 매수/매도 큐에 추가
//             호가 리스트도 추가
            if (order.getOrderType() == OrderType.BUY) {
                System.out.println("Adding buy order to queue: " + order);
                orderQueueService.addBuyOrder(key, order);
                orderBookService.updateOrderBook(key, order, true, true);
            } else if (order.getOrderType() == OrderType.SELL) {
                System.out.println("Adding sell order to queue: " + order);
                orderQueueService.addSellOrder(key, order);
                orderBookService.updateOrderBook(key, order, false, true);
            }

            // 주문 체결 시도
            pendingOrderMatcherService.matchOrders(order);

            //호가 리스트 출력
//            orderBookService.printOrderBook(key);
        } catch (Exception e) {
            // 예외 처리: 로그를 기록하거나 필요한 조치를 수행
            System.err.println("Failed to save order: " + e.getMessage());
        }
    }
}
