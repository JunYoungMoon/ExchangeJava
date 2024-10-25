package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompletedOrderProcessorService {

    private final RedisService redisService;
    private final MasterCoinOrderRepository masterCoinOrderRepository;
    private final SlaveCoinOrderRepository slaveCoinOrderRepository;
//    private final KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate;

    @Autowired
    public CompletedOrderProcessorService(RedisService redisService,
                                  MasterCoinOrderRepository masterCoinOrderRepository,
                                  SlaveCoinOrderRepository slaveCoinOrderRepository
//                                  @Qualifier("listCoinOrderKafkaTemplate") KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate
                                  /*KafkaTemplate<String, CoinOrderDTO> kafkaTemplate*/) {
        this.redisService = redisService;
        this.masterCoinOrderRepository = masterCoinOrderRepository;
        this.slaveCoinOrderRepository = slaveCoinOrderRepository;
//        this.kafkaTemplate = kafkaTemplate;
    }

    public void completedProcessorOrder(List<CoinOrderDTO> order){
//        if (slaveCoinOrderRepository.findByUuid(order.getUuid()).isEmpty()) {
//            // 동기적으로 주문 저장
//            try {
//                saveOrderSync(order);
//            } catch (Exception e) {
//                System.err.println("Error processing completed order: " + e.getMessage());
//                kafkaTemplate.send("Order-Completed", order.getCoinName() + "-" + order.getMarketName(), order);
//            }
//        }
    }

    private void saveOrderSync(CoinOrderDTO order) {
        // MySQL에 동기로 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));

        // Redis에서 PENDING ORDER 삭제
        redisService.deleteHashOps("PENDING:ORDER:" + order.getCoinName() + "-" + order.getMarketName(), order.getUuid());
    }
}
