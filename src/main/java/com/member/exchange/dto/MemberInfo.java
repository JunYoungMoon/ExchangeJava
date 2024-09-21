package com.member.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MemberInfo {
    private String email;
    private String name;
    private String profileImage;
    private String nickName;
}