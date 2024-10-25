package com.mjy.coin.service;

import com.mjy.coin.repository.coin.slave.SlaveChartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ChartServiceTest {

    @Mock
    private SlaveChartRepository slaveChartRepository;

    @InjectMocks
    private ChartService chartService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetChartData() {
//        // Given
//        ChartDataRequest chartDataRequest = new ChartDataRequest();
//        chartDataRequest.setSymbol("BTC_KRW");
//        chartDataRequest.setFrom(1630454400L);  // Example timestamp
//        chartDataRequest.setTo(1630540800L);    // Example timestamp
//        chartDataRequest.setInterval("1");
//
//        CandleDTO[] candleData = new CandleDTO[]{
//                new CandleDTO(1.0, 2.0, 3.0, 0.5, 100.0, 1630454400L),
//                new CandleDTO(1.2, 2.1, 3.2, 0.7, 110.0, 1630458000L)
//        };
//
//        // When (Mocking the repository call)
//        when(slaveChartRepository.getCandleData(anyString(), anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(Arrays.asList(candleData));
//
//        // When (Executing the service method)
//        List<CandleDTO[]> result = chartService.getChartData(chartDataRequest);
//
//        // Then (Verifying the result)
//        assertEquals(2, result.size());
//        assertEquals(1.0, result.get(0)[0].getOpenPrice());
//        assertEquals(2.0, result.get(0)[0].getClosePrice());
//        assertEquals(1630454400L, result.get(0)[0].getTimestamp());
    }
}