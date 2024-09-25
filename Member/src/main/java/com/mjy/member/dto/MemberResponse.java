package com.mjy.member.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberResponse {
    private String email;
    private String name;
}
