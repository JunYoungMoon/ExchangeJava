package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.mjy.coin.enums.OrderStatus.*;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PendingOrderMatcherServiceV2Test {

    @InjectMocks
    private PendingOrderMatcherServiceV2 pendingOrderMatcherService;

    @Test
    public void testCalculateRemainingQuantity() {
        // given
        CoinOrderDTO order = new CoinOrderDTO();
        order.setUuid(generateUniqueKey("Order"));
        order.setCoinAmount(new BigDecimal("1.5"));
        order.setOrderStatus(PENDING);

        CoinOrderDTO oppositeOrder = new CoinOrderDTO();
        oppositeOrder.setUuid(generateUniqueKey("Order"));
        oppositeOrder.setCoinAmount(new BigDecimal("1.5"));
        oppositeOrder.setOrderStatus(PENDING);

        // when
        BigDecimal remaining = pendingOrderMatcherService.calculateRemainingQuantity(order, oppositeOrder);

        // then
        assertEquals(0, remaining.compareTo(BigDecimal.ZERO));
    }

    @Test
    public void testIsCompleteMatch() {
        // given
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal nonZeroQuantity = new BigDecimal("1.5");

        // when & then
        assertTrue(pendingOrderMatcherService.isCompleteMatch(zeroQuantity), "남은 수량이 0인 경우 true 반환");
        assertFalse(pendingOrderMatcherService.isCompleteMatch(nonZeroQuantity), "남은 수량이 양수인 경우 false 반환");
    }


    @Test
    public void testIsOversizeMatch() {
        // given
        BigDecimal positiveQuantity = new BigDecimal("1.5");
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal negativeQuantity = new BigDecimal("-1.5");

        // when & then
        assertTrue(pendingOrderMatcherService.isOversizeMatch(positiveQuantity), "남은 수량이 양수인 경우 true 반환");
        assertFalse(pendingOrderMatcherService.isOversizeMatch(zeroQuantity), "남은 수량이 0인 경우 false 반환");
        assertFalse(pendingOrderMatcherService.isOversizeMatch(negativeQuantity), "남은 수량이 음수인 경우 false 반환");
    }

    @Test
    public void testIsUndersizedMatch() {
        // given
        BigDecimal negativeQuantity = new BigDecimal("-1.5");
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal positiveQuantity = new BigDecimal("1.5");

        // when & then
        assertTrue(pendingOrderMatcherService.isUndersizedMatch(negativeQuantity), "남은 수량이 음수인 경우 true 반환");
        assertFalse(pendingOrderMatcherService.isUndersizedMatch(zeroQuantity), "남은 수량이 0인 경우 false 반환");
        assertFalse(pendingOrderMatcherService.isUndersizedMatch(positiveQuantity), "남은 수량이 양수인 경우 false 반환");
    }

    private CoinOrderDTO createOrder(OrderType type, String price, String amount) {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setUuid(generateUniqueKey("Order"));
        order.setOrderType(type);
        order.setOrderPrice(new BigDecimal(price));
        order.setCoinAmount(new BigDecimal(amount));
        order.setOrderStatus(PENDING);
        return order;
    }

    @Test
    public void testCanMatchOrders(){
        // Case 1: BUY 타입, 가격 일치, 수량 > 0 => true 반환
        CoinOrderDTO buyOrder = createOrder(BUY, "100", "1.5");
        CoinOrderDTO sellOrder = createOrder(SELL, "90", "1.5");
        assertTrue(pendingOrderMatcherService.canMatchOrders(buyOrder, sellOrder), "BUY 주문이 SELL 주문과 가격 조건을 만족해야 함");

        // Case 2: SELL 타입, 가격 일치, 수량 > 0 => true 반환
        CoinOrderDTO sellOrder2 = createOrder(SELL, "90", "1.5");
        CoinOrderDTO buyOrder2 = createOrder(BUY, "100", "1.5");
        assertTrue(pendingOrderMatcherService.canMatchOrders(sellOrder2, buyOrder2), "SELL 주문이 BUY 주문과 가격 조건을 만족해야 함");

        // Case 3: BUY 타입, 가격 불일치 => false 반환
        CoinOrderDTO buyOrderFail = createOrder(BUY, "80", "1.5");
        CoinOrderDTO sellOrderFail = createOrder(SELL, "90", "1.5");
        assertFalse(pendingOrderMatcherService.canMatchOrders(buyOrderFail, sellOrderFail), "BUY 주문이 SELL 주문의 가격 조건을 만족하지 않아야 함");

        // Case 4: SELL 타입, 가격 불일치 => false 반환
        CoinOrderDTO sellOrderFail2 = createOrder(SELL, "110", "1.5");
        CoinOrderDTO buyOrderFail2 = createOrder(BUY, "100", "1.5");
        assertFalse(pendingOrderMatcherService.canMatchOrders(sellOrderFail2, buyOrderFail2), "SELL 주문이 BUY 주문의 가격 조건을 만족하지 않아야 함");

        // Case 5: 주문 수량이 0 => false 반환
        CoinOrderDTO zeroAmountOrder = createOrder(BUY, "100", "0");
        CoinOrderDTO validOppositeOrder = createOrder(SELL, "90", "1.5");
        assertFalse(pendingOrderMatcherService.canMatchOrders(zeroAmountOrder, validOppositeOrder), "수량이 0인 경우 false 반환");

        // Case 6: 수량 < 0 => false 반환
        CoinOrderDTO negativeAmountOrder = createOrder(BUY, "100", "-1.5");
        assertFalse(pendingOrderMatcherService.canMatchOrders(negativeAmountOrder, validOppositeOrder), "수량이 음수인 경우 false 반환");
    }
}