package com.mjy.exchange.service;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.entity.SocialMember;
import com.mjy.exchange.enums.AuthProvider;
import com.mjy.exchange.oauth2.OAuth2UserInfo;
import com.mjy.exchange.oauth2.OAuth2UserInfoFactory;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.master.MasterMemberRepository;
import com.mjy.exchange.repository.master.MasterSocialMemberRepository;
import com.mjy.exchange.repository.slave.SlaveMemberRepository;
import com.mjy.exchange.repository.slave.SlaveSocialMemberRepository;
import com.mjy.exchange.security.SecurityMember;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MasterSocialMemberRepository masterSocialMemberRepository;
    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveSocialMemberRepository socialMemberRepository;
    private final MasterMemberRepository masterMemberRepository;
    private final SlaveMemberRepository slaveMemberRepository;
    private final MessageSourceAccessor messageSourceAccessor;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("getAttributes : {}", oAuth2User.getAttributes());

        return processOAuth2User(userRequest, oAuth2User);
    }

    protected OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        AuthProvider authProvider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authProvider, oAuth2User.getAttributes());

        Optional<SocialMember> optionalUser = socialMemberRepository.findByProviderId(oAuth2UserInfo.getOAuth2Id());

        SocialMember socialMember;
        Member member;

        if (optionalUser.isEmpty()) {
            socialMember = SocialMember.builder()
                    .provider(authProvider)
                    .providerId(oAuth2UserInfo.getOAuth2Id())
                    .build();

            socialMember = masterSocialMemberRepository.save(socialMember);

            Long socialMemberId = socialMember.getIdx();

            List<String> roles = new ArrayList<>();
            roles.add("user");

            //jwt토큰에 보여질 UUID 생성
            UUID randomUUID = UUID.randomUUID();

            member = Member.builder()
                    .socialIdx(socialMemberId)
                    .uuid(randomUUID.toString())
                    .email(oAuth2UserInfo.getEmail())
                    .name(oAuth2UserInfo.getName())
                    .roles(roles)
                    .profileImage(oAuth2UserInfo.getProfileImage())
                    .build();

            member = masterMemberRepository.save(member); // 저장된 Member 객체

            // 임시 지갑 생성
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

            List<CoinHolding> coinHoldings = Arrays.asList(coinHolding1, coinHolding2);

            masterCoinHoldingRepository.saveAll(coinHoldings);
        } else {
            socialMember = optionalUser.get();

            Optional<Member> foundMember = slaveMemberRepository.findBySocialIdx(socialMember.getIdx());
            if (foundMember.isPresent()) {
                member = foundMember.get();
            } else {
                String failMessage = messageSourceAccessor.getMessage("member.notFound.fail.message");

                throw new RuntimeException(failMessage);
            }
        }

        return new SecurityMember(member, oAuth2User.getAttributes());
    }
}
