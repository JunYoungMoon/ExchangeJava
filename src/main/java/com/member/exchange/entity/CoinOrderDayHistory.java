package com.member.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 코인 거래의 체결 상세 CoinOrder를 바탕으로 일일 종가 내역을 저장합니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinOrderDayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private String marketName; // 예: KRW

    @Column(nullable = false)
    private String coinName; // 예: BTC

    @Column(nullable = false)
    private BigDecimal closingPrice; // 그날의 종가

    @Column(nullable = false)
    private BigDecimal averagePrice; // 그날의 평균가

    @Column(nullable = false)
    private BigDecimal tradingVolume; // 거래량

    @Column(nullable = false)
    private LocalDate tradingDate; // 그날의 일자
}
