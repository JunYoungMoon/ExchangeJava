package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartDataRequest {
    private String symbol;
    private long from;
    private long to;
    private String interval;
}
