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
    private final KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate;

    @Autowired
    public CompletedOrderProcessorService(RedisService redisService,
                                  MasterCoinOrderRepository masterCoinOrderRepository,
                                  SlaveCoinOrderRepository slaveCoinOrderRepository,
                                  @Qualifier("coinOrderListKafkaTemplate") KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate) {
        this.redisService = redisService;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void completedProcessorOrder(List<CoinOrderDTO> orders) {
        List<String> uuids = extractUuids(orders);
        List<String> existingUuids = findExistingUuids(uuids);
        List<CoinOrder> newOrders = filterNewOrders(orders, existingUuids);
        processNewOrders(newOrders);
    }

    private List<String> extractUuids(List<CoinOrderDTO> orders) {
        return orders.stream()
                .map(CoinOrderDTO::getUuid)
                .toList();
    }

    private List<String> findExistingUuids(List<String> uuids) {
        return slaveCoinOrderRepository.findAllByUuidIn(uuids)
                .stream()
                .map(CoinOrder::getUuid)
                .toList();
    }

    private List<CoinOrder> filterNewOrders(List<CoinOrderDTO> orders, List<String> existingUuids) {
        return orders.stream()
                .filter(order -> !existingUuids.contains(order.getUuid()))
                .map(CoinOrderMapper::toEntity)
                .toList();
    }

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

    private void deleteFromRedis(List<CoinOrder> newOrders) {
        for (CoinOrder newOrder : newOrders) {
            redisService.deleteHashOps("PENDING:ORDER:" + newOrder.getCoinName() + "-" + newOrder.getMarketName(), newOrder.getUuid());
        }
    }

    private void handleException(List<CoinOrder> newOrders, Exception e) {
        List<CoinOrderDTO> failedOrders = newOrders.stream()
                .map(CoinOrderMapper::fromEntity)
                .toList();
        kafkaTemplate.send("Order-Completed", failedOrders.get(0).getCoinName() + "-" + failedOrders.get(0).getMarketName(), failedOrders);
        System.err.println("Batch insert failed, sending to Kafka: " + e.getMessage());
    }
}
