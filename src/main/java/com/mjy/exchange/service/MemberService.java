package com.mjy.exchange.service;

import com.mjy.exchange.dto.*;
import com.mjy.exchange.entity.member.Member;
import com.mjy.exchange.repository.master.MasterMemberRepository;
import com.mjy.exchange.repository.slave.SlaveMemberRepository;
import com.mjy.exchange.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MasterMemberRepository masterMemberRepository;
    private final SlaveMemberRepository slaveMemberRepository;
    private final MessageSourceAccessor messageSourceAccessor;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;
    private final MailService mailService;

    private static final String AUTH_CODE_PREFIX = "AuthCode ";
    private static final String EMAIL_VERIFICATION_PREFIX = "EmailVerification ";

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

//    @Transactional(transactionManager = "masterTransactionManager")
    public MemberResponse registerUser(MemberRequest request) {
        // 이메일 중복 체크
        if (slaveMemberRepository.existsByEmail(request.getEmail())) {
            String failMessage = messageSourceAccessor.getMessage("member.alreadyEmail.fail.message");

            throw new RuntimeException(failMessage);
        }

        // 메일 인증 체크
        String storedEmailVerification = redisService.getValues(EMAIL_VERIFICATION_PREFIX + request.getEmail());

        if (!redisService.checkExistsValue(storedEmailVerification) || !storedEmailVerification.equals("success")) {
            String failMessage = messageSourceAccessor.getMessage("member.verificationEmail.fail.message");

            throw new RuntimeException(failMessage);
        }

        // 비밀번호를 Spring Security를 이용하여 해싱하여 저장
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // jwt토큰에 보여질 UUID 생성
        UUID randomUUID = UUID.randomUUID();

        List<String> roles = request.getRoles();

        // role이 null이거나 비어 있는지 확인하고 필요한 경우 기본 role을 할당
        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("user");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .uuid(randomUUID.toString())
                .password(hashedPassword)
                .name(request.getName())
                .roles(roles)
                .build();

        masterMemberRepository.save(member);

        // 회원가입 성공 후에 storedEmailVerification 값을 제거
        redisService.deleteValues(EMAIL_VERIFICATION_PREFIX + request.getEmail());

        return MemberResponse.builder()
                .email(member.getEmail())
                .name(member.getName())
                .build();
    }

    public TokenInfo login(MemberRequest request) {
        //아이디 값으로 사용자 정보를 가져와 uuid로 아이디값을 저장한다.
        //소셜 로그인일때 id 값을 jwt로 노출시키기에는 보안적인 부분을 우려.
        Optional<Member> user = slaveMemberRepository.findByEmail(request.getEmail());

        if (user.isPresent()) {
            //사용자의 인증을 위해 이 객체를 사용하여 사용자가 제공한 아이디와 비밀번호를 저장
            //이 토큰은 사용자 인증을 위해 사용되며, 인증 매니저를 통해 실제 인증이 수행
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.get().getUuid(), request.getPassword());

            //authenticationManagerBuilder는 스프링 시큐리티 설정에서 정의한 AuthenticationManager를 생성하는 빌더 클래스이고
            //getObject() 메서드를 사용하여 실제 AuthenticationManager 객체를 가져온다.
            //AuthenticationManager의 authenticate 메서드에 authenticationToken을 전달하여 사용자를 인증하는데,
            //이때 인증은 CustomUserDetailsService에서 UserDetailsService 인터페이스의 구현 메소드인 loadUserByUsername를 통해 인증을 진행한다.
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            return jwtTokenProvider.generateToken(authentication);
        } else {
            String failMessage = messageSourceAccessor.getMessage("member.notFound.fail.message");

            throw new RuntimeException(request.getEmail() + " " + failMessage);
        }
    }

    public MemberInfo userInfo(String uuid){
        // uuid 기반으로 User 테이블 row 찾기
        Optional<Member> optionalMember = slaveMemberRepository.findByUuid(uuid);

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            return MemberInfo.builder()
                    .profileImage(member.getProfileImage())
                    .email(member.getEmail())
                    .name(member.getName())
                    .nickName(member.getNickname())
                    .build();
        } else {
            String failMessage = messageSourceAccessor.getMessage("member.notFound.fail.message");

            throw new RuntimeException(failMessage);
        }
    }

    @Transactional
    public void updateMemberInfo(String uuid, String newNickname, String newProfileImagePath) {
        String failMessage = messageSourceAccessor.getMessage("member.notFound.fail.message");

        // uuid 기반으로 Member 테이블 row 찾기
        Member member = slaveMemberRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(failMessage));

        if (newNickname != null) {
            member.setNickname(newNickname);
        }
        if (newProfileImagePath != null) {
            member.setProfileImage(newProfileImagePath);
        }

        masterMemberRepository.save(member);
    }

    private String createCode() {
        int length = 6;
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length());
                builder.append(characters.charAt(index));
            }
            return builder.toString();
        } catch (RuntimeException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendCodeToEmail(EmailRequest emailRequest) {
        checkDuplicatedEmail(emailRequest.getEmail());
        String title = messageSourceAccessor.getMessage("email.title.message", new Object[]{"Easy Sign App"});
        String authCode = this.createCode();
        String msg = mailService.sendEmail(emailRequest, title, authCode);

        redisService.setValues(AUTH_CODE_PREFIX + emailRequest.getEmail(),
                authCode, Duration.ofMillis(this.authCodeExpirationMillis));
        return msg;
    }

    private void checkDuplicatedEmail(String email) {
        Optional<Member> existingMember = slaveMemberRepository.findByEmail(email);
        if (existingMember.isPresent()) {
            String failMessage = messageSourceAccessor.getMessage("member.alreadyEmail.fail.message");

            throw new RuntimeException(failMessage + " : " + email);
        }
    }

    public void verifiedCode(EmailVerificationRequest emailVerificationRequest) {
        checkDuplicatedEmail(emailVerificationRequest.getEmail());
        String storedAuthCode = redisService.getValues(AUTH_CODE_PREFIX + emailVerificationRequest.getEmail());

        if (!redisService.checkExistsValue(storedAuthCode)) {
            String failMessage = messageSourceAccessor.getMessage("email.authExpired.fail.message");

            throw new RuntimeException(failMessage);
        }

        if (!storedAuthCode.equals(emailVerificationRequest.getAuthCode())) {
            String failMessage = messageSourceAccessor.getMessage("email.notValidCode.fail.message");

            throw new RuntimeException(failMessage);
        }

        //실제 가입을 할때 체크할 데이터 유효 기간은 하루
        redisService.setValues(EMAIL_VERIFICATION_PREFIX + emailVerificationRequest.getEmail(),
                "success", Duration.ofDays(1));
    }
}

