package com.mjy.member.api;

import com.mjy.member.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public CsrfController(MessageSourceAccessor messageSourceAccessor) {
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Operation(summary = "CSRF 토큰 얻기", description = "CSRF 토큰을 얻습니다.")
    @GetMapping
    public ApiResponse getCsrfToken(HttpServletRequest servletRequest) {
        CsrfToken csrfToken = (CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName());

        String successMessage = messageSourceAccessor.getMessage("csrf.success.message", new Object[]{csrfToken.getToken()});

        return ApiResponse.builder()
                .status("success")
                .csrfToken(csrfToken.getToken())
                .msg(successMessage)
                .build();
    }
}