package com.mjy.exchange.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberRequest extends BaseRequest {
    @NotEmpty(message = "{memberRequest.NotEmpty.password}") //비밀번호는 필수 입력 사항 입니다.
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,30}$"
            , message = "{memberRequest.Pattern.password}") //비밀번호는 최소 8자 최대 30자 이어야 하며, 최소 하나의 숫자와 특수문자를 포함해야 합니다.
    private String password;
    @NotEmpty(message = "{memberRequest.NotEmpty.name}") //이름은 필수 입력 사항 입니다.
    private String name;
    private List<String> roles;
}
