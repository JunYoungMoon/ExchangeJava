package com.mjy.coin.entity.coin;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 거래의 체결 상세 내역을 저장합니다.
 */
@Getter
@Setter
@Entity
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // 주문 ID

    @Column(nullable = false)
    private Long memberId; // 주문 등록인, member_id

    @Column(nullable = false)
    private String marketName; // 예: KRW

    @Column(nullable = false)
    private String coinName; // 예: BTC

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType; // 매수/매도 (enum)

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal coinAmount; // 매수/매도 코인 개수

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal orderPrice; // 매수/매도 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus; // 거래 상태 (enum: 체결/미체결/취소)

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus; // 체결 상태 (enum: 완전체결/부분체결)

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal fee; // 수수료

    @Column
    private Long matchedMemberId; // 거래 성사인, member_id

    @Column(nullable = false)
    private LocalDateTime createdAt; // 등록일자

    @Column
    private LocalDateTime matchedAt; // 체결일자

    // 주문 타입 (매수/매도)
    public enum OrderType {
        BUY, // 매수
        SELL // 매도
    }

    // 거래 상태 (체결/미체결/취소)
    public enum OrderStatus {
        PENDING, // 미체결
        COMPLETED, // 체결
        CANCELED // 취소
    }

    // 체결 상태 (완전체결/부분체결)
    public enum MatchStatus {
        FULL, // 완전체결
        PARTIAL // 부분체결
    }
}