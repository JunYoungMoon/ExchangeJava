package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceService {

    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;

    public BalanceService(MasterCoinHoldingRepository masterCoinHoldingRepository, SlaveCoinHoldingRepository slaveCoinHoldingRepository) {
        this.masterCoinHoldingRepository = masterCoinHoldingRepository;
        this.slaveCoinHoldingRepository = slaveCoinHoldingRepository;
    }

    public CoinHolding checkAndUpdateBalance(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo) {
        CoinHolding coinHolding = slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, orderRequest.getCoinName())
                .orElseThrow(() -> new IllegalArgumentException("지갑이 생성되지 않았습니다."));

        BigDecimal totalOrderAmount = orderRequest.getCoinAmount().multiply(orderRequest.getOrderPrice());
        BigDecimal fee = totalOrderAmount.multiply(coinInfo.getFeeRate());

        if (coinHolding.getAvailableAmount().compareTo(totalOrderAmount.add(fee)) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        coinHolding.setAvailableAmount(coinHolding.getAvailableAmount().subtract(totalOrderAmount.add(fee)));
        masterCoinHoldingRepository.save(coinHolding);

        return coinHolding;
    }
}
