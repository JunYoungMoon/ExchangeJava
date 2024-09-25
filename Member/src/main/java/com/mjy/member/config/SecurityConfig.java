package com.mjy.member.config;

import com.mjy.member.exception.CustomAuthenticationEntryPoint;
import com.mjy.member.handler.OAuth2LoginFailureHandler;
import com.mjy.member.handler.OAuth2LoginSuccessHandler;
import com.mjy.member.repository.slave.SlaveMemberRepository;
import com.mjy.member.security.JwtAuthenticationFilter;
import com.mjy.member.security.JwtTokenProvider;
import com.mjy.member.service.CustomOAuth2UserService;
import com.mjy.member.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final SlaveMemberRepository slaveMemberRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    //csrf를 허용할 패턴
    String[] csrfPatterns = new String[]{
            "/api/csrf",
            "/login/**",
            "/oauth2/**",
            "/profile/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    //사용자 인증을 허용할 패턴
    List<String> authPatterns = Arrays.asList(
            "/api/csrf",
            "/api/users",
            "/api/mail/send",
            "/api/mail/verify",
            "/api/users/login",
            "/api/check-auth",
            "/api/test",
            "/login/**",
            "/oauth2/**",
            "/profile/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    );

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //CSP로 XSS 공격을 방지 및 csrf 검증 모바일일때 제외
        http
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("script-src 'self'")
                        )
                )
//                .headers(headers ->
//                        headers.contentSecurityPolicy("script-src 'self'"))
//                .csrf()
                .csrf((csrf) -> csrf
                        .requireCsrfProtectionMatcher(request -> !CommonUtil.isMobile(request))
                        .ignoringRequestMatchers(csrfPatterns)
                        .csrfTokenRepository(csrfTokenRepository()))
//                .requireCsrfProtectionMatcher(request -> !CommonUtil.isMobile(request))
//                .ignoringAntMatchers(csrfPatterns)
//                .csrfTokenRepository(csrfTokenRepository())
//                .and()
//                .cors()
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //jwt filter 설정
        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, authPatterns, slaveMemberRepository), UsernamePasswordAuthenticationFilter.class);
        //요청에 대한 권한 설정
//        http
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers(authPatterns).permitAll()
//                        .anyRequest().authenticated()
//                );
//                .authorizeRequests()
//                .antMatchers(authPatterns).permitAll()
//                .anyRequest().authenticated(); // 그 외의 URL은 인증된 사용자만 접근 가능
        //인증 실패시 예외 처리 재정의
        http
                .exceptionHandling((exceptionHandling) -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint()));
//                .exceptionHandling()
//                .authenticationEntryPoint(customAuthenticationEntryPoint());
        //OAuth 2.0 로그인 설정 시작
        http
                .oauth2Login((oauth2) -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                );
//                .oauth2Login()
//                .successHandler(oAuth2LoginSuccessHandler)
//                .failureHandler(oAuth2LoginFailureHandler)
//                .userInfoEndpoint()
//                .userService(customOAuth2UserService);

        //csrf filter 검증 이후 새로운 토큰 발급 필터 설정
        //csrf는 XSRF-TOKEN의 쿠키값과 전달받은 파라미터 _csrf 혹은 헤더 X-XSRF-TOKEN 값과 비교한다.
//        http
//                .addFilterAfter(new CsrfTokenRenewalFilter(csrfTokenRepository()), CsrfFilter.class);

        return http.build();
    }
}