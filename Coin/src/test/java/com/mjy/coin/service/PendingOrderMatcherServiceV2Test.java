package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.mjy.coin.enums.OrderStatus.*;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PendingOrderMatcherServiceV2Test {

    @InjectMocks
    private PendingOrderMatcherServiceV2 pendingOrderMatcherService;

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

    @Test
    public void testCalculateRemainingQuantity() {
        BigDecimal remaining = pendingOrderMatcherService.calculateRemainingQuantity(order, oppositeOrder);

        assertEquals(0, remaining.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testIsCompleteMatch() {
    }

    @Test
    public void isOversizeMatch() {
    }

    @Test
    public void isUndersizedMatch() {
    }
}