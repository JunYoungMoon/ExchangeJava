package com.mjy.coin.dto;

import lombok.Getter;

@Getter
public class ChartDataRequest {
    private String symbol;
    private long from;
    private long to;
    private String interval;
}
