package com.mjy.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long memberIdx;

    @Column(length = 10, nullable = false)
    private String coinType;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal usingAmount;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal availableAmount;

    @Column(length = 64)
    private String walletAddress;

    @Column(nullable = false)
    private Boolean isFavorited = false;

    @Builder
    public CoinHolding(Long memberIdx, String coinType, BigDecimal usingAmount, BigDecimal availableAmount, String walletAddress, Boolean isFavorited) {
        this.memberIdx = memberIdx;
        this.coinType = coinType;
        this.usingAmount = usingAmount;
        this.availableAmount = availableAmount;
        this.walletAddress = walletAddress;
        this.isFavorited = isFavorited;
    }
}