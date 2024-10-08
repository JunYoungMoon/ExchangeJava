package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class OrderMatcher {

    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookManager orderBookManager;
    private final RedisService redisService;

    @Autowired
    public OrderMatcher(MasterCoinOrderRepository masterCoinOrderRepository, OrderBookManager orderBookManager, RedisService redisService) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookManager = orderBookManager;
        this.redisService = redisService;
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
                // 체결 조건에서 최종적으로 결정되는 기준은 매수자의 가격
                if (buyOrder.getOrderPrice().compareTo(sellOrder.getOrderPrice()) >= 0) {
                    // 체결 가능 시 처리
                    BigDecimal buyQuantity = buyOrder.getCoinAmount();
                    BigDecimal sellQuantity = sellOrder.getCoinAmount();
                    BigDecimal remainingQuantity = buyQuantity.subtract(sellQuantity).setScale(8, RoundingMode.DOWN).stripTrailingZeros();

                    if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        // 완전체결
                        // 매수와 매도 모두 체결
                        System.out.println("Matched completely: BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);

                        // 주문 삽입 (완전체결인 경우)
                        // 매수와 매도 모두 체결로 처리
                        buyOrder.setOrderStatus(OrderStatus.COMPLETED);
                        buyOrder.setMatchedAt(LocalDateTime.now());
                        buyOrder.setMatchIdx(buyOrder.getIdx() + "-" + sellOrder.getIdx());
                        buyOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "-" + sellOrder.getIdx());
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

                        // 매수와 매도 체결된 상태를 DB에 기록
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////

                        // 1. Redis에서 기존 데이터 가져오기
                        String buyOrderData = redisService.getHashOps(key, buyOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        Map<String, String> orderDataMap = redisService.convertStringToMap(buyOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", buyOrder.getUuid() + "-" + sellOrder.getUuid());
                        orderDataMap.put("executionPrice", buyOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        String updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(buyOrder.getUuid(), updatedOrderData));

                        // 1. Redis에서 기존 데이터 가져오기
                        String sellOrderData = redisService.getHashOps(key, sellOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        orderDataMap = redisService.convertStringToMap(sellOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", buyOrder.getUuid() + "-" + sellOrder.getUuid());
                        orderDataMap.put("executionPrice", sellOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(sellOrder.getUuid(), updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 큐에서 양쪽 주문 제거
                        buyOrders.poll();
                        sellOrders.poll();

                        // 체결 되었으니 호가 리스트 제거
                        orderBookManager.updateOrderBook(key, buyOrder, true, false);
                        orderBookManager.updateOrderBook(key, sellOrder, false, false);
                    } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        // 매도량 보다 매수량이 높은경우
                        // 매도는 모두 체결되고 매수는 일부 남음
                        System.out.println("Partial match (remaining buy): BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);

                        // 매도 모두 체결 처리
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "-" + sellOrder.getIdx());
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////

                        // 1. Redis에서 기존 데이터 가져오기
                        String sellOrderData = redisService.getHashOps(key, sellOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        Map<String, String> orderDataMap = redisService.convertStringToMap(sellOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", buyOrder.getUuid() + "-" + sellOrder.getUuid());
                        orderDataMap.put("executionPrice", buyOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        String updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(sellOrder.getUuid(), updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 매도 주문 제거
                        sellOrders.poll();

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookManager.updateOrderBook(key, sellOrder, false, false);

                        // 매수 이전 idx 저장
                        Long previousIdx = buyOrder.getIdx();

                        // 매도가 체결 되는 만큼 매수도 체결
                        // idx가 빈값으로 들어가 insert 필요
                        buyOrder.setIdx(null);
                        buyOrder.setOrderStatus(OrderStatus.COMPLETED);
                        buyOrder.setCoinAmount(sellOrder.getCoinAmount());
                        buyOrder.setMatchedAt(LocalDateTime.now());
                        buyOrder.setMatchIdx(previousIdx + "-" + sellOrder.getIdx());
                        buyOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));

                        //////////////////////////////////시작////////////////////////////////////

                        String previousUUID = buyOrder.getUuid();

                        // 1. Redis에서 기존 데이터 가져오기
                        String buyOrderData = redisService.getHashOps(key, buyOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        orderDataMap = redisService.convertStringToMap(buyOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        String uuid = UUID.randomUUID().toString();
                        orderDataMap.put("uuid", uuid);
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("coinAmount", sellOrder.getCoinAmount().toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", previousUUID + "-" + sellOrder.getUuid());
                        orderDataMap.put("executionPrice", buyOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(uuid, updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookManager.updateOrderBook(key, buyOrder, true, false);

                        // 매수 주문 수량 업데이트 (남은 수량)
                        // 기존의 idx를 가져와 기존 매수 update
                        buyOrder.setIdx(previousIdx);
                        buyOrder.setCoinAmount(remainingQuantity);
                        buyOrder.setOrderStatus(OrderStatus.PENDING);

                        // 미체결 수량 업데이트
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder)); // 상태 업데이트

                        //////////////////////////////////시작////////////////////////////////////

                        // 1. Redis에서 기존 데이터 가져오기
                        buyOrderData = redisService.getHashOps(key, previousUUID);

                        // 2. String 데이터를 Map으로 변환
                        orderDataMap = redisService.convertStringToMap(buyOrderData);

                        orderDataMap.put("orderStatus", OrderStatus.PENDING.toString());
                        orderDataMap.put("coinAmount", remainingQuantity.toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(buyOrder.getUuid(), updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 우선순위 큐에서 매수 주문 수량도 업데이트
                        buyOrders.poll(); // 기존 주문 제거
                        buyOrders.offer(buyOrder); // 수정된 주문 다시 추가
                    } else {
                        // 매수량 보다 매도량이 높은경우
                        // 매수는 모두 체결되고 매도는 일부 남음
                        System.out.println("Partial match (remaining sell): BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);

                        // 매수는 모두 체결되고 매도는 일부 남음
                        // 매수 모두 체결 처리
                        buyOrder.setOrderStatus(OrderStatus.COMPLETED);
                        buyOrder.setMatchedAt(LocalDateTime.now());
                        buyOrder.setMatchIdx(buyOrder.getIdx() + "-" + sellOrder.getIdx());
                        buyOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));

                        //////////////////////////////////시작////////////////////////////////////

                        // 1. Redis에서 기존 데이터 가져오기
                        String buyOrderData = redisService.getHashOps(key, buyOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        Map<String, String> orderDataMap = redisService.convertStringToMap(buyOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", buyOrder.getUuid() + "-" + sellOrder.getUuid());
                        orderDataMap.put("executionPrice", buyOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        String updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(buyOrder.getUuid(), updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 매수 주문 제거
                        buyOrders.poll();

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookManager.updateOrderBook(key, buyOrder, true, false);

                        //이전 idx 저장
                        Long previousIdx = sellOrder.getIdx();

                        // 매수가 체결 되는 만큼 매도도 체결
                        // idx가 빈값으로 들어가 insert 필요
                        sellOrder.setIdx(null);
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setCoinAmount(buyOrder.getCoinAmount());
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "-" + previousIdx);
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////

                        String previousUUID = sellOrder.getUuid();

                        // 1. Redis에서 기존 데이터 가져오기
                        String sellOrderData = redisService.getHashOps(key, sellOrder.getUuid());

                        // 2. String 데이터를 Map으로 변환
                        orderDataMap = redisService.convertStringToMap(sellOrderData);

                        // 3. 데이터를 수정 (예: orderStatus 업데이트)
                        String uuid = UUID.randomUUID().toString();
                        orderDataMap.put("uuid", uuid);
                        orderDataMap.put("orderStatus", OrderStatus.COMPLETED.toString());
                        orderDataMap.put("coinAmount", buyOrder.getCoinAmount().toString());
                        orderDataMap.put("matchedAt", LocalDateTime.now().toString());
                        orderDataMap.put("matchIdx", buyOrder.getUuid() + "-" + previousUUID);
                        orderDataMap.put("executionPrice", buyOrder.getOrderPrice().toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(uuid, updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookManager.updateOrderBook(key, sellOrder, false, false);

                        // 매수 주문 수량 업데이트 (남은 수량)
                        // 기존의 idx를 가져와 update 필요
                        sellOrder.setIdx(previousIdx);
                        sellOrder.setCoinAmount(remainingQuantity.negate());
                        sellOrder.setOrderStatus(OrderStatus.PENDING);

                        // 미체결 수량 업데이트
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder)); // 상태 업데이트

                        //////////////////////////////////시작////////////////////////////////////

                        // 1. Redis에서 기존 데이터 가져오기
                        sellOrderData = redisService.getHashOps(key, previousUUID);

                        // 2. String 데이터를 Map으로 변환
                        orderDataMap = redisService.convertStringToMap(sellOrderData);

                        orderDataMap.put("orderStatus", OrderStatus.PENDING.toString());
                        orderDataMap.put("coinAmount", remainingQuantity.toString());

                        // 4. Map 데이터를 다시 String으로 직렬화
                        updatedOrderData = redisService.convertMapToString(orderDataMap);

                        // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
                        redisService.setHashOps(key, Map.of(sellOrder.getUuid(), updatedOrderData));

                        //////////////////////////////////끝////////////////////////////////////

                        // 우선순위 큐에서 매도 주문 수량도 업데이트
                        sellOrders.poll(); // 기존 주문 제거
                        sellOrders.offer(sellOrder); // 수정된 주문 다시 추가
                    }
                } else {
                    break; // 더 이상 체결할 수 없으면 중단
                }
            }
        }
    }
}