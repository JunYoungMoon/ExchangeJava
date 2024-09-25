package com.mjy.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequest extends BaseRequest {

    private String checkSum;
}
