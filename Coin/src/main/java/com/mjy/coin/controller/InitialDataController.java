package com.mjy.coin.controller;

import com.mjy.coin.dto.ApiResponse;
import com.mjy.coin.dto.CandleDTO;
import com.mjy.coin.dto.ChartDataRequest;
import com.mjy.coin.dto.OrderBookDataRequest;
import com.mjy.coin.service.ChartService;
import com.mjy.coin.service.CoinOrderService;
import com.mjy.coin.service.OrderBookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class InitialDataController {

    private final ChartService chartService;
    private final CoinOrderService coinOrderService;
    private final OrderBookService orderBookService;

    public InitialDataController(ChartService chartService, CoinOrderService coinOrderService, OrderBookService orderBookService) {
        this.chartService = chartService;
        this.coinOrderService = coinOrderService;
        this.orderBookService = orderBookService;
    }

    @GetMapping("/chart")
    public ApiResponse getChartData(@Valid ChartDataRequest chartDataRequest) {
        List<CandleDTO> chartData = chartService.getChartData(chartDataRequest);

        return ApiResponse.builder()
                .status("success")
                .msg("msg")
                .data(chartData)
                .build();
    }

    @GetMapping("/orderBook")
    public ApiResponse getOrderBookData(@Valid OrderBookDataRequest orderBookDataRequest) {
        Map<String, Map<BigDecimal, BigDecimal>> orderBookData = orderBookService.getTopNOrders(orderBookDataRequest.getSymbol(), 10);

        return ApiResponse.builder()
                .status("success")
                .data(orderBookData)
                .build();
    }

    @GetMapping("/test")
    public ApiResponse test() {
        LocalDate today = LocalDate.of(2024, 10, 16);

        Long[] result = coinOrderService.getMinMaxIdx(today);

        return ApiResponse.builder()
                .status("success")
                .msg("msg")
//                .data(chartService.getChartData(chartDataRequest))
                .data("data")
                .build();
    }
}
