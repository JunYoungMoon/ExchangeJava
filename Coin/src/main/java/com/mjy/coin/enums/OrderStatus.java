package com.mjy.coin.enums;

// 거래 상태 (체결/미체결/취소)
public enum OrderStatus {
    PENDING, // 미체결
    COMPLETED, // 체결
    CANCELED // 취소
}