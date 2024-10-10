package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderMapper;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CompletedOrderProcessor implements ItemProcessor<CoinOrderDTO, CoinOrderDTO> {

    private final MasterCoinOrderRepository masterCoinOrderRepository;

    public CompletedOrderProcessor(MasterCoinOrderRepository masterCoinOrderRepository) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
    }

    @Override
    public CoinOrderDTO process(CoinOrderDTO order) {
        // MySQL에 저장
        masterCoinOrderRepository.save(CoinOrderMapper.toEntity(order));
        return order; // 후속 처리 단계로 전달
    }
}
