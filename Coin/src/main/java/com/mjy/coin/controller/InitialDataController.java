package com.mjy.coin.controller;

import com.mjy.coin.dto.*;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.service.ChartService;
import com.mjy.coin.service.CoinOrderService;
import com.mjy.coin.service.OrderBookService;
import com.mjy.coin.service.LimitOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
public class InitialDataController {

    private final LimitOrderService limitOrderService;
    private final ChartService chartService;
    private final CoinOrderService coinOrderService;
    private final OrderBookService orderBookService;

    public InitialDataController(ChartService chartService, CoinOrderService coinOrderService, OrderBookService orderBookService
    , LimitOrderService limitOrderService) {
        this.chartService = chartService;
        this.coinOrderService = coinOrderService;
        this.orderBookService = orderBookService;
        this.limitOrderService = limitOrderService;
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

//    @GetMapping("/test")
//    public ApiResponse test() {
//        LocalDate today = LocalDate.of(2024, 10, 16);
//
//        Long[] result = coinOrderService.getMinMaxIdx(today);
//
//        return ApiResponse.builder()
//                .status("success")
//                .msg("msg")
////                .data(chartService.getChartData(chartDataRequest))
//                .data("data")
//                .build();
//    }


    @PostMapping("/test")
    public ApiResponse test() {
        Random random = new Random();

        CoinOrderDTO coinOrderDto = new CoinOrderDTO();

        boolean nextBoolean = random.nextBoolean();
        coinOrderDto.setMemberIdx(nextBoolean ? 1L : 2L);
        coinOrderDto.setMemberUuid(nextBoolean ? "2b005552-ee2b-4851-8857-6e595800395d" : "cfccbb28-f07d-4e7c-8bd2-4cbd720aceab");
        coinOrderDto.setMarketName("KRW");

//            String randomCoinName = random.nextBoolean() ? "BTC" : "ETH";
        String randomCoinName = "BTC";

        coinOrderDto.setCoinName(randomCoinName);

        // 0.01 ~ 0.1 범위의 랜덤 금액
        BigDecimal randomAmount = new BigDecimal(0.01 + (0.09 * random.nextDouble())).setScale(2, RoundingMode.DOWN);
        coinOrderDto.setCoinAmount(new BigDecimal(String.valueOf(randomAmount)));

        // 5000 ~ 6000 범위에서 100원 단위로 랜덤 가격 생성
        int randomPrice = 5000 + (random.nextInt(11) * 100); // 5000에서 6000까지 100원 단위 (5000 + 100*0~10)
        coinOrderDto.setOrderPrice(new BigDecimal(String.valueOf(randomPrice)));

        // BUY 또는 SELL 중 랜덤 타입 선택
        OrderType randomOrderType = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;
        coinOrderDto.setOrderType(randomOrderType);

        // 주문 상태는 PENDING으로 고정
        coinOrderDto.setOrderStatus(OrderStatus.PENDING);

        // 수수료는 0.01로 고정
        coinOrderDto.setFee(new BigDecimal("0.01"));

        // 생성 시간 설정
        coinOrderDto.setCreatedAt(LocalDateTime.now());

        limitOrderService.processOrder(coinOrderDto);

        return ApiResponse.builder()
                .status("success")
                .msg("msg")
//                .data(chartService.getChartData(chartDataRequest))
                .data("data")
                .build();
    }
}
