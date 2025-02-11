package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.UUID;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceV1MockTest {
    @Mock
    private OrderBookService orderBookService;
    @Mock
    private OrderQueueService orderQueueService;
    @Mock
    private MasterCoinOrderRepository masterCoinOrderRepository;
    @InjectMocks
    private MatchingServiceV1 matchingService;

    String symbol;

    private CoinOrderDTO createOrder(OrderType type, String price, String quantity) {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setCoinName("BTC");
        order.setMarketName("KRW");
        order.setFee(new BigDecimal("0.1"));
        order.setOrderType(type);
        order.setOrderPrice(new BigDecimal(price));
        order.setQuantity(new BigDecimal(quantity));
        order.setOrderStatus(PENDING);
        order.setMemberIdx(1L);
        order.setMemberUuid(String.valueOf(UUID.randomUUID()));
        order.setUuid(String.valueOf(UUID.randomUUID()));
        return order;
    }

    @BeforeEach
    public void setup() {
        symbol = "BTC-KRW";
    }

    @Test
    @DisplayName("반대 주문 큐 가져오기")
    public void testGetOppositeOrdersQueue() {
        //given
        Comparator<CoinOrderDTO> buyOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        Comparator<CoinOrderDTO> sellOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        PriorityQueue<CoinOrderDTO> buyQueue = new PriorityQueue<>(buyOrderComparator);
        PriorityQueue<CoinOrderDTO> sellQueue = new PriorityQueue<>(sellOrderComparator);

        CoinOrderDTO buyOrder = createOrder(BUY, "100", "1.5");
        CoinOrderDTO sellOrder = createOrder(SELL, "90", "1.5");

        buyQueue.add(buyOrder);
        sellQueue.add(sellOrder);

        //when
        when(orderQueueService.getSellOrderQueue(symbol)).thenReturn(sellQueue);
        when(orderQueueService.getBuyOrderQueue(symbol)).thenReturn(buyQueue);

        PriorityQueue<CoinOrderDTO> resultForBuy = matchingService.getOppositeOrdersQueue(buyOrder, symbol);
        PriorityQueue<CoinOrderDTO> resultForSell = matchingService.getOppositeOrdersQueue(sellOrder, symbol);

        //then
        assertEquals(sellQueue, resultForBuy, "BUY 주문은 SELL 큐를 반환");
        assertEquals(buyQueue, resultForSell, "SELL 주문은 BUY 큐를 반환");
    }

    @Test
    @DisplayName("체결 조건 확인")
    public void testCanMatchOrders() {
        // Case 1: BUY 타입, 가격 일치, 수량 > 0 => true 반환
        CoinOrderDTO buyOrder = createOrder(BUY, "100", "1.5");
        CoinOrderDTO sellOrder = createOrder(SELL, "90", "1.5");
        assertTrue(matchingService.canMatchOrders(buyOrder, sellOrder), "BUY 주문이 SELL 주문과 가격 조건을 만족해야 함");

        // Case 2: SELL 타입, 가격 일치, 수량 > 0 => true 반환
        CoinOrderDTO sellOrder2 = createOrder(SELL, "90", "1.5");
        CoinOrderDTO buyOrder2 = createOrder(BUY, "100", "1.5");
        assertTrue(matchingService.canMatchOrders(sellOrder2, buyOrder2), "SELL 주문이 BUY 주문과 가격 조건을 만족해야 함");

        // Case 3: BUY 타입, 가격 불일치 => false 반환
        CoinOrderDTO buyOrderFail = createOrder(BUY, "80", "1.5");
        CoinOrderDTO sellOrderFail = createOrder(SELL, "90", "1.5");
        assertFalse(matchingService.canMatchOrders(buyOrderFail, sellOrderFail),
                "BUY 주문이 SELL 주문의 가격 조건을 만족하지 않아야 함");

        // Case 4: SELL 타입, 가격 불일치 => false 반환
        CoinOrderDTO sellOrderFail2 = createOrder(SELL, "110", "1.5");
        CoinOrderDTO buyOrderFail2 = createOrder(BUY, "100", "1.5");
        assertFalse(matchingService.canMatchOrders(sellOrderFail2, buyOrderFail2),
                "SELL 주문이 BUY 주문의 가격 조건을 만족하지 않아야 함");

        // Case 5: 주문 수량이 0 => false 반환
        CoinOrderDTO zeroAmountOrder = createOrder(BUY, "100", "0");
        CoinOrderDTO validOppositeOrder = createOrder(SELL, "90", "1.5");
        assertFalse(matchingService.canMatchOrders(zeroAmountOrder, validOppositeOrder),
                "수량이 0인 경우 false 반환");

        // Case 6: 수량 < 0 => false 반환
        CoinOrderDTO negativeAmountOrder = createOrder(BUY, "100", "-1.5");
        assertFalse(matchingService.canMatchOrders(negativeAmountOrder, validOppositeOrder),
                "수량이 음수인 경우 false 반환");
    }

    @Test
    @DisplayName("나머지 수량 계산")
    public void testCalculateRemainingQuantity() {
        // given
        CoinOrderDTO order = createOrder(BUY, "110", "1.5");
        CoinOrderDTO oppositeOrder = createOrder(BUY, "110", "1.5");

        // when
        BigDecimal remaining = matchingService.calculateRemainingQuantity(order, oppositeOrder);

        // then
        assertEquals(0, remaining.compareTo(BigDecimal.ZERO), "같은 수량일때 나머지 수량은 0이 되어야한다.");
    }

    @Test
    @DisplayName("체결가 반환")
    public void testGetExecutionPricey() {
        // given
        CoinOrderDTO oppositeOrder = createOrder(BUY, "110", "1.5");

        // when
        BigDecimal executionPrice = matchingService.getExecutionPrice(oppositeOrder);

        // then
        assertEquals(executionPrice, oppositeOrder.getOrderPrice());
    }

    @Test
    @DisplayName("미체결 주문인지 체크")
    public void testProcessNonMatchedOrder() {
        // given
        CoinOrderDTO order = createOrder(BUY, "110", "1.5");

        // when
        matchingService.processNonMatchedOrder(symbol, order);

        // then
        verify(masterCoinOrderRepository, times(1)).save(any(CoinOrder.class));
        verify(orderQueueService, times(1)).addBuyOrder(symbol, order);
        verify(orderBookService, times(1)).updateOrderBook(symbol, order, true, true);
    }

    @Test
    @DisplayName("체결 상태 변경")
    public void testUpdateOrderStatus() {
        //given
        CoinOrderDTO order = createOrder(BUY, "100", "1.5");
        CoinOrderDTO oppositeOrder = createOrder(SELL, "90", "1.5");

        BigDecimal executionPrice = new BigDecimal("100.0");
        BigDecimal quantity = new BigDecimal("0.1");

        // when
        matchingService.updateOrderStatus(order, oppositeOrder, executionPrice, COMPLETED, order.getIdx(), quantity);

        // then
        assertEquals(COMPLETED, order.getOrderStatus(), "주문 상태가 COMPLETED여야 한다.");
        assertNotNull(order.getMatchedAt(), "매칭 시간이 존재해야 한다.");
        assertEquals(executionPrice, order.getExecutionPrice(), "매치 가격이 같아야 한다.");
        assertEquals(order.getIdx() + "|" + oppositeOrder.getIdx(), order.getMatchIdx(), "매치 인덱스가 일치해야 한다.");
    }

    @Test
    @DisplayName("주문 수량이 완전히 일치하는가?")
    public void testIsCompleteMatch() {
        // given
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal nonZeroQuantity = new BigDecimal("1.5");

        // when & then
        assertTrue(matchingService.isCompleteMatch(zeroQuantity), "남은 수량이 0인 경우 true 반환");
        assertFalse(matchingService.isCompleteMatch(nonZeroQuantity), "남은 수량이 양수인 경우 false 반환");
    }

    @Test
    @DisplayName("주문 수량이 더 많은가?")
    public void testIsOversizeMatch() {
        // given
        BigDecimal positiveQuantity = new BigDecimal("1.5");
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal negativeQuantity = new BigDecimal("-1.5");

        // when & then
        assertTrue(matchingService.isOversizeMatch(positiveQuantity), "남은 수량이 양수인 경우 true 반환");
        assertFalse(matchingService.isOversizeMatch(zeroQuantity), "남은 수량이 0인 경우 false 반환");
        assertFalse(matchingService.isOversizeMatch(negativeQuantity), "남은 수량이 음수인 경우 false 반환");
    }

    @Test
    @DisplayName("주문 수량이 더 적은가?")
    public void testIsUndersizedMatch() {
        // given
        BigDecimal negativeQuantity = new BigDecimal("-1.5");
        BigDecimal zeroQuantity = BigDecimal.ZERO;
        BigDecimal positiveQuantity = new BigDecimal("1.5");

        // when & then
        assertTrue(matchingService.isUndersizedMatch(negativeQuantity), "남은 수량이 음수인 경우 true 반환");
        assertFalse(matchingService.isUndersizedMatch(zeroQuantity), "남은 수량이 0인 경우 false 반환");
        assertFalse(matchingService.isUndersizedMatch(positiveQuantity), "남은 수량이 양수인 경우 false 반환");
    }
}