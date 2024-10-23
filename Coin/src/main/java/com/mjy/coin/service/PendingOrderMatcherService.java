package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class PendingOrderMatcherService {

    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final OrderBookService orderBookService;
    private final RedisService redisService;
    private final KafkaTemplate<String, CoinOrderDTO> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public PendingOrderMatcherService(MasterCoinOrderRepository masterCoinOrderRepository,
                                      OrderBookService orderBookService,
                                      RedisService redisService,
                                      KafkaTemplate<String, CoinOrderDTO> kafkaTemplate,
                                      SimpMessagingTemplate messagingTemplate) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.orderBookService = orderBookService;
        this.redisService = redisService;
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
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
                        buyOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
                        buyOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

                        // 매수와 매도 체결된 상태를 DB에 기록
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////
                        // 1. BuyOrder 업데이트
                        buyOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
                        redisService.updateOrderInRedis(buyOrder);

                        // 2. SellOrder 업데이트
                        sellOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());
                        redisService.updateOrderInRedis(sellOrder);
                        //////////////////////////////////끝////////////////////////////////////

                        kafkaTemplate.send("Order-Completed", buyOrder);
                        kafkaTemplate.send("Order-Completed", sellOrder);

                        // 큐에서 양쪽 주문 제거
                        buyOrders.poll();
                        sellOrders.poll();

                        // 체결 되었으니 호가 리스트 제거
                        orderBookService.updateOrderBook(key, buyOrder, true, false);
                        orderBookService.updateOrderBook(key, sellOrder, false, false);

                        messagingTemplate.convertAndSend("/topic/coin/" + key + "/order", buyOrder);
                    } else if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        // 매도량 보다 매수량이 높은경우
                        // 매도는 모두 체결되고 매수는 일부 남음
                        System.out.println("Partial match (remaining buy): BuyOrder: " + buyOrder + " with SellOrder: " + sellOrder);

                        // 매도 모두 체결 처리
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////
                        // 1. SellOrder 업데이트
                        sellOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());

                        redisService.updateOrderInRedis(sellOrder);
                        //////////////////////////////////끝////////////////////////////////////

                        // 매도 주문 제거
                        sellOrders.poll();

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookService.updateOrderBook(key, sellOrder, false, false);

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

                        // 2. 새로운 BuyOrder 생성
                        String uuid = UUID.randomUUID() + "_" + buyOrder.getMemberId();
                        buyOrder.setUuid(uuid);
                        buyOrder.setMatchIdx(previousUUID + "|" + sellOrder.getUuid());
                        redisService.insertOrderInRedis(buyOrder);
                        //////////////////////////////////끝////////////////////////////////////

                        kafkaTemplate.send("Order-Completed", buyOrder);
                        kafkaTemplate.send("Order-Completed", sellOrder);

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookService.updateOrderBook(key, buyOrder, true, false);

                        // 매수 주문 수량 업데이트 (남은 수량)
                        // 기존의 idx를 가져와 기존 매수 update
                        buyOrder.setIdx(previousIdx);
                        buyOrder.setCoinAmount(remainingQuantity);
                        buyOrder.setOrderStatus(OrderStatus.PENDING);

                        // 미체결 수량 업데이트
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder)); // 상태 업데이트

                        //////////////////////////////////시작////////////////////////////////////
                        // 3. BuyOrder 업데이트
                        buyOrder.setUuid(previousUUID);

                        redisService.updateOrderInRedis(buyOrder);
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
                        buyOrder.setMatchIdx(buyOrder.getIdx() + "|" + sellOrder.getIdx());
                        buyOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(buyOrder));

                        //////////////////////////////////시작////////////////////////////////////
                        // 1. SellOrder 업데이트
                        buyOrder.setMatchIdx(buyOrder.getUuid() + "|" + sellOrder.getUuid());

                        redisService.updateOrderInRedis(buyOrder);
                        //////////////////////////////////끝////////////////////////////////////

                        // 매수 주문 제거
                        buyOrders.poll();

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookService.updateOrderBook(key, buyOrder, true, false);

                        //이전 idx 저장
                        Long previousIdx = sellOrder.getIdx();

                        // 매수가 체결 되는 만큼 매도도 체결
                        // idx가 빈값으로 들어가 insert 필요
                        sellOrder.setIdx(null);
                        sellOrder.setOrderStatus(OrderStatus.COMPLETED);
                        sellOrder.setCoinAmount(buyOrder.getCoinAmount());
                        sellOrder.setMatchedAt(LocalDateTime.now());
                        sellOrder.setMatchIdx(buyOrder.getIdx() + "|" + previousIdx);
                        sellOrder.setExecutionPrice(buyOrder.getOrderPrice());   //실제 체결 되는 가격은 매수자의 가격으로 체결

//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder));

                        //////////////////////////////////시작////////////////////////////////////
                        String previousUUID = sellOrder.getUuid();

                        // 2. 새로운 BuyOrder 생성
                        String uuid = UUID.randomUUID() + "_" + sellOrder.getMemberId();
                        sellOrder.setUuid(uuid);
                        sellOrder.setMatchIdx(previousUUID + "|" + sellOrder.getUuid());
                        redisService.insertOrderInRedis(sellOrder);
                        //////////////////////////////////끝////////////////////////////////////

                        kafkaTemplate.send("Order-Completed", buyOrder);
                        kafkaTemplate.send("Order-Completed", sellOrder);

                        // 이미 미체결을 넣어줬기 때문에 체결 되었으니 호가 리스트 제거(가격만 구분하고 수량 차감은 같이 한다.)
                        orderBookService.updateOrderBook(key, sellOrder, false, false);

                        // 매수 주문 수량 업데이트 (남은 수량)
                        // 기존의 idx를 가져와 update 필요
                        sellOrder.setIdx(previousIdx);
                        sellOrder.setCoinAmount(remainingQuantity.negate());
                        sellOrder.setOrderStatus(OrderStatus.PENDING);

                        // 미체결 수량 업데이트
//                        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(sellOrder)); // 상태 업데이트

                        //////////////////////////////////시작////////////////////////////////////
                        // 3. BuyOrder 업데이트
                        sellOrder.setUuid(previousUUID);

                        redisService.updateOrderInRedis(sellOrder);
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
