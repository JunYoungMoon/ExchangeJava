package com.mjy.coin.dto;

import com.mjy.coin.entity.coin.CoinOrder;

public class CoinOrderMapper {
    public static CoinOrder toEntity(CoinOrderDTO dto) {
        CoinOrder entity = new CoinOrder();

        // DTO의 idx가 null이 아니면 엔티티에 설정
        if (dto.getIdx() != null) {
            entity.setIdx(dto.getIdx());
        }

        entity.setMemberId(dto.getMemberId());
        entity.setCoinName(dto.getCoinName());
        entity.setMarketName(dto.getMarketName());
        entity.setOrderType(dto.getOrderType());
        entity.setOrderPrice(dto.getOrderPrice());
        entity.setCoinAmount(dto.getCoinAmount());
        entity.setOrderStatus(dto.getOrderStatus());
        entity.setFee(dto.getFee());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setMatchedAt(dto.getMatchedAt());
        entity.setMatchIdx(dto.getMatchIdx());
        entity.setExecutionPrice(dto.getExecutionPrice());
        return entity;
    }
}