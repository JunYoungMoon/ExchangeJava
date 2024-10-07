package com.mjy.coin.api;

import com.mjy.coin.dto.ApiResponse;
import com.mjy.coin.dto.ChartDataRequest;
import com.mjy.coin.service.ChartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChartController {

    // 차트 데이터를 가져오는 서비스 (의존성 주입이 필요할 경우 사용)
    private final ChartService chartService;

    public ChartController(ChartService chartService) {
        this.chartService = chartService;
    }

    @GetMapping("/chart")
    public ApiResponse getChartData(ChartDataRequest chartDataRequest) {
        // 받은 파라미터를 사용하여 차트 데이터를 가져옴

        chartService.getChartData(chartDataRequest);

        return ApiResponse.builder()
                .status("success")
                .msg("msg")
                .data("data")
                .build();
    }
}