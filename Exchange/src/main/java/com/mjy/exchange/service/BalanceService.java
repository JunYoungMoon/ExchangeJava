package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinInfo;
import org.springframework.stereotype.Service;

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
