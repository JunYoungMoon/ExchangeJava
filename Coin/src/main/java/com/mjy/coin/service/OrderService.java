package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;

public interface OrderService {
    void processOrder(CoinOrderDTO order);
}
