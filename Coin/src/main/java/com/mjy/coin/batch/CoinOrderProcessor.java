package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CoinOrderProcessor implements ItemProcessor<CoinOrderDTO, CoinOrderDTO> {

    @Override
    public CoinOrderDTO process(CoinOrderDTO item) {
        return item;
    }
}
