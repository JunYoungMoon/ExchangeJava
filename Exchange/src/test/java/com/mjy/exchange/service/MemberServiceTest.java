package com.mjy.exchange.service;

import com.mjy.exchange.dto.MemberResponse;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.dto.MemberRequest;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.master.MasterMemberRepository;
import com.mjy.exchange.repository.slave.SlaveMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    private MasterCoinHoldingRepository masterCoinHoldingRepository;

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
        memberRequest.setPassword("password123!");
        memberRequest.setName("Jun");
        memberRequest.setRoles(null);
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
        when(masterMemberRepository.save(any(Member.class))).thenReturn(null);

        // when
        MemberResponse response = memberService.registerUser(memberRequest);

        // then
        assertEquals(memberRequest.getEmail(), response.getEmail());
        assertEquals(memberRequest.getName(), response.getName());
        verify(masterMemberRepository, times(1)).save(any(Member.class));
        verify(redisService, times(1)).deleteValues(EMAIL_VERIFICATION_PREFIX + memberRequest.getEmail());
    }

    @Test
    public void testSaveMemberAndCoinHoldings() throws Exception {
        //given
        // 테스트에 사용할 임시 회원 생성
        UUID randomUUID = UUID.randomUUID();
        Member member = Member.builder()
                .socialIdx(123L)
                .uuid(randomUUID.toString())
                .email("test@example.com")
                .name("Test User")
                .roles(List.of("USER"))
                .profileImage("test-image.jpg")
                .build();
        member = masterMemberRepository.save(member); // 회원 저장

        CoinHolding coinHolding1 = CoinHolding.builder()
                .member(member)
                .coinType("BTC")
                .usingAmount(BigDecimal.valueOf(100000000))
                .availableAmount(BigDecimal.valueOf(100000000))
                .walletAddress("1YoURbEATcoiN99MYWaLLetiDaDdRess72")
                .isFavorited(false)
                .build();

        CoinHolding coinHolding2 = CoinHolding.builder()
                .member(member)
                .coinType("ETH")
                .usingAmount(BigDecimal.valueOf(100000000))
                .availableAmount(BigDecimal.valueOf(100000000))
                .walletAddress("0x1234567890ABCDEF1234567890ABCDEF123456")
                .isFavorited(false)
                .build();
        //when
        List<CoinHolding> coinHoldings = List.of(coinHolding1, coinHolding2);
        masterCoinHoldingRepository.saveAll(coinHoldings);

        // 저장된 Member와 CoinHoldings 조회
        Member savedMember = masterMemberRepository.findById(member.getIdx()).orElseThrow();

        //then
    }

}