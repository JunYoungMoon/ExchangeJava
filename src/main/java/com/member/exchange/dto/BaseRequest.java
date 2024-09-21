package com.member.exchange.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
public class BaseRequest {
    @NotEmpty(message = "{baseRequest.NotEmpty.email}") //이메일은 필수 입력 사항입니다.
    @Email(message = "{baseRequest.Email.email}")   //올바른 이메일 형식이어야 합니다.
    private String email;
}
