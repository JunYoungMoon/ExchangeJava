package com.mjy.exchange.service;

import com.mjy.exchange.dto.*;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.repository.slave.SlaveCoinInfoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final SlaveCoinInfoRepository slaveCoinInfoRepository;
    private final BalanceService balanceService;
    private final KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate;

    public OrderService(SlaveCoinInfoRepository slaveCoinInfoRepository,
                        BalanceService balanceService,
                        @Qualifier("coinOrderKafkaTemplate") KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate) {
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.balanceService = balanceService;
        this.coinOrderKafkaTemplate = coinOrderKafkaTemplate;
    }

    public void processOrder(OrderRequest orderRequest, String memberUuid) {
        CoinInfo coinInfo = slaveCoinInfoRepository.findByMarketNameAndCoinName(orderRequest.getMarketName(), orderRequest.getCoinName())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 심볼 정보입니다."));

        //1. 지갑 정보 조회 및 주문 검증
        CoinHolding coinHolding = balanceService.checkAndUpdateBalance(memberUuid, orderRequest, coinInfo);

        //2. 주문 생성
        CoinOrder coinOrder = CoinOrderFactory.createCoinOrder(orderRequest, coinInfo, memberUuid);

        //3. kafka send
        coinOrderKafkaTemplate.send(orderRequest.getCoinName() + "-" + orderRequest.getMarketName(), coinOrder);
    }
}

