package com.member.exchange.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CsrfTokenRenewalFilter extends OncePerRequestFilter{

    private final CsrfTokenRepository csrfTokenRepository;

    public CsrfTokenRenewalFilter(CsrfTokenRepository csrfTokenRepository) {
        this.csrfTokenRepository = csrfTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //새로운 토큰 생성 후 저장
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);

        //컨트롤러에서 사용하기 위한 속성 설정
        request.setAttribute("myCsrfToken", csrfToken.getToken());

        filterChain.doFilter(request, response);
    }
}
