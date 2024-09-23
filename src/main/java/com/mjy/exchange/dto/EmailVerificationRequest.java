package com.mjy.exchange.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationRequest extends BaseRequest {
    @NotEmpty(message = "{emailVerificationRequest.NotEmpty.authCode}") //인증코드는 필수 입력 사항 입니다.
    private String authCode;
}
