package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.entity.coin.CoinOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CompletedOrderProcessorServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private MasterCoinOrderRepository masterCoinOrderRepository;

    @Mock
    private SlaveCoinOrderRepository slaveCoinOrderRepository;

    @Mock
    private KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate;

    @InjectMocks
    private CompletedOrderProcessorService completedOrderProcessorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCompletedProcessorOrder_Success() {
        // given
        CoinOrderDTO order1 = new CoinOrderDTO();
        order1.setUuid("uuid1");
        order1.setCoinName("BTC");
        order1.setMarketName("KRW");

        CoinOrderDTO order2 = new CoinOrderDTO();
        order2.setUuid("uuid2");
        order2.setCoinName("ETH");
        order2.setMarketName("KRW");

        List<CoinOrderDTO> orders = List.of(order1, order2);

        when(slaveCoinOrderRepository.findAllByUuidIn(any())).thenReturn(Collections.emptyList());

        // When
        completedOrderProcessorService.completedProcessorOrder(orders);

        // Then
        verify(masterCoinOrderRepository).saveAll(any());
        verify(redisService, times(2)).deleteHashOps(anyString(), anyString());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    public void testCompletedProcessorOrder_WithExistingOrders() {
        // Given
        CoinOrderDTO order1 = new CoinOrderDTO();
        order1.setUuid("uuid1");
        order1.setCoinName("BTC");
        order1.setMarketName("KRW");

        List<CoinOrderDTO> orders = List.of(order1);

        CoinOrder existingOrder = new CoinOrder();

        existingOrder.setUuid("uuid1");
        existingOrder.setCoinName("BTC");
        existingOrder.setMarketName("KRW");

        when(slaveCoinOrderRepository.findAllByUuidIn(any())).thenReturn(List.of(existingOrder));

        // When
        completedOrderProcessorService.completedProcessorOrder(orders);

        // Then
        verify(masterCoinOrderRepository, never()).saveAll(any());
        verify(redisService, never()).deleteHashOps(anyString(), anyString());
    }


    @Test
    public void testCompletedProcessorOrder_Failure() {
        // Given
        CoinOrderDTO order1 = new CoinOrderDTO();
        order1.setUuid("uuid1");
        order1.setCoinName("BTC");
        order1.setMarketName("KRW");

        CoinOrderDTO order2 = new CoinOrderDTO();
        order2.setUuid("uuid2");
        order2.setCoinName("ETH");
        order2.setMarketName("KRW");

        List<CoinOrderDTO> orders = List.of(order1, order2);

        when(slaveCoinOrderRepository.findAllByUuidIn(any())).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("Database error")).when(masterCoinOrderRepository).saveAll(any());

        // When
        completedOrderProcessorService.completedProcessorOrder(orders);

        // Then
        verify(kafkaTemplate).send(eq("Order-Completed"), anyString(), any());
    }

}