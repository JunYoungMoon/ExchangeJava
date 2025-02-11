package com.mjy.coin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mjy.coin.entity.coin.CoinOrder;
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
public class CoinOrderDTO {
    private Long idx; // 주문 ID
    private Long memberIdx; // 주문 등록인, member_idx
    private String memberUuid; // 주문 등록인, member_uuid
    private String marketName; // 예: KRW
    private String coinName; // 예: BTC
    private BigDecimal quantity; // 매수/매도 코인 개수
    private BigDecimal orderPrice; // 주문가 (사용자가 입력한 가격)
    private BigDecimal executionPrice; // 체결가 (실제로 거래된 가격)
    private OrderType orderType; // 매수/매도 타입(enum)
    private OrderStatus orderStatus; // 거래 상태 (체결/미체결/취소)
    private BigDecimal fee; //수수료
    private LocalDateTime createdAt; // 등록일자
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime matchedAt;
//    private LocalDateTime matchedAt; // 체결일자
    private Long matchIdx; // 매칭되는 반대 주문의 idx
    private String uuid; //redis 전용 uuid

    public CoinOrderDTO() {
    }

    @JsonCreator // JSON 역직렬화를 위한 생성자
    public CoinOrderDTO(
            @JsonProperty("idx") Long idx,
            @JsonProperty("memberIdx") Long memberIdx,
            @JsonProperty("memberUuid") String memberUuid,
            @JsonProperty("marketName") String marketName,
            @JsonProperty("coinName") String coinName,
            @JsonProperty("quantity") BigDecimal quantity,
            @JsonProperty("orderPrice") BigDecimal orderPrice,
            @JsonProperty("orderType") OrderType orderType,
            @JsonProperty("orderStatus") OrderStatus orderStatus,
            @JsonProperty("fee") BigDecimal fee,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("matchIdx") Long matchIdx,
            @JsonProperty("matchedAt") LocalDateTime matchedAt,
            @JsonProperty("executionPrice") BigDecimal executionPrice,
            @JsonProperty("uuid") String uuid
    ) {
        this.idx = idx;
        this.memberIdx = memberIdx;
        this.memberUuid = memberUuid;
        this.marketName = marketName;
        this.coinName = coinName;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.fee = fee;
        this.createdAt = createdAt;
        this.matchIdx = matchIdx;
        this.matchedAt = matchedAt;
        this.executionPrice = executionPrice;
        this.uuid = uuid;
    }

    // 복사 생성자
    public CoinOrderDTO(CoinOrderDTO order) {
        this.idx = order.idx;
        this.memberIdx = order.memberIdx;
        this.memberUuid = order.memberUuid;
        this.marketName = order.marketName;
        this.coinName = order.coinName;
        this.quantity = order.quantity;
        this.orderPrice = order.orderPrice;
        this.executionPrice = order.executionPrice;
        this.orderType = order.orderType;
        this.orderStatus = order.orderStatus;
        this.fee = order.fee;
        this.createdAt = order.createdAt;
        this.matchedAt = order.matchedAt;
        this.matchIdx = order.matchIdx;
        this.uuid = order.uuid;
    }
}
