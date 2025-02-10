package com.mjy.exchange.service;

import com.mjy.exchange.dto.CoinOrder;
import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mjy.exchange.enums.OrderStatus.PENDING;

public class CoinOrderFactory {

    public static CoinOrder createCoinOrder(OrderRequest orderRequest, CoinInfo coinInfo, String memberUuid) {
        CoinOrder coinOrder = new CoinOrder();
        coinOrder.setMemberUuid(memberUuid);
        coinOrder.setMarketName(orderRequest.getMarketName());
        coinOrder.setCoinName(orderRequest.getCoinName());
        coinOrder.setQuantity(new BigDecimal(String.valueOf(orderRequest.getQuantity())));
        coinOrder.setOrderPrice(new BigDecimal(String.valueOf(orderRequest.getOrderPrice())));
        coinOrder.setOrderType(orderRequest.getOrderType());
        coinOrder.setOrderStatus(PENDING);
        coinOrder.setFee(new BigDecimal(String.valueOf(coinInfo.getFeeRate())));
        coinOrder.setCreatedAt(LocalDateTime.now());
        return coinOrder;
    }
}
