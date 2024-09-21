package com.member.exchange.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.member.exchange.entity.CoinInfo;
import com.member.exchange.repository.slave.SlaveCoinInfoRepository;
import com.member.exchange.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class CoinInfoInitializer {

    private final SlaveCoinInfoRepository slaveCoinInfoRepository;
    private final RedisService redisService;

    public CoinInfoInitializer(SlaveCoinInfoRepository slaveCoinInfoRepository, RedisService redisService) {
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        List<CoinInfo> coinInfoList = slaveCoinInfoRepository.findAll();

        redisService.setValues("CoinInfo ", new ObjectMapper().writeValueAsString(coinInfoList));

        System.out.println("CoinInfo list saved to Redis on startup.");
    }
}
