package com.mjy.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"marketName", "coinName"})
        }
)
public class CoinInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private String marketName; // 예: KRW

    @Column(nullable = false)
    private String coinName; // 예: BTC

    @Column(nullable = false)
    private BigDecimal feeRate; // 거래 수수료율

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoinType coinType; // MAJOR or MINOR

    public enum CoinType {
        MAJOR,
        MINOR
    }
}
