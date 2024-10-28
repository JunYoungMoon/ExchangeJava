package com.mjy.exchange.controller;

import com.mjy.exchange.dto.ApiResponse;
import com.mjy.exchange.dto.MemberRequest;
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
@RequestMapping("/api/order")
public class OrderController {
    private final MemberService memberService;
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public OrderController(MemberService memberService, MessageSourceAccessor messageSourceAccessor) {
        this.memberService = memberService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Operation(summary = "코인 주문", description = "코인을 주문합니다.", security = {@SecurityRequirement(name = "csrfToken")})
    @PostMapping
    public ApiResponse order(HttpServletRequest servletRequest, @RequestBody @Valid MemberRequest memberRequest) {
        String successMessage = messageSourceAccessor.getMessage("member.registerUser.success.message");

        return ApiResponse.builder()
                .status("success")
                .csrfToken(((CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName())).getToken())
                .msg(successMessage)
                .data(memberService.registerUser(memberRequest))
                .build();
    }
}
