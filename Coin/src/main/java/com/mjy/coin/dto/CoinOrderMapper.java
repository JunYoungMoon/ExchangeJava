package com.mjy.coin.dto;

import com.mjy.coin.entity.coin.CoinOrder;

import java.util.List;
import java.util.stream.Collectors;

public class CoinOrderMapper {
    public static CoinOrder toEntity(CoinOrderDTO dto) {
        return CoinOrder.builder()
                .idx(dto.getIdx())
                .memberIdx(dto.getMemberIdx())
                .memberUuid(dto.getMemberUuid())
                .coinName(dto.getCoinName())
                .marketName(dto.getMarketName())
                .orderType(dto.getOrderType())
                .orderPrice(dto.getOrderPrice())
                .quantity(dto.getQuantity())
                .orderStatus(dto.getOrderStatus())
                .fee(dto.getFee())
                .matchIdx(dto.getMatchIdx())
                .matchedAt(dto.getMatchedAt())
                .executionPrice(dto.getExecutionPrice())
                .uuid(dto.getUuid())
                .build();
    }

    public static CoinOrderDTO fromEntity(CoinOrder entity) {
        return new CoinOrderDTO(
                entity.getIdx(),
                entity.getMemberIdx(),
                entity.getMemberUuid(),
                entity.getMarketName(),
                entity.getCoinName(),
                entity.getQuantity(),
                entity.getOrderPrice(),
                entity.getOrderType(),
                entity.getOrderStatus(),
                entity.getFee(),
                entity.getCreatedAt(),
                entity.getMatchIdx(),
                entity.getMatchedAt(),
                entity.getExecutionPrice(),
                entity.getUuid()
        );
    }

    // 새로운 리스트 변환 메서드
    public static List<CoinOrder> toEntityList(List<CoinOrderDTO> orderDTOList) {
        return orderDTOList.stream()
                .map(CoinOrderMapper::toEntity)  // 각각의 DTO를 변환
                .collect(Collectors.toList());
    }
}