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
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;

    public BalanceService(SlaveCoinHoldingRepository slaveRepo) {
        this.slaveCoinHoldingRepository = slaveRepo;
    }

    public void checkBalance(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo) {
        BigDecimal totalOrderAmount = orderRequest.getCoinAmount().multiply(orderRequest.getOrderPrice());
        BigDecimal fee = totalOrderAmount.multiply(coinInfo.getFeeRate());

        // 마켓 잔액 검증 및 차감
        CoinHolding marketHolding = slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, orderRequest.getMarketName())
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재 하지 않습니다."));

        if (marketHolding.getAvailableAmount().compareTo(totalOrderAmount.add(fee)) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
    }
}
