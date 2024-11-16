package com.mjy.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
public class EmailRequest extends BaseRequest {
    @Schema(description = "체크썸", defaultValue = "개발환경 일때는 아무값이나 입력 가능")
    private String checkSum;
}
