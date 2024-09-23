package com.mjy.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberResponse {
    private String email;
    private String name;
}
