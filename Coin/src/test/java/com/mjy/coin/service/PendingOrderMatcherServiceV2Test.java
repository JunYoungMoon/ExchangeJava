package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.mjy.coin.enums.OrderStatus.*;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PendingOrderMatcherServiceV2Test {

    private CoinOrderDTO order;
    private CoinOrderDTO oppositeOrder;

    @BeforeEach
    void setUp() {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setUuid(generateUniqueKey("Order"));
        order.setCoinAmount(new BigDecimal("1.5"));
        order.setOrderStatus(PENDING);

        CoinOrderDTO oppositeOrder = new CoinOrderDTO();
        oppositeOrder.setUuid(generateUniqueKey("Order"));
        oppositeOrder.setCoinAmount(new BigDecimal("1.5"));
        oppositeOrder.setOrderStatus(PENDING);

        this.order = order;
        this.oppositeOrder = oppositeOrder;
    }

    private BigDecimal calculateRemainingQuantity(CoinOrderDTO order, CoinOrderDTO oppositeOrder) {
        return order.getCoinAmount().subtract(oppositeOrder.getCoinAmount());
    }

    @Test
    public void testCalculateRemainingQuantity() {
        BigDecimal remaining = calculateRemainingQuantity(order, oppositeOrder);

        assertEquals(0, remaining.compareTo(BigDecimal.ZERO));
    }
}