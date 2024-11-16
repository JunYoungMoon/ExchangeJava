package com.mjy.exchange.dto;

import com.mjy.exchange.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRequest{

    @NotEmpty(message = "{orderRequest.NotEmpty.marketName}") // 마켓 이름은 필수 입력 사항입니다.
    @Schema(description = "마켓", defaultValue = "KRW")
    private String marketName; // 마켓

    @NotEmpty(message = "{orderRequest.NotEmpty.coinName}") // 코인 이름은 필수 입력 사항입니다.
    @Schema(description = "코인명", defaultValue = "BTC")
    private String coinName; // 코인명

    @NotNull(message = "{orderRequest.NotEmpty.coinAmount}") // 코인 개수는 필수 입력 사항입니다.
    @Schema(description = " 매수/매도 코인 개수", defaultValue = "0.01")
    private BigDecimal coinAmount; // 매수/매도 코인 개수

    @NotNull(message = "{orderRequest.NotEmpty.orderPrice}") // 주문 금액은 필수 입력 사항입니다.
    @Schema(description = "매수/매도 금액", defaultValue = "5000")
    private BigDecimal orderPrice; // 매수/매도 금액

    @NotNull(message = "{orderRequest.NotNull.orderType}") // 주문 타입은 필수 입력 사항입니다.
    @Schema(description = "매수/매도 타입(enum)", defaultValue = "BUY")
    private OrderType orderType; // 매수/매도 타입(enum)
}
