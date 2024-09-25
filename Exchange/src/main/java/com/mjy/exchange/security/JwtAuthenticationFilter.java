package com.mjy.exchange.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.exchange.dto.TokenInfo;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.repository.slave.SlaveMemberRepository;
import com.mjy.exchange.util.CommonUtil;
import com.mjy.exchange.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> authPatterns;
    private final SlaveMemberRepository slaveMemberRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, List<String> authPatterns, SlaveMemberRepository slaveMemberRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authPatterns = authPatterns;
        this.slaveMemberRepository = slaveMemberRepository;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        PathMatcher pathMatcher = new AntPathMatcher();

        if (isAuthPatternMatched(request.getRequestURI(), authPatterns, pathMatcher)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        try {
            if (token == null || token.isEmpty()) {
                CommonUtil.handleException(request, response, "JWT does not exist.");
                return;
            }

            jwtTokenProvider.validateToken(token);
            Claims claims = jwtTokenProvider.parseClaims(token);
            String tokenType = claims.get("tokenType", String.class);

            switch (tokenType) {
                case "refresh" -> handleRefreshToken(request, response, token, claims);
                case "access" -> handleAccessToken(request, response, filterChain, claims);
                default -> CommonUtil.handleException(request, response, "JWT type is incorrect.");
            }
        } catch (ExpiredJwtException e) {
            handleExpiredToken(request, response, e);
        } catch (JwtException | IllegalArgumentException e) {
            CommonUtil.handleException(request, response, "Invalid JWT");
        } catch (SecurityException e) {
            CommonUtil.handleException(request, response, "Forbidden");
        }
    }

    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response, String token, Claims claims) throws IOException {
        if (!jwtTokenProvider.isRefreshTokenValid(token, claims.getSubject())) {
            CommonUtil.handleException(request, response, "Validation failed with the corresponding refresh token.");
            return;
        }

//        Authentication authentication = createAuthentication(claims);

        String uuid = claims.getSubject();
        Optional<Member> user = slaveMemberRepository.findByUuid(uuid);

        if (user.isEmpty()) {
            CommonUtil.handleException(request, response, "User information not found.");
            return;
        }

        SecurityMember securityMember = new SecurityMember(user.get());
        Collection<? extends GrantedAuthority> authorities = securityMember.getAuthorities();

        UserDetails principal = new org.springframework.security.core.userdetails.User(uuid, "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "", authorities);

        TokenInfo newTokenInfo = jwtTokenProvider.generateToken(authentication);

        ApiResponse apiResponse = ApiResponse.builder()
                .status("success")
                .csrfToken(((CsrfToken) request.getAttribute(CsrfToken.class.getName())).getToken())
                .msg("A new token has been created.")
                .data(newTokenInfo)
                .build();

        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(apiResponse));
    }

    private void handleAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Claims claims) throws ServletException, IOException {
        if (claims.get("auth") == null) {
            CommonUtil.handleException(request, response, "You do not have permission.");
            return;
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, "", authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void handleExpiredToken(HttpServletRequest request, HttpServletResponse response, ExpiredJwtException e) throws IOException {
        String tokenType = e.getClaims().get("tokenType", String.class);
        if ("access".equals(tokenType)) {
            CommonUtil.handleException(request, response, "Please provide a refresh token.");
        } else {
            CommonUtil.handleException(request, response, "Refresh token expired.");
        }
    }

    private boolean isAuthPatternMatched(String requestURI, List<String> authPatterns, PathMatcher pathMatcher) {
        for (String pattern : authPatterns) {
            if (pathMatcher.match(pattern, requestURI)) {
                return true;
            }
        }
        return false;
    }
}