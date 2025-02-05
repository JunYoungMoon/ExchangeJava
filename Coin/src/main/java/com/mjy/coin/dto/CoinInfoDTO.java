package com.mjy.coin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
public class CoinInfoDTO {
    private Long idx;
    private String marketName;
    private String coinName;
    private BigDecimal feeRate;
    private String coinType;

    public CoinInfoDTO(String marketName, String coinName, BigDecimal feeRate, String coinType) {
        this.marketName = marketName;
        this.coinName = coinName;
        this.feeRate = feeRate;
        this.coinType = coinType;
    }
}