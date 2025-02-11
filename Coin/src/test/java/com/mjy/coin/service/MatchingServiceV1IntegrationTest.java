package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class MatchingServiceV1IntegrationTest {

    @MockBean(name = "matchListKafkaTemplate")
    private KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;
    @MockBean(name = "priceVolumeMapKafkaTemplate")
    private KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    @Autowired
    private RedisService redisService;
    @Autowired
    private OrderBookService orderBookService;
    @Autowired
    private OrderQueueService orderQueueService;
    @Autowired
    private MatchingServiceV1 matchingService;

    String symbol;

    @BeforeEach
    public void setup() {
        symbol = "BTC-KRW";

        orderQueueService.initializeBuyOrder(symbol);
        orderQueueService.initializeSellOrder(symbol);
        orderBookService.initializeSellOrderBook(symbol);
        orderBookService.initializeBuyOrderBook(symbol);
    }

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

    @Test
    @DisplayName("미체결 동작 확인")
    public void testPendingOrderCheck() {
        //given
        CoinOrderDTO order = createOrder(BUY, "100", "1.5");

        //when & then
        matchingService.matchOrders(symbol,order);
    }

    @Test
    @DisplayName("완전 체결 : 나의 주문 수량이 반대 주문 수량과 완전 일치")
    public void testCompleteMatch() {
        //given
        CoinOrderDTO oppositeOrder = createOrder(SELL, "100", "1.5");
        CoinOrderDTO order = createOrder(BUY, "100", "1.5");

        //when & then
        matchingService.matchOrders(symbol,oppositeOrder);
        matchingService.matchOrders(symbol,order);
    }

    @Test
    @DisplayName("부분 체결1 : 나의 주문 수량이 반대 주문 수량보다 클때")
    public void testOversizeMatch() {
        //given
        testPendingOrderCheck();
        testPendingOrderCheck();
        CoinOrderDTO order = createOrder(SELL, "100", "1.8");

        //when & then
        matchingService.matchOrders(symbol,order);
    }

    @Test
    @DisplayName("부분 체결2 : 나의 주문 수량이 반대 주문 수량보다 작을때")
    public void testUndersizedMatch() {
        //given
        testPendingOrderCheck();
        CoinOrderDTO order = createOrder(BUY, "100", "1.0");

        //when & then
        matchingService.matchOrders(symbol,order);
    }
}