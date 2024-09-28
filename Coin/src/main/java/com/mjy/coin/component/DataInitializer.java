package com.mjy.coin.component;

import com.mjy.coin.service.CoinOrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CoinOrderService coinOrderService;

    public DataInitializer(CoinOrderService coinOrderService) {
        this.coinOrderService = coinOrderService;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1천만 건의 주문 데이터를 생성
//        coinOrderService.generateCoinOrders(10000000);
    }
}