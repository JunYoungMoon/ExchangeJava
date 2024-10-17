package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Setter
@Getter
public class CoinOrderDayHistory {
    private Long idx;
    private String marketName; // 예: KRW
    private String coinName; // 예: BTC
    private BigDecimal closingPrice; // 그날의 종가
    private BigDecimal averagePrice; // 그날의 평균가
    private BigDecimal tradingVolume; // 거래량
    private LocalDate tradingDate; // 그날의 일자
}
