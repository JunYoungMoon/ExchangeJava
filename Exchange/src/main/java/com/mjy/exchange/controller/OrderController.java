package com.mjy.exchange.controller;

import com.mjy.exchange.dto.ApiResponse;
import com.mjy.exchange.dto.MemberRequest;
import com.mjy.exchange.dto.OrderRequest;
import com.mjy.exchange.security.SecurityMember;
import com.mjy.exchange.service.MemberService;
import com.mjy.exchange.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final MessageSourceAccessor messageSourceAccessor;

    @Autowired
    public OrderController(OrderService orderService, MessageSourceAccessor messageSourceAccessor) {
        this.orderService = orderService;
        this.messageSourceAccessor = messageSourceAccessor;
    }

    @Operation(summary = "코인 주문", description = "코인을 주문합니다.", security = {@SecurityRequirement(name = "csrfToken"), @SecurityRequirement(name = "bearerAuth")})
    @PostMapping
    public ApiResponse order(HttpServletRequest servletRequest,
                             @RequestBody @Valid OrderRequest orderRequest) {
        // 현재 사용자의 인증 객체 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체에서 사용자 정보 가져오기
        Object principal = authentication.getPrincipal();
        String uuid = ((UserDetails) principal).getUsername();

        orderService.processOrder(orderRequest, uuid);

        return ApiResponse.builder()
                .status("success")
                .csrfToken(((CsrfToken) servletRequest.getAttribute(CsrfToken.class.getName())).getToken())
                .msg("코인 주문 완료")
                .data("data")
                .build();
    }
}
