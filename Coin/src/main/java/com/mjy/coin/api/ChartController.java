package com.mjy.coin.api;

import com.mjy.coin.dto.ApiResponse;
import com.mjy.coin.dto.ChartDataRequest;
import com.mjy.coin.service.ChartService;
import com.mjy.coin.service.CoinOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class ChartController {

    // 차트 데이터를 가져오는 서비스 (의존성 주입이 필요할 경우 사용)
    private final ChartService chartService;
    private final CoinOrderService coinOrderService;

    public ChartController(ChartService chartService, CoinOrderService coinOrderService) {
        this.chartService = chartService;
        this.coinOrderService = coinOrderService;
    }

    @GetMapping("/chart")
    public ApiResponse getChartData(ChartDataRequest chartDataRequest) {

        return ApiResponse.builder()
                .status("success")
                .msg("msg")
//                .data(chartService.getChartData(chartDataRequest))
                .data("data")
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