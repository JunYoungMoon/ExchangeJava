package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.stereotype.Service;

@Service
public class MarketOrderService implements OrderService {

    @Override
    public void processOrder(CoinOrderDTO order) {
        // 시장가 주문 처리 로직
    }
}
