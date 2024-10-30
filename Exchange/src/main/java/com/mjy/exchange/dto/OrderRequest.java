package com.mjy.exchange.dto;

import com.mjy.exchange.enums.OrderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderRequest{
    @NotEmpty(message = "{orderRequest.NotEmpty.marketName}") // 마켓 이름은 필수 입력 사항입니다.
    private String marketName; // 예: KRW

    @NotEmpty(message = "{orderRequest.NotEmpty.coinName}") // 코인 이름은 필수 입력 사항입니다.
    private String coinName; // 예: BTC

    @NotNull(message = "{orderRequest.NotEmpty.coinAmount}") // 코인 개수는 필수 입력 사항입니다.
    @DecimalMin(value = "0.01", message = "{orderRequest.Min.coinAmount}")
    private BigDecimal coinAmount; // 매수/매도 코인 개수

    @NotNull(message = "{orderRequest.NotEmpty.orderPrice}") // 주문 금액은 필수 입력 사항입니다.
    @DecimalMin(value = "0.01", message = "{orderRequest.Min.orderPrice}")
    private BigDecimal orderPrice; // 매수/매도 금액

    @NotNull(message = "{orderRequest.NotNull.orderType}") // 주문 타입은 필수 입력 사항입니다.
    private OrderType orderType; // 매수/매도 타입(enum)
}
