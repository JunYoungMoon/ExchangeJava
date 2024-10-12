package com.mjy.coin.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mjy.coin.entity.coin.CoinOrder;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.util.LocalDateTimeDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CoinOrderDTO {
    private Long idx; // 주문 ID
    private Long memberId; // 주문 등록인, member_id
    private String marketName; // 예: KRW
    private String coinName; // 예: BTC
    private BigDecimal coinAmount; // 매수/매도 코인 개수
    private BigDecimal orderPrice; // 주문가 (사용자가 입력한 가격)
    private BigDecimal executionPrice; // 체결가 (실제로 거래된 가격)
    private OrderType orderType; // 매수/매도 타입(enum)
    private OrderStatus orderStatus; // 거래 상태 (체결/미체결/취소)
    private BigDecimal fee; //수수료
    private LocalDateTime createdAt; // 등록일자
//    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime matchedAt; // 체결일자
    private String matchIdx; // 매수 idx와 매도 idx를 결합한 매치 ID
    private String uuid; //redis 전용 uuid

    @JsonCreator // JSON 역직렬화를 위한 생성자
    public CoinOrderDTO(
            @JsonProperty("idx") Long idx,
            @JsonProperty("memberId") Long memberId,
            @JsonProperty("marketName") String marketName,
            @JsonProperty("coinName") String coinName,
            @JsonProperty("coinAmount") BigDecimal coinAmount,
            @JsonProperty("orderPrice") BigDecimal orderPrice,
            @JsonProperty("orderType") OrderType orderType,
            @JsonProperty("orderStatus") OrderStatus orderStatus,
            @JsonProperty("fee") BigDecimal fee,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("matchIdx") String matchIdx,
            @JsonProperty("matchedAt") LocalDateTime matchedAt,
            @JsonProperty("executionPrice") BigDecimal executionPrice,
            @JsonProperty("uuid") String uuid
    ) {
        this.idx = idx;
        this.memberId = memberId;
        this.marketName = marketName;
        this.coinName = coinName;
        this.coinAmount = coinAmount;
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

    // 엔티티에서 VO로 변환하는 정적 팩토리 메서드
    public static CoinOrderDTO fromEntity(CoinOrder entity) {
        return new CoinOrderDTO(
                entity.getIdx(),
                entity.getMemberId(),
                entity.getMarketName(),
                entity.getCoinName(),
                entity.getCoinAmount(),
                entity.getOrderPrice(),
                entity.getOrderType(),
                entity.getOrderStatus(),
                entity.getFee(),
                entity.getCreatedAt(),
                entity.getMatchIdx(),
                entity.getMatchedAt(),
                entity.getExecutionPrice(),
                entity.getUuid()
        );
    }

    @Override
    public String toString() {
        return "CoinOrderDTO{" +
                "idx=" + idx +
                ", memberId=" + memberId +
                ", marketName='" + marketName + '\'' +
                ", coinName='" + coinName + '\'' +
                ", coinAmount=" + coinAmount +
                ", orderPrice=" + orderPrice +
                ", orderType=" + orderType +
                ", orderStatus=" + orderStatus +
                ", fee=" + fee +
                ", createdAt=" + createdAt +
                ", matchedAt=" + matchedAt +
                ", matchedOrderIdx=" + matchIdx +
                ", executionPrice=" + executionPrice +
                ", uuid=" + uuid +
                '}';
    }
}
