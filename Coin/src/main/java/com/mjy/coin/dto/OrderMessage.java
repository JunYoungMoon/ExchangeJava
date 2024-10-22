package com.mjy.coin.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderMessage {
    private int price;
    private int quantity;
}