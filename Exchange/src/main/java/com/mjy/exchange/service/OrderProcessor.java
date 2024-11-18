package com.mjy.exchange.service;

import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinInfo;

public interface OrderProcessor {
    String getOrderType();
    void process(String memberUuid, OrderRequest orderRequest, CoinInfo coinInfo);
}
