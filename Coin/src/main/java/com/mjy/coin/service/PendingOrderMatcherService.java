package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;

public interface PendingOrderMatcherService {
    public void matchOrders(CoinOrderDTO order);
}
