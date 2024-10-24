package com.mjy.coin.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ChartDataRequest {
    @NotEmpty(message = "Resolution is required")
    private String resolution;
    @NotEmpty(message = "Symbol is required")
    private String symbol;
    @NotEmpty(message = "From is required")
    private long from;
    @NotEmpty(message = "To is required")
    private long to;
}
