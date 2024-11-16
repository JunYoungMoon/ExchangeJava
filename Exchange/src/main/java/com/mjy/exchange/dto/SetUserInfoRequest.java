package com.mjy.exchange.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class SetUserInfoRequest {
    @Schema(description = "프로필 이미지", defaultValue = "MultipartFile 타입")
    MultipartFile profileImage;
    @Schema(description = "변경 닉네임", defaultValue = "변경할 닉네임")
    String nickname;
}
