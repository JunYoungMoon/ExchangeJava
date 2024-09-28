package com.mjy.coin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class CoinInfo {
    private Long idx;
    private String marketName;
    private String coinName;
    private BigDecimal feeRate;
    private String coinType;
}