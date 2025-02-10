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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;

@Service
public class PendingOrderMatcherServiceV1 implements PendingOrderMatcherService {
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final OrderQueueService orderQueueService;
    private final RedisService redisService;
    private final KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    private final KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    public PendingOrderMatcherServiceV1(MasterCoinOrderRepository masterCoinOrderRepository,
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
                processOversizeMatch(order, oppositeOrder, symbol, oppositeOrdersQueue, remainingQuantity, executionPrice);
            } else if (isUndersizedMatch(remainingQuantity)) {
                processUndersizedMatch(order, oppositeOrder, symbol, oppositeOrdersQueue, remainingQuantity, executionPrice);
                break; // 나의 주문은 더 이상 처리할 수 없으므로 종료
            }
        }

        //남은 주문 미체결 처리
        processNonMatchedOrder(symbol, order);


//        BigDecimal executionPrice;
//
//        PriorityQueue<CoinOrderDTO> buyOrders = orderQueueService.getBuyOrderQueue(symbol);
//        PriorityQueue<CoinOrderDTO> sellOrders = orderQueueService.getSellOrderQueue(symbol);
//
//        //kafka로 전달하기 위한 자료구조
////        List<CoinOrderDTO> matchList = new ArrayList<>();
////        List<PriceVolumeDTO> priceVolumeList = new ArrayList<>();
//
//        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
//            CoinOrderDTO buyOrder = buyOrders.peek();
//            CoinOrderDTO sellOrder = sellOrders.peek();
//
//            // 체결 가능 조건 확인
//            // 체결 조건에서 최종적으로 결정되는 기준은 매수자의 가격
//            if (buyOrder.getOrderPrice().compareTo(sellOrder.getOrderPrice()) < 0) {
//
//                break;
//            }
//
//            // 체결 가능 시 처리
//            BigDecimal buyQuantity = buyOrder.getQuantity();
//            BigDecimal sellQuantity = sellOrder.getQuantity();
//            //buyQuantity - sellQuantity 계산값을 소수점 8자리까지 표현한 결과가 저장됩니다.
//            BigDecimal remainingQuantity = buyQuantity.subtract(sellQuantity).setScale(8, RoundingMode.DOWN).stripTrailingZeros();
//
//            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
//                // 완전체결
//                // 매수와 매도 모두 체결
//                System.out.println("Matched completely: BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);
//
//                executionPrice = buyOrder.getOrderPrice(); //실제 체결 되는 가격은 매수자의 가격으로 체결
//
//                // 주문 삽입 (완전체결인 경우)
//                // 매수와 매도 모두 체결로 처리
//                buyOrder.setOrderStatus(COMPLETED);
//                buyOrder.setMatchedAt(LocalDateTime.now());
//                buyOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
//                buyOrder.setExecutionPrice(executionPrice);
//                sellOrder.setOrderStatus(COMPLETED);
//                sellOrder.setMatchedAt(LocalDateTime.now());
//                sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
//                sellOrder.setExecutionPrice(executionPrice);
//
//                // 매수와 매도 체결된 상태를 DB에 기록
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));
//
//                //////////////////////////////////시작////////////////////////////////////
////                        // 1. BuyOrder 업데이트
////                        buyOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, buyOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, buyOrder);
////
////                        // 2. SellOrder 업데이트
////                        sellOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, sellOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, sellOrder);
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 큐에서 양쪽 주문 제거
//                buyOrders.poll();
//                sellOrders.poll();
//
//                // 체결 되었으니 호가 리스트 제거
//                orderBookService.updateOrderBook(symbol, buyOrder, true, false);
//                orderBookService.updateOrderBook(symbol, sellOrder, false, false);
//
//                //체결 완료 된 데이터를 쌓아서 kafka로 전달할 list
////                    priceVolumeList.add(new PriceVolumeDTO(buyOrder));
////                    matchList.add(new CoinOrderDTO(buyOrder));
////                    matchList.add(new CoinOrderDTO(sellOrder));
//            } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
//                // 매수량이 매도량을 초과
//                // 매수는 일부 남고 매도는 모두 체결
//                System.out.println("Partial match (remaining buy): BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);
//
//                // 주문 생성 시간 비교하여 오래된 주문의 가격을 체결가로 설정
//                if (buyOrder.getCreatedAt().isAfter(sellOrder.getCreatedAt())) {
//                    executionPrice = sellOrder.getOrderPrice(); // 매도 주문이 먼저 생성된 경우
//                } else {
//                    executionPrice = buyOrder.getOrderPrice(); // 매수 주문이 먼저 생성된 경우
//                }
//
//                // 매도 모두 체결 처리
//                sellOrder.setOrderStatus(COMPLETED);
//                sellOrder.setMatchedAt(LocalDateTime.now());
//                sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
//                sellOrder.setExecutionPrice(executionPrice);   //실제 체결 되는 가격은 매수자의 가격으로 체결
//
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));
//
//                //////////////////////////////////시작////////////////////////////////////
////                        // 1. SellOrder 업데이트
////                        sellOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
////
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, sellOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, sellOrder);
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 매도 주문 제거
//                sellOrders.poll();
//
//                // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
//                orderBookService.updateOrderBook(symbol, sellOrder, false, false);
//
//                // 매수 이전 idx 저장
//                Long previousIdx = buyOrder.getIdx();
//
//                // 매도가 체결 되는 만큼 매수도 체결
//                // idx가 빈값으로 들어가 insert 필요
//                buyOrder.setIdx(null);
//                buyOrder.setOrderStatus(COMPLETED);
//                buyOrder.setQuantity(sellOrder.getQuantity());
//                buyOrder.setMatchedAt(LocalDateTime.now());
//                buyOrder.setMatchIdx(previousIdx + "-" + sellOrder.getIdx());
//                buyOrder.setExecutionPrice(executionPrice);   //실제 체결 되는 가격은 매수자의 가격으로 체결
//
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));
//
//                //////////////////////////////////시작////////////////////////////////////
////                        String previousUUID = buyOrder.getUuid();
////
////                        // 2. 새로운 BuyOrder 생성
////                        String uuid = buyOrder.getMemberIdx() + "_" + UUID.randomUUID();
////                        buyOrder.setUuid(uuid);
////                        buyOrder.setMatchIdx(previousUUID + "|" + sellOrder.getUuid());
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, buyOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, buyOrder);
////
////                        //체결 완료 된 데이터를 쌓아서 kafka로 전달할 list
////                        priceVolumeList.add(new PriceVolumeDTO(sellOrder));
////                        matchList.add(new CoinOrderDTO(buyOrder));
////                        matchList.add(new CoinOrderDTO(sellOrder));
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
//                orderBookService.updateOrderBook(symbol, buyOrder, true, false);
//
//                // 매수 주문 수량 업데이트 (남은 수량)
//                // 기존의 idx를 가져와 기존 매수 update
//                buyOrder.setIdx(previousIdx);
//                buyOrder.setQuantity(remainingQuantity);
//                buyOrder.setOrderStatus(PENDING);
//
//                // 미체결 수량 업데이트
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder)); // 상태 업데이트
//
//                //////////////////////////////////시작////////////////////////////////////
//                // 3. BuyOrder 업데이트
////                        buyOrder.setUuid(previousUUID);
////
////                        redisService.updateOrderInRedis(buyOrder);
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 우선순위 큐에서 매수 주문 수량도 업데이트
//                buyOrders.poll(); // 기존 주문 제거
//                buyOrders.offer(buyOrder); // 수정된 주문 다시 추가
//            } else {
//                // 매도량이 매수량을 초과
//                // 매도는 일부 남고 매수는 모두 체결
//                System.out.println("Partial match (remaining sell): BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);
//
//                // 주문 생성 시간 비교하여 오래된 주문의 가격을 체결가로 설정
//                if (buyOrder.getCreatedAt().isAfter(sellOrder.getCreatedAt())) {
//                    executionPrice = sellOrder.getOrderPrice(); // 매도 주문이 먼저 생성된 경우
//                } else {
//                    executionPrice = buyOrder.getOrderPrice(); // 매수 주문이 먼저 생성된 경우
//                }
//
//                // 매수 모두 체결 처리
//                buyOrder.setOrderStatus(COMPLETED);
//                buyOrder.setMatchedAt(LocalDateTime.now());
//                buyOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
//                buyOrder.setExecutionPrice(executionPrice);   //실제 체결 되는 가격은 매수자의 가격으로 체결
//
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));
//
//                //////////////////////////////////시작////////////////////////////////////
////                        // 1. SellOrder 업데이트
////                        buyOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
////
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, buyOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, buyOrder);
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 매수 주문 제거
//                buyOrders.poll();
//
//                // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
//                orderBookService.updateOrderBook(symbol, buyOrder, true, false);
//
//                //이전 idx 저장
//                Long previousIdx = sellOrder.getIdx();
//
//                // 매수가 체결 되는 만큼 매도도 체결
//                // idx가 빈값으로 들어가 insert 필요
//                sellOrder.setIdx(null);
//                sellOrder.setOrderStatus(COMPLETED);
//                sellOrder.setQuantity(buyOrder.getQuantity());
//                sellOrder.setMatchedAt(LocalDateTime.now());
//                sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + previousIdx);
//                sellOrder.setExecutionPrice(executionPrice);   //실제 체결 되는 가격은 매수자의 가격으로 체결
//
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));
//
//                //////////////////////////////////시작////////////////////////////////////
////                        String previousUUID = sellOrder.getUuid();
////
////                        // 2. 새로운 BuyOrder 생성
////                        String uuid = sellOrder.getMemberIdx() + "_" + UUID.randomUUID();
////                        sellOrder.setUuid(uuid);
////                        sellOrder.setMatchIdx(previousUUID + "|" + sellOrder.getUuid());
////                        redisService.deleteHashOps(PENDING + ":ORDER:" + key, sellOrder.getUuid());
////                        redisService.insertOrderInRedis(key, COMPLETED, sellOrder);
////
////                        //체결 완료 된 데이터를 쌓아서 kafka로 전달할 list
////                        priceVolumeList.add(new PriceVolumeDTO(buyOrder));
////                        matchList.add(new CoinOrderDTO(buyOrder));
////                        matchList.add(new CoinOrderDTO(sellOrder));
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
//                orderBookService.updateOrderBook(symbol, sellOrder, false, false);
//
//                // 매수 주문 수량 업데이트 (남은 수량)
//                // 기존의 idx를 가져와 update 필요
//                sellOrder.setIdx(previousIdx);
//                sellOrder.setQuantity(remainingQuantity.negate());
//                sellOrder.setOrderStatus(PENDING);
//
//                // 미체결 수량 업데이트
//                masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder)); // 상태 업데이트
//
//                //////////////////////////////////시작////////////////////////////////////
////                        // 3. BuyOrder 업데이트
////                        sellOrder.setUuid(previousUUID);
////
////                        redisService.updateOrderInRedis(sellOrder);
//                //////////////////////////////////끝////////////////////////////////////
//
//                // 우선순위 큐에서 매도 주문 수량도 업데이트
//                sellOrders.poll(); // 기존 주문 제거
//                sellOrders.offer(sellOrder); // 수정된 주문 다시 추가
//            }
//        }

//        //반복하는 동안 쌓인 가격과 볼륨 리스트 kafka로 전달(실시간 차트에서 사용)
//        if (!priceVolumeList.isEmpty()) {
//            Map<String, List<PriceVolumeDTO>> priceVolumeMap = new HashMap<>();
//            priceVolumeMap.put(key, priceVolumeList);
//            priceVolumeMapKafkaTemplate.send("Price-Volume", priceVolumeMap);
//        }
//
//        //반복하는 동안 쌓인 완료 주문 리스트 kafka로 전달(웹소켓을 통해 완료 리스트를 사용자에게 전달하기 위함)
//        if (!matchList.isEmpty()) {
//            Map<String, List<CoinOrderDTO>> matchListeMap = new HashMap<>();
//            matchListeMap.put(key, matchList);
//            matchListKafkaTemplate.send("Match-List", matchListeMap);
//        }
    }

    public PriorityQueue<CoinOrderDTO> getOppositeOrdersQueue(CoinOrderDTO order, String key) {
        return (order.getOrderType() == BUY)
                ? orderQueueService.getSellOrderQueue(key)
                : orderQueueService.getBuyOrderQueue(key);
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

    public BigDecimal calculateRemainingQuantity(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        return order.getQuantity().subtract(oppositeOrder.getQuantity());
    }

    public BigDecimal getExecutionPrice(CoinOrderDTO oppositeOrder) {
        return oppositeOrder.getOrderPrice();
    }

    public void processNonMatchedOrder(String key, CoinOrderDTO order){
        if (order.getOrderStatus() == PENDING && order.getOrderPrice().compareTo(BigDecimal.ZERO) > 0) {
            addPendingOrder(key, order);
        }
    }

    //미체결 주문 저장
    public void addPendingOrder(String key, CoinOrderDTO order) {
        CoinOrder orderEntity = CoinOrderMapper.toEntity(order);
        masterCoinOrderRepository.save(orderEntity);

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

    // 체결 상태 변경
    public void updateOrderStatus(CoinOrderDTO order, CoinOrderDTO oppositeOrder,
                                  BigDecimal executionPrice, OrderStatus orderStatus,
                                  Long idx, BigDecimal quantity) {
        order.setIdx(idx);
        order.setOrderStatus(orderStatus);
        order.setExecutionPrice(executionPrice);
        order.setQuantity(quantity);

        if(orderStatus == COMPLETED){
            order.setMatchedAt(LocalDateTime.now());
            order.setMatchIdx(order.getIdx() + "|" + oppositeOrder.getIdx());
        } else {
            order.setMatchIdx(null);
            order.setMatchedAt(null);
        }
    }

    //완전 체결
    public void processCompleteMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, BigDecimal executionPrice) {
        System.out.println("완전체결 : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 주문과 반대주문 모두 체결 상태 변경
        updateOrderStatus(order, oppositeOrder, executionPrice, COMPLETED, order.getIdx() ,null);
        updateOrderStatus(oppositeOrder, order, executionPrice, COMPLETED, order.getIdx() ,null);

        // 주문과 반대주문 모두 DB 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(oppositeOrder));
    }

    //주문이 반대 주문보다 클때
    private void processOversizeMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key,
                                      PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity,
                                      BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 크다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

        // 반대 주문 모두 체결 처리
        updateOrderStatus(oppositeOrder, order, executionPrice, COMPLETED, oppositeOrder.getIdx(), null);

        // 나의 주문 이전 Idx 저장
        Long previousIdx = order.getIdx();

        // 나의 주문 부분 체결 처리 (새로운 row 생성)
        updateOrderStatus(order, oppositeOrder, executionPrice, COMPLETED, null,null);

        // 주문과 반대주문 모두 DB 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(oppositeOrder));

        // 나의 주문 남은 수량을 잔여 수량으로 설정
        updateOrderStatus(order, oppositeOrder, executionPrice, PENDING, previousIdx, remainingQuantity);
        
        // 미체결 수량 업데이트
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
    }

    //주문이 반대 주문보다 작을때
    private void processUndersizedMatch(CoinOrderDTO order, CoinOrderDTO oppositeOrder, String key,
                                        PriorityQueue<CoinOrderDTO> queue, BigDecimal remainingQuantity,
                                        BigDecimal executionPrice) {
        System.out.println("부분체결 (주문이 반대 주문보다 작다) : " + " 주문 : " + order + " 반대 주문 : " + oppositeOrder);

    }
}
