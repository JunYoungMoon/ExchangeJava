package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.ConvertService;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisToMySQLOrderProcessor implements ItemProcessor<Map.Entry<String, String>, CoinOrderDTO> {

    private final ConvertService convertService;

    public RedisToMySQLOrderProcessor(ConvertService convertService) {
        this.convertService = convertService;
    }

    @Override
    public CoinOrderDTO process(Map.Entry<String, String> entry){
        String jsonValue = entry.getValue();

        return convertService.convertStringToObject(jsonValue, CoinOrderDTO.class);
    }
}
