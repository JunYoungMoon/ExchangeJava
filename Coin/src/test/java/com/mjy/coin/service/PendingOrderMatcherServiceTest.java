package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingOrderMatcherServiceTest {
    @Mock
    private OrderBookService orderBookService;

    @Mock
    private OrderService orderService;

    @Mock
    private RedisService redisService;

    @Mock
    private KafkaTemplate<String, Map<String, List<CoinOrderDTO>>> matchListKafkaTemplate;

    @Mock
    private KafkaTemplate<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaTemplate;

    @Mock
    private MasterCoinOrderRepository masterCoinOrderRepository;

    @InjectMocks
    private PendingOrderMatcherService pendingOrderMatcherService;

    private String testKey = "BTC-KRW";

    @Test
    public void testMatchOrders() {

        // 1. BuyOrders와 SellOrders 설정
        CoinOrderDTO buyOrder = new CoinOrderDTO();
        buyOrder.setCoinAmount(BigDecimal.valueOf(1));
        buyOrder.setOrderPrice(BigDecimal.valueOf(50000));
        buyOrder.setOrderStatus(OrderStatus.PENDING);
        buyOrder.setExecutionPrice(BigDecimal.valueOf(50000));

        CoinOrderDTO sellOrder = new CoinOrderDTO();
        sellOrder.setCoinAmount(BigDecimal.valueOf(1));
        sellOrder.setOrderPrice(BigDecimal.valueOf(50000));
        sellOrder.setOrderStatus(OrderStatus.PENDING);
        buyOrder.setExecutionPrice(BigDecimal.valueOf(50000));


        // Comparator 설정 (OrderService에서 사용한 것과 동일)
        Comparator<CoinOrderDTO> buyOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        Comparator<CoinOrderDTO> sellOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        // PriorityQueue 초기화
        PriorityQueue<CoinOrderDTO> buyOrders = new PriorityQueue<>(buyOrderComparator);
        PriorityQueue<CoinOrderDTO> sellOrders = new PriorityQueue<>(sellOrderComparator);

        // 주문 추가
        buyOrders.add(buyOrder);
        sellOrders.add(sellOrder);

        // Mockito에서 PriorityQueue 반환하도록 설정
        when(orderService.getBuyOrderQueue(testKey)).thenReturn(buyOrders);
        when(orderService.getSellOrderQueue(testKey)).thenReturn(sellOrders);

        // 2. 주문 매칭 실행
        pendingOrderMatcherService.matchOrders(testKey);

        // 3. 검증
        // 매칭된 주문이 COMPLETED 상태로 업데이트 되었는지 확인
        verify(redisService, times(2)).insertOrderInRedis(eq(testKey), eq(OrderStatus.COMPLETED), any(CoinOrderDTO.class));

        // 주문 상태 검증 (매수, 매도 모두 COMPLETED 상태로 처리되어야 함)
        assertEquals(OrderStatus.COMPLETED, buyOrder.getOrderStatus());
        assertEquals(OrderStatus.COMPLETED, sellOrder.getOrderStatus());

        verify(matchListKafkaTemplate, times(2)).send(anyString(), anyMap());
    }

    @Test
    public void testMatchOrders2_fullMatch() {
        // 1. BuyOrders와 SellOrders 설정
        CoinOrderDTO buyOrder = new CoinOrderDTO();
        buyOrder.setCoinAmount(BigDecimal.valueOf(0.2));
        buyOrder.setOrderPrice(BigDecimal.valueOf(50000));
        buyOrder.setCreatedAt(LocalDateTime.now());
        buyOrder.setOrderStatus(OrderStatus.PENDING);
        buyOrder.setOrderType(OrderType.BUY);
        buyOrder.setCoinName("BTC");
        buyOrder.setMarketName("KRW");

        CoinOrderDTO sellOrder = new CoinOrderDTO();
        sellOrder.setCoinAmount(BigDecimal.valueOf(1));
        sellOrder.setOrderPrice(BigDecimal.valueOf(50000));
        sellOrder.setCreatedAt(LocalDateTime.now());
        sellOrder.setOrderStatus(OrderStatus.PENDING);
        sellOrder.setOrderType(OrderType.SELL);
        sellOrder.setCoinName("BTC");
        sellOrder.setMarketName("KRW");


        // Comparator 설정 (OrderService에서 사용한 것과 동일)
        Comparator<CoinOrderDTO> buyOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice).reversed()
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        Comparator<CoinOrderDTO> sellOrderComparator =
                Comparator.comparing(CoinOrderDTO::getOrderPrice)
                        .thenComparing(CoinOrderDTO::getCreatedAt);

        // PriorityQueue 초기화
        PriorityQueue<CoinOrderDTO> buyOrders = new PriorityQueue<>(buyOrderComparator);
        PriorityQueue<CoinOrderDTO> sellOrders = new PriorityQueue<>(sellOrderComparator);

        // 주문 추가
        buyOrders.add(buyOrder);
        sellOrders.add(sellOrder);

        // Mockito에서 PriorityQueue 반환하도록 설정
        when(orderService.getBuyOrderQueue(buyOrder.getCoinName() + "-" + buyOrder.getMarketName())).thenReturn(buyOrders);
        when(orderService.getSellOrderQueue(sellOrder.getCoinName() + "-" + sellOrder.getMarketName())).thenReturn(sellOrders);

        // 2. 주문 매칭 실행
        pendingOrderMatcherService.matchOrders2(buyOrder);

        // 3. 검증
        // BuyOrder의 coinAmount가 0인지 확인
        assertEquals(BigDecimal.ZERO, buyOrder.getCoinAmount());

        // SellOrder의 coinAmount가 남은 금액만큼 감소했는지 확인
        assertEquals(BigDecimal.valueOf(0.8), sellOrder.getCoinAmount());

        // BuyOrder와 SellOrder의 상태가 COMPLETED로 변경되었는지 확인
        assertEquals(OrderStatus.COMPLETED, buyOrder.getOrderStatus());
        assertEquals(OrderStatus.COMPLETED, sellOrder.getOrderStatus());

        // 반대 주문 큐에서 주문이 poll 되었는지 확인
        verify(orderService).getBuyOrderQueue(buyOrder.getCoinName() + "-" + buyOrder.getMarketName());
        verify(orderService).getSellOrderQueue(sellOrder.getCoinName() + "-" + sellOrder.getMarketName());
    }
}