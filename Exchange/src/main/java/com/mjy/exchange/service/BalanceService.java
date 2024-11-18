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
    private final OrderProcessorFactory orderProcessorFactory;

    public BalanceService(OrderProcessorFactory orderProcessorFactory) {
        this.orderProcessorFactory = orderProcessorFactory;
    }

    public void checkAndUpdateBalance(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo) {
        OrderProcessor processor = orderProcessorFactory.getProcessor(orderRequest.getOrderType());
        processor.process(memberUuid, orderRequest, coinInfo);
    }
}
