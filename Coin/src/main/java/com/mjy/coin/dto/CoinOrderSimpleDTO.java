package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class CoinOrderSimpleDTO implements Serializable {
    private Long idx;
    private String coinName;
    private LocalDateTime matchedAt;
}
