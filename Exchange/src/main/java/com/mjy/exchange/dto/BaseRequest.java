package com.mjy.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseRequest {
    @NotEmpty(message = "{baseRequest.NotEmpty.email}") //이메일은 필수 입력 사항입니다.
    @Email(message = "{baseRequest.Email.email}")   //올바른 이메일 형식이어야 합니다.
    @Schema(description = "이메일", defaultValue = "test2@naver.com")
    private String email;
}
