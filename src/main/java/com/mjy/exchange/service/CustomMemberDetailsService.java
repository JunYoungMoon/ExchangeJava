package com.mjy.exchange.service;

import com.mjy.exchange.repository.member.slave.SlaveMemberRepository;
import com.mjy.exchange.security.SecurityMember;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailsService implements UserDetailsService {
    private final SlaveMemberRepository slaveMemberRepository;
    private final MessageSourceAccessor messageSourceAccessor;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String failMessage = messageSourceAccessor.getMessage("member.notFound.fail.message");

        return new SecurityMember(slaveMemberRepository.findByUuid(username)
                .orElseThrow(() -> new RuntimeException(failMessage)));
    }
}
