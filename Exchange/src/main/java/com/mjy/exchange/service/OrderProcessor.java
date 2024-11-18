package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.enums.OrderType;

public interface OrderProcessor {
    OrderType getOrderType();
    void process(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo);
}
