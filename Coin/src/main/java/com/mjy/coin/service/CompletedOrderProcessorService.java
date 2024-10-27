package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.mjy.coin.entity.coin.CoinOrder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompletedOrderProcessorService {

    private final RedisService redisService;
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
    private final KafkaTemplate<String, List<CoinOrderDTO>> coinOrderListKafkaTemplate;

    @Autowired
    public CompletedOrderProcessorService(RedisService redisService,
                                          MasterCoinOrderRepository masterCoinOrderRepository,
                                          SlaveCoinOrderRepository slaveCoinOrderRepository,
                                          @Qualifier("coinOrderListKafkaTemplate") KafkaTemplate<String, List<CoinOrderDTO>> coinOrderListKafkaTemplate) {
        this.redisService = redisService;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.coinOrderListKafkaTemplate = coinOrderListKafkaTemplate;
    }

    public void completedProcessorOrder(List<CoinOrderDTO> orders) {
        // 1. 주문 목록에서 UUID 목록을 추출
        List<String> uuids = extractUuids(orders);

        // 2. 기존에 저장된 UUID 목록을 조회
        List<String> existingUuids = findExistingUuids(uuids);

        // 3. 새로운 주문을 필터링하여 리스트 생성
        List<CoinOrder> newOrders = filterNewOrders(orders, existingUuids);

        // 4. 새로운 주문을 처리
        processNewOrders(newOrders);
    }

    //주문 목록에서 각 주문의 UUID를 추출하여 UUID 목록을 반환
    private List<String> extractUuids(List<CoinOrderDTO> orders) {
        return orders.stream()
                .map(CoinOrderDTO::getUuid)
                .toList();
    }

    //주어진 UUID 목록에서 데이터베이스에 이미 존재하는 UUID를 조회하여 반환
    private List<String> findExistingUuids(List<String> uuids) {
        return slaveCoinOrderRepository.findAllByUuidIn(uuids)
                .stream()
                .map(CoinOrder::getUuid)
                .toList();
    }

    //기존 주문을 제외한 새로운 주문 목록을 필터링하여 반환
    private List<CoinOrder> filterNewOrders(List<CoinOrderDTO> orders, List<String> existingUuids) {
        return orders.stream()
                .filter(order -> !existingUuids.contains(order.getUuid()))
                .map(CoinOrderMapper::toEntity)
                .toList();
    }

    //새로운 주문 목록을 데이터베이스에 저장하고, Redis에서 해당 주문 정보를 삭제
    //저장 중 오류가 발생할 경우 예외를 처리
    private void processNewOrders(List<CoinOrder> newOrders) {
        try {
            if (!newOrders.isEmpty()) {
                masterCoinOrderRepository.saveAll(newOrders);
                deleteFromRedis(newOrders);
            }
        } catch (Exception e) {
            handleException(newOrders, e);
        }
    }

    //새로운 주문 목록에 해당하는 Redis 데이터를 삭제
    private void deleteFromRedis(List<CoinOrder> newOrders) {
        for (CoinOrder newOrder : newOrders) {
            redisService.deleteHashOps("PENDING:ORDER:" + newOrder.getCoinName() + "-" + newOrder.getMarketName(), newOrder.getUuid());
        }
    }

    //예외 발생 시, 처리하지 못한 주문 목록을 Kafka로 전송
    private void handleException(List<CoinOrder> newOrders, Exception e) {
        List<CoinOrderDTO> failedOrders = newOrders.stream()
                .map(CoinOrderMapper::fromEntity)
                .toList();
        coinOrderListKafkaTemplate.send("Order-Completed", failedOrders.get(0).getCoinName() + "-" + failedOrders.get(0).getMarketName(), failedOrders);
        System.err.println("Batch insert failed, sending to Kafka: " + e.getMessage());
    }
}
