package com.mjy.exchange.controller;

import com.mjy.exchange.annotation.RateLimit;
import com.mjy.exchange.dto.ApiResponse;
import com.mjy.exchange.dto.EmailRequest;
import com.mjy.exchange.dto.EmailVerificationRequest;
import com.mjy.exchange.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/mail")
public class MailController {
    private final MemberService memberService;
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public MailController(MemberService memberService, MessageSourceAccessor messageSourceAccessor) {
        this.memberService = memberService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Operation(summary = "메일 전송", description = "메일을 전송합니다.", security = {@SecurityRequirement(name = "csrfToken")})
    @PostMapping("/send")
    @RateLimit(key = "sendMail", limit = 2, period = 60000)
    public ApiResponse sendMail(HttpServletRequest servletRequest, @RequestBody @Valid EmailRequest emailRequest) {
        String successMessage = memberService.sendCodeToEmail(emailRequest);

        return ApiResponse.builder()
                .status("success")
                .csrfToken(((CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName())).getToken())
                .msg(successMessage)
                .build();
    }

    @Operation(summary = "메일 인증 코드 검증", description = "발송한 메일의 인증코드를 검증합니다.", security = {@SecurityRequirement(name = "csrfToken")})
    @PostMapping("/verify")
    public ApiResponse verificationEmail(HttpServletRequest servletRequest, @RequestBody @Valid EmailVerificationRequest emailVerificationRequest) {
        memberService.verifiedCode(emailVerificationRequest);

        String successMessage = messageSourceAccessor.getMessage("member.verificationEmail.success.message");

        return ApiResponse.builder()
                .status("success")
                .csrfToken(((CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName())).getToken())
                .msg(successMessage)
                .build();
    }
}
