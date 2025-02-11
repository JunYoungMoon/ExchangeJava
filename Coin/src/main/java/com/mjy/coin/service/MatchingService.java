package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;

public interface MatchingService {
    public void matchOrders(String key, CoinOrderDTO order);
}
