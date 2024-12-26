package com.mjy.coin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CoinDailyCloseDTO {
    private String coinName;
    private String marketName;
    private BigDecimal closePrice;
}
