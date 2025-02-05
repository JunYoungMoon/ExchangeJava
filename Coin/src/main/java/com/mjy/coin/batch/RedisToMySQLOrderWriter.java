//package com.mjy.coin.batch;
//
//import com.mjy.coin.dto.CoinOrderDTO;
//import com.mjy.coin.dto.CoinOrderMapper;
//import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
//import com.mjy.coin.service.RedisService;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//@StepScope
//public class RedisToMySQLOrderWriter implements ItemWriter<CoinOrderDTO> {
//
//    private final MasterCoinOrderRepository masterCoinOrderRepository;
//    private final String redisKey;
//    private final RedisService redisService;
//
//    public RedisToMySQLOrderWriter(RedisService redisService,
//                                   MasterCoinOrderRepository masterCoinOrderRepository,
//                                   @Value("#{jobParameters['redisKey']}") String redisKey) {
//        this.masterCoinOrderRepository = masterCoinOrderRepository;
//        this.redisKey = redisKey;
//        this.redisService = redisService;
//    }
//
//    @Override
//    public void write(Chunk<? extends CoinOrderDTO> items){
//        List<CoinOrderDTO> itemList = new ArrayList<>();
//
//        for (CoinOrderDTO item : items) {
//            itemList.add(item);
//        }
//
//        masterCoinOrderRepository.saveAll(CoinOrderMapper.toEntityList(itemList));
//
//        for (CoinOrderDTO item : items) {
//            redisService.deleteHashOps("COMPLETED:ORDER:" + redisKey, item.getUuid());
//        }
//    }
//}
