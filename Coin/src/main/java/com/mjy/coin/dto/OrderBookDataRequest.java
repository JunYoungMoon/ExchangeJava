package com.mjy.coin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderBookDataRequest {
    @NotEmpty(message = "Symbol is required")
    String symbol;
}
