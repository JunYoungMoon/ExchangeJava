package com.mjy.coin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChartDataRequest {
    @NotEmpty(message = "Resolution is required")
    private String resolution;
    @NotEmpty(message = "Symbol is required")
    private String symbol;
    @NotNull(message = "From is required")
    @Min(value = 1, message = "From timestamp must be greater than 0")
    private Long from;
    @NotNull(message = "To is required")
    @Min(value = 1, message = "To timestamp must be greater than 0")
    private Long to;
}
