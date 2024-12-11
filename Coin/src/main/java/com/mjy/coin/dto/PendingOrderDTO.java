package com.mjy.coin.dto;

import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class PendingOrderDTO {
    private Long idx; // 주문 ID
    private Long memberIdx; // 주문 등록인, member_idx
    private String memberUuid; // 주문 등록인, member_uuid
    private String marketName; // 예: KRW
    private String coinName; // 예: BTC
    private BigDecimal coinAmount; // 매수/매도 코인 개수
    private BigDecimal orderPrice; // 주문가 (사용자가 입력한 가격)
    private BigDecimal executionPrice; // 체결가 (실제로 거래된 가격)
    private OrderType orderType; // 매수/매도 타입(enum)
    private OrderStatus orderStatus; // 거래 상태 (체결/미체결/취소)
    private BigDecimal fee; //수수료
    private LocalDateTime createdAt; // 등록일자
    private LocalDateTime matchedAt;
    private String matchIdx; // 매수 idx와 매도 idx를 결합한 매치 ID
    private String uuid; //redis 전용 uuid

    public PendingOrderDTO(CoinOrderDTO order) {
        this.idx = order.getIdx();
        this.memberIdx = order.getMemberIdx();
        this.memberUuid = order.getMemberUuid();
        this.marketName = order.getMarketName();
        this.coinName = order.getCoinName();
        this.coinAmount = order.getCoinAmount();
        this.orderPrice = order.getOrderPrice();
        this.orderType = order.getOrderType();
        this.orderStatus = order.getOrderStatus();
        this.createdAt = order.getCreatedAt();
        this.uuid = order.getUuid();
    }
}
