package com.mjy.exchange.service;

import com.mjy.exchange.dto.*;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinInfoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mjy.exchange.enums.OrderStatus.PENDING;

@Service
public class OrderService {
    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;
    private final SlaveCoinInfoRepository slaveCoinInfoRepository;
    private final KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate;

    public OrderService(MasterCoinHoldingRepository masterCoinHoldingRepository,
                        SlaveCoinHoldingRepository slaveCoinHoldingRepository,
                        SlaveCoinInfoRepository slaveCoinInfoRepository,
                        @Qualifier("coinOrderKafkaTemplate") KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate) {
        this.masterCoinHoldingRepository = masterCoinHoldingRepository;
        this.slaveCoinHoldingRepository = slaveCoinHoldingRepository;
        this.slaveCoinInfoRepository = slaveCoinInfoRepository;
        this.coinOrderKafkaTemplate = coinOrderKafkaTemplate;
    }

    public void processOrder(OrderRequest orderRequest, String memberUuid) {
        //1.지갑 여부 확인
        CoinHolding coinHolding = slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, orderRequest.getCoinName())
                .orElseThrow(() -> new IllegalArgumentException("지갑이 생성되지 않았습니다."));

        //2.주문 금액과 수수료 확인
        BigDecimal totalOrderAmount = orderRequest.getCoinAmount().multiply(orderRequest.getOrderPrice());
        
        CoinInfo coinInfo = slaveCoinInfoRepository.findByMarketNameAndCoinName(orderRequest.getMarketName(), orderRequest.getCoinName())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 심볼 정보입니다."));

        BigDecimal fee = totalOrderAmount.multiply(coinInfo.getFeeRate());

        //3.잔액 확인 및 차감
        if (coinHolding.getAvailableAmount().compareTo(totalOrderAmount.add(fee)) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        coinHolding.setAvailableAmount(coinHolding.getAvailableAmount().subtract(totalOrderAmount.add(fee)));
        masterCoinHoldingRepository.save(coinHolding);

        //4.CoinOrderDTO 입력
        CoinOrder coinOrder = new CoinOrder();
        coinOrder.setMemberUuid(memberUuid);
        coinOrder.setMarketName(orderRequest.getMarketName());
        coinOrder.setCoinName(orderRequest.getCoinName());
        coinOrder.setCoinAmount(new BigDecimal(String.valueOf(orderRequest.getCoinAmount())));
        coinOrder.setOrderPrice(new BigDecimal(String.valueOf(orderRequest.getOrderPrice())));
        coinOrder.setOrderType(orderRequest.getOrderType());
        coinOrder.setOrderStatus(PENDING);
        coinOrder.setFee(new BigDecimal(String.valueOf(coinInfo.getFeeRate())));
        coinOrder.setCreatedAt(LocalDateTime.now());

        //5. kafka send
        coinOrderKafkaTemplate.send(orderRequest.getCoinName() + "-" + orderRequest.getMarketName(), coinOrder);
    }
}

