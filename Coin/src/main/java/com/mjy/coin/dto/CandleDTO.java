package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class CandleDTO {
    private String timeInterval;
    private int orderCount;
    private BigDecimal totalTradedValue;
    private BigDecimal totalVolume;
    private BigDecimal openingPrice;
    private BigDecimal closingPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private long firstMatchedAtUnix;
    private long lastMatchedAtUnix;
}
