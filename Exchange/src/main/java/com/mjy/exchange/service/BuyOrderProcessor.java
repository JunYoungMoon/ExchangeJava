package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.enums.OrderType;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.mjy.exchange.enums.OrderType.BUY;

@Service
public class BuyOrderProcessor implements OrderProcessor {
    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;

    public BuyOrderProcessor(MasterCoinHoldingRepository masterRepo, SlaveCoinHoldingRepository slaveRepo) {
        this.masterCoinHoldingRepository = masterRepo;
        this.slaveCoinHoldingRepository = slaveRepo;
    }

    @Override
    public OrderType getOrderType() {
        return BUY;
    }

    @Override
    public void process(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo) {
        BigDecimal totalOrderAmount = orderRequest.getCoinAmount().multiply(orderRequest.getOrderPrice());
        BigDecimal fee = totalOrderAmount.multiply(coinInfo.getFeeRate());

        // 마켓 잔액 검증 및 차감
        CoinHolding marketHolding = slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, orderRequest.getMarketName())
                .orElseThrow(() -> new IllegalArgumentException("마켓 잔액이 부족합니다."));

        if (marketHolding.getAvailableAmount().compareTo(totalOrderAmount.add(fee)) < 0) {
            throw new IllegalArgumentException("마켓 잔액이 부족합니다.");
        }

        marketHolding.setAvailableAmount(marketHolding.getAvailableAmount().subtract(totalOrderAmount.add(fee)));
        masterCoinHoldingRepository.save(marketHolding);
    }
}
