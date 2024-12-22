package com.mjy.exchange.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
public class CoinHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

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
    public CoinHolding(Member member, String coinType, BigDecimal usingAmount, BigDecimal availableAmount, String walletAddress, Boolean isFavorited) {
        this.member = member;
        this.coinType = coinType;
        this.usingAmount = usingAmount;
        this.availableAmount = availableAmount;
        this.walletAddress = walletAddress;
        this.isFavorited = isFavorited;
    }

    public CoinHolding() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberUuid", referencedColumnName = "uuid", insertable = false, updatable = false)
    private Member member;
}
