package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.enums.OrderType;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
import org.springframework.stereotype.Service;

import static com.mjy.exchange.enums.OrderType.SELL;

@Service
public class SellOrderProcessor implements OrderProcessor {
    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;

    public SellOrderProcessor(MasterCoinHoldingRepository masterRepo, SlaveCoinHoldingRepository slaveRepo) {
        this.masterCoinHoldingRepository = masterRepo;
        this.slaveCoinHoldingRepository = slaveRepo;
    }

    @Override
    public OrderType getOrderType() {
        return SELL;
    }

    @Override
    public void process(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo) {
        // 코인 잔액 검증 및 차감
        CoinHolding coinHolding = slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, orderRequest.getCoinName())
                .orElseThrow(() -> new IllegalArgumentException("지갑이 존재하지 않습니다."));

        if (coinHolding.getAvailableAmount().compareTo(orderRequest.getCoinAmount()) < 0) {
            throw new IllegalArgumentException("판매할 코인이 부족합니다.");
        }

        coinHolding.setAvailableAmount(coinHolding.getAvailableAmount().subtract(orderRequest.getCoinAmount()));
        masterCoinHoldingRepository.save(coinHolding);
    }
}
