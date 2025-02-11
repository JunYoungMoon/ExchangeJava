package com.mjy.coin.entity.coin;

import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 거래의 체결 상세 내역을 저장합니다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 빌더 패턴에서만 객체 생성 가능하도록 제한
@Builder
public class CoinOrder extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx; // 주문 ID

    @Column(nullable = false)
    private Long memberIdx; // 주문 등록인, member_idx

    @Column(nullable = false)
    private String memberUuid; // 주문 등록인, member_uuid

    @Column(nullable = false)
    private String marketName; // 예: KRW

    @Column(nullable = false)
    private String coinName; // 예: BTC

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType; // 매수/매도 (enum)

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal quantity; // 매수/매도 코인 개수

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal orderPrice; // 주문가 (사용자가 입력한 가격)

    private BigDecimal executionPrice; // 체결가 (실제로 거래된 가격)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus; // 거래 상태 (enum: 체결/미체결/취소)

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal fee; // 수수료

    @Column(nullable = false)
    private String uuid; // redis uuid

    @Column
    private Long matchIdx; // 매칭되는 반대 주문의 idx

    @Column
    private LocalDateTime matchedAt; // 체결일자
}