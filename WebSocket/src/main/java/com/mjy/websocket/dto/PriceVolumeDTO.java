package com.mjy.websocket.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PriceVolumeDTO {
    private BigDecimal price;
    private BigDecimal volume;

    @JsonCreator // JSON 역직렬화를 위한 생성자
    public PriceVolumeDTO(
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("volume") BigDecimal volume) {
        this.price = price;
        this.volume = volume;
    }

    // 복사 생성자
    public PriceVolumeDTO(CoinOrderDTO order) {
        this.price = order.getExecutionPrice();
        this.volume = order.getCoinAmount();
    }

    @Override
    public String toString() {
        return "PriceVolumeDTO{" +
                "price=" + price +
                ", volume=" + volume +
                '}';
    }
}