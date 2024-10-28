package com.mjy.exchange.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecuritySchemes({
        @SecurityScheme(
                name = "csrfToken",
                type = SecuritySchemeType.APIKEY,
                in = SecuritySchemeIn.HEADER,
                paramName = "X-XSRF-TOKEN"  // CSRF 토큰을 받을 헤더 이름
        ),
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
@OpenAPIDefinition(
        info = @Info(
                title = "Exchange API",
                description = "<p>간단한 회원가입 앱입니다.</p>" +
                        "<p>/api/csrf 호출로 CSRF 토큰을 헤더에 입력 후 API 요청이 가능합니다.</p>" +
                        "<p>회원가입 시 이메일 인증을 먼저 진행해야 합니다.</p>",
                license = @License(name = "GitHub", url = "https://github.com/JunYoungMoon/Exchange")
        ),
        servers = @Server(url = "http://localhost:8080")
)
public class SwaggerConfig {
}
