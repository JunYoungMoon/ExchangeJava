package com.mjy.coin.dto;

import com.mjy.coin.entity.coin.CoinOrder;

import java.util.List;
import java.util.stream.Collectors;

public class CoinOrderMapper {
    public static CoinOrder toEntity(CoinOrderDTO dto) {
        CoinOrder entity = new CoinOrder();

        // DTO의 idx가 null이 아니면 엔티티에 설정
        if (dto.getIdx() != null) {
            entity.setIdx(dto.getIdx());
        }

        entity.setMemberIdx(dto.getMemberIdx());
        entity.setMemberUuid(dto.getMemberUuid());
        entity.setCoinName(dto.getCoinName());
        entity.setMarketName(dto.getMarketName());
        entity.setOrderType(dto.getOrderType());
        entity.setOrderPrice(dto.getOrderPrice());
        entity.setCoinAmount(dto.getCoinAmount());
        entity.setOrderStatus(dto.getOrderStatus());
        entity.setFee(dto.getFee());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setMatchIdx(dto.getMatchIdx());
        entity.setMatchedAt(dto.getMatchedAt());
        entity.setExecutionPrice(dto.getExecutionPrice());
        entity.setUuid(dto.getUuid());
        return entity;
    }

    public static CoinOrderDTO fromEntity(CoinOrder entity) {
        return new CoinOrderDTO(
                entity.getIdx(),
                entity.getMemberIdx(),
                entity.getMemberUuid(),
                entity.getMarketName(),
                entity.getCoinName(),
                entity.getCoinAmount(),
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