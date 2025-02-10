package com.mjy.exchange.service;

import com.mjy.exchange.dto.CoinOrder;
import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.CoinInfo;
import com.mjy.exchange.enums.OrderType;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
import com.mjy.exchange.repository.slave.SlaveCoinInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class OrderServiceTest {

    @Mock
    private MasterCoinHoldingRepository masterCoinHoldingRepository;

    @Mock
    private SlaveCoinHoldingRepository slaveCoinHoldingRepository;

    @Mock
    private SlaveCoinInfoRepository slaveCoinInfoRepository;

    @Mock
    private KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void processOrder_shouldThrowExceptionWhenNoCoinHolding() {
        // Given
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCoinName("BTC");
        orderRequest.setMarketName("KRW");
        orderRequest.setQuantity(BigDecimal.valueOf(1));
        orderRequest.setOrderPrice(BigDecimal.valueOf(5000000));
        orderRequest.setOrderType(OrderType.BUY);

        String memberUuid = "cfccbb28-f07d-4e7c-8bd2-4cbd720aceab";

        when(slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, "BTC")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.processOrder(orderRequest, memberUuid);
        });

        assertEquals("지갑이 생성되지 않았습니다.", exception.getMessage());
    }

    @Test
    void processOrder_shouldThrowExceptionWhenInsufficientBalance() {
        // Given
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCoinName("BTC");
        orderRequest.setMarketName("KRW");
        orderRequest.setQuantity(BigDecimal.valueOf(1));
        orderRequest.setOrderPrice(BigDecimal.valueOf(5000000));
        orderRequest.setOrderType(OrderType.BUY);

        String memberUuid = "cfccbb28-f07d-4e7c-8bd2-4cbd720aceab";

        CoinHolding coinHolding = new CoinHolding();
        coinHolding.setAvailableAmount(BigDecimal.valueOf(1000000)); // 부족한 잔액

        when(slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, "BTC")).thenReturn(Optional.of(coinHolding));

        CoinInfo coinInfo = new CoinInfo();
        coinInfo.setFeeRate(BigDecimal.valueOf(0.001)); // 0.1% fee
        when(slaveCoinInfoRepository.findByMarketNameAndCoinName("KRW", "BTC")).thenReturn(Optional.of(coinInfo));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.processOrder(orderRequest, memberUuid);
        });

        assertEquals("잔액이 부족합니다.", exception.getMessage());
    }

    @Test
    void processOrder_shouldProcessOrderSuccessfully() {
        // Given
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCoinName("BTC");
        orderRequest.setMarketName("KRW");
        orderRequest.setQuantity(BigDecimal.valueOf(1));
        orderRequest.setOrderPrice(BigDecimal.valueOf(5000000));
        orderRequest.setOrderType(OrderType.BUY);

        String memberUuid = "cfccbb28-f07d-4e7c-8bd2-4cbd720aceab";

        CoinHolding coinHolding = new CoinHolding();
        coinHolding.setAvailableAmount(BigDecimal.valueOf(10000000)); // 충분한 잔액

        when(slaveCoinHoldingRepository.findByMemberUuidAndCoinType(memberUuid, "BTC")).thenReturn(Optional.of(coinHolding));

        CoinInfo coinInfo = new CoinInfo();
        coinInfo.setFeeRate(BigDecimal.valueOf(0.001)); // 0.1% fee
        when(slaveCoinInfoRepository.findByMarketNameAndCoinName("KRW", "BTC")).thenReturn(Optional.of(coinInfo));

        // When
        orderService.processOrder(orderRequest, memberUuid);

        // Then
        verify(masterCoinHoldingRepository, times(1)).save(any(CoinHolding.class));
        verify(coinOrderKafkaTemplate, times(1)).send(anyString(), any(CoinOrder.class));
    }


}