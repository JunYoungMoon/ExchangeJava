package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CandleDTO {
    private long timestamp;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    private double volume;
}
