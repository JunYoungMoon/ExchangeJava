package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.util.CommonUtil.generateUniqueKey;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test") // test 프로파일을 활성화
class LimitOrderServiceTest {
    @Autowired
    private LimitOrderService limitOrderService;

    @MockBean
    private MatchingServiceV1 priorityQueueManager;

    @MockBean
    private OrderBookService orderBookService;

    @MockBean
    private OrderQueueService orderQueueService;

    @MockBean
    private MasterCoinOrderRepository masterCoinOrderRepository;

    @MockBean
    private RedisService redisService;

    private CoinOrderDTO order;

    @BeforeEach
    void setUp() {
        order = new CoinOrderDTO();
        order.setCoinName("BTC");
        order.setMarketName("KRW");
        order.setOrderType(OrderType.BUY);
        order.setMemberIdx(123L);
    }

    @Test
    void testProcessOrder_NewOrder() {
        // Redis에 기존 주문이 없는 상황을 설정
        Mockito.when(redisService.getHashOps(anyString(), anyString())).thenReturn("");

        // 메서드 실행
        limitOrderService.processOrder(order);

        // Redis에 새로운 주문이 추가되었는지 검증
        verify(redisService, times(1)).insertOrderInRedis(anyString(), any(), eq(order));

        // 매수 주문이 추가되고 주문서가 업데이트되는지 검증
        verify(orderQueueService, times(1)).addBuyOrder(anyString(), eq(order));
        verify(orderBookService, times(1)).updateOrderBook(anyString(), eq(order), eq(true), eq(true));

        // 체결 시도가 이루어졌는지 검증
        verify(priorityQueueManager, times(1)).matchOrders("BTC-KRW", order);
    }

    @Test
    void testProcessOrder_ExistingOrder() {
        // Redis에 기존 주문이 있는 상황을 설정
        Mockito.when(redisService.getHashOps(anyString(), anyString())).thenReturn("existingOrder");

        // 메서드 실행
        limitOrderService.processOrder(order);

        // Redis에 새로운 주문이 추가되지 않았는지 검증
        verify(redisService, times(0)).insertOrderInRedis(anyString(), any(), eq(order));

        // 매수 주문 및 주문서 업데이트가 실행되지 않았는지 검증
        verify(orderQueueService, times(0)).addBuyOrder(anyString(), eq(order));
        verify(orderBookService, times(0)).updateOrderBook(anyString(), eq(order), eq(true), eq(true));

        // 체결 시도도 이루어지지 않았는지 검증
        verify(priorityQueueManager, times(0)).matchOrders("BTC-KRW",order);
    }

    @Test
    void testProcessOrder_SellOrder() {
        // Redis에 기존 주문이 없는 상황을 설정
        Mockito.when(redisService.getHashOps(anyString(), anyString())).thenReturn("");
        order.setOrderType(OrderType.SELL); // 매도 주문으로 설정

        // 메서드 실행
        limitOrderService.processOrder(order);

        // Redis에 새로운 주문이 추가되었는지 검증
        verify(redisService, times(1)).insertOrderInRedis(anyString(), any(), eq(order));

        // 매도 주문이 추가되고 주문서가 업데이트되는지 검증
        verify(orderQueueService, times(1)).addSellOrder(anyString(), eq(order));
        verify(orderBookService, times(1)).updateOrderBook(anyString(), eq(order), eq(false), eq(true));

        // 체결 시도가 이루어졌는지 검증
        verify(priorityQueueManager, times(1)).matchOrders("BTC-KRW", order);
    }

    private CoinOrderDTO createOrder(OrderType type, String price, String amount) {
        CoinOrderDTO order = new CoinOrderDTO();
        order.setOrderType(type);
        order.setOrderPrice(new BigDecimal(price));
        order.setQuantity(new BigDecimal(amount));
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderStatus(PENDING);
        return order;
    }

    @Test
    public void testGenerateUniqueKey() throws Exception {
        CoinOrderDTO order = createOrder(BUY, "110", "1.5");

        System.out.println(generateUniqueKey(order));
    }
}