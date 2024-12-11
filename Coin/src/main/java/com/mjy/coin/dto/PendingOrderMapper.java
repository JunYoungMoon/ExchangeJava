package com.mjy.coin.dto;

public class PendingOrderMapper {
    public static PendingOrderDTO toPendingOrderDTO(CoinOrderDTO order) {
        return new PendingOrderDTO(order);
    }
}