package com.mjy.coin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // 빈 값은 포함 하지 않도록 설정
public class ApiResponse {
    private String status;
    private String msg;
    private Object data;
}