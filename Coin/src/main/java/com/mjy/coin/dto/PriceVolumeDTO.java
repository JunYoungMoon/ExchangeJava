package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PriceVolumeDTO {
    private BigDecimal price;
    private BigDecimal volume;
}