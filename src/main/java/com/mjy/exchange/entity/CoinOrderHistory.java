package com.mjy.exchange.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 거래의 체결 히스토리를 저장합니다.
 * 차트 정보를 내려줄때 사용할 데이터입니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinOrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // 체결 히스토리 ID

    @Column(nullable = false)
    private String marketName; // 예: KRW

    @Column(nullable = false)
    private String coinName; // 예: BTC

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal executionPrice; // 체결 가격

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal executionQuantity; // 체결 수량

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType; // 매수/매도 (enum)

    @Column(nullable = false)
    private LocalDateTime executionTime; // 체결 시각

    // 매수/매도 타입 (enum)
    public enum OrderType {
        BUY, // 매수
        SELL // 매도
    }
}
