package com.mjy.coin.dto;

import com.mjy.coin.entity.coin.CoinOrder;

public class CoinOrderMapper {
    public static CoinOrder toEntity(CoinOrderDTO dto) {
        CoinOrder entity = new CoinOrder();
        entity.setMemberId(dto.getMemberId());
        entity.setCoinName(dto.getCoinName());
        entity.setMarketName(dto.getMarketName());
        entity.setOrderType(dto.getOrderType());
        entity.setOrderPrice(dto.getOrderPrice());
        entity.setCoinAmount(dto.getCoinAmount());
        entity.setOrderStatus(dto.getOrderStatus());
        entity.setFee(dto.getFee());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }
}