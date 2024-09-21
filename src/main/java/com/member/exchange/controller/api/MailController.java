package com.member.exchange.controller.api;

import com.member.exchange.annotation.RateLimit;
import com.member.exchange.dto.ApiResponse;
import com.member.exchange.dto.EmailRequest;
import com.member.exchange.dto.EmailVerificationRequest;
import com.member.exchange.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;


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
