package com.member.exchange.service;

import com.member.exchange.dto.MemberResponse;
import com.member.exchange.entity.Member;
import com.member.exchange.dto.MemberRequest;
import com.member.exchange.repository.master.MasterMemberRepository;
import com.member.exchange.repository.slave.SlaveMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    private static final String EMAIL_VERIFICATION_PREFIX = "EmailVerification ";

    @Mock
    private SlaveMemberRepository slaveMemberRepository;

    @Mock
    private MasterMemberRepository masterMemberRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSourceAccessor messageSourceAccessor;

    @InjectMocks
    private MemberService memberService;

    private MemberRequest memberRequest;

    @BeforeEach
    void setUp() {
        memberRequest = new MemberRequest();
        memberRequest.setEmail("mooon@naver.com");
        memberRequest.setPassword("password123!"); // Ensure the password matches the pattern
        memberRequest.setName("Jun");
        memberRequest.setRoles(null); // Testing with null roles
    }

    @Test
    void whenEmailAlreadyExists_thenThrowRuntimeException() {
        // given
        when(slaveMemberRepository.existsByEmail(memberRequest.getEmail())).thenReturn(true);
        when(messageSourceAccessor.getMessage("member.alreadyEmail.fail.message")).thenReturn("Email already exists");

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> memberService.registerUser(memberRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(slaveMemberRepository, times(1)).existsByEmail(memberRequest.getEmail());
    }

    @Test
    void whenEmailNotVerified_thenThrowRuntimeException() {
        // given
        when(slaveMemberRepository.existsByEmail(memberRequest.getEmail())).thenReturn(false);
        when(redisService.getValues(EMAIL_VERIFICATION_PREFIX + memberRequest.getEmail())).thenReturn("fail");
        when(redisService.checkExistsValue("fail")).thenReturn(false);
        when(messageSourceAccessor.getMessage("member.verificationEmail.fail.message")).thenReturn("Email not verified");

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            memberService.registerUser(memberRequest);
        });

        assertEquals("Email not verified", exception.getMessage());
        verify(redisService, times(1)).getValues(EMAIL_VERIFICATION_PREFIX + memberRequest.getEmail());
    }

    @Test
    void whenValidRequest_thenRegisterUserSuccessfully() {
        // given
        when(slaveMemberRepository.existsByEmail(memberRequest.getEmail())).thenReturn(false);
        when(redisService.getValues(EMAIL_VERIFICATION_PREFIX + memberRequest.getEmail())).thenReturn("success");
        when(redisService.checkExistsValue("success")).thenReturn(true);
        when(passwordEncoder.encode(memberRequest.getPassword())).thenReturn("hashedPassword123");
        when(masterMemberRepository.save(any(Member.class))).thenReturn(null); // save method has void return type

        // when
        MemberResponse response = memberService.registerUser(memberRequest);

        // then
        assertEquals(memberRequest.getEmail(), response.getEmail());
        assertEquals(memberRequest.getName(), response.getName());
        verify(masterMemberRepository, times(1)).save(any(Member.class));
        verify(redisService, times(1)).deleteValues(EMAIL_VERIFICATION_PREFIX + memberRequest.getEmail());
    }

}