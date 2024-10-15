package com.mjy.exchange.dto;

import com.mjy.exchange.enums.OrderStatus;
import com.mjy.exchange.enums.OrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CoinOrder {
    private Long memberId; // 주문 등록인, member_id
    private String marketName; // 예: KRW
    private String coinName; // 예: BTC
    private BigDecimal coinAmount; // 매수/매도 코인 개수
    private BigDecimal orderPrice; // 매수/매도 금액
    private OrderType orderType; // 매수/매도 타입(enum)
    private OrderStatus orderStatus; // 거래 상태 (체결/미체결/취소)
    private BigDecimal fee; //수수료
    private LocalDateTime createdAt; // 등록일자
    private LocalDateTime matchedAt; // 등록일자
}
