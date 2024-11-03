package com.mjy.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfoDTO {
    private String grantType;
    private String accessToken;
    private String refreshToken;
}