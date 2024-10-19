package com.mjy.coin.dto;

import com.mjy.coin.entity.coin.CoinOrderDayHistory;

public class CoinOrderDayHistoryMapper {
    public static CoinOrderDayHistory toEntity(CoinOrderDayHistoryDTO dto) {
        CoinOrderDayHistory entity = new CoinOrderDayHistory();

        entity.setCoinName(dto.getCoinName());
        entity.setMarketName(dto.getMarketName());
        entity.setClosingPrice(dto.getClosingPrice());
        entity.setAveragePrice(dto.getAveragePrice());
        entity.setTradingVolume(dto.getTradingVolume());
        entity.setTradingDate(dto.getTradingDate());

        return entity;
    }
}