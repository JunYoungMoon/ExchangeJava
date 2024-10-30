package com.mjy.exchange.service;

import com.mjy.exchange.dto.*;
import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.master.MasterMemberRepository;
import com.mjy.exchange.repository.slave.SlaveCoinHoldingRepository;
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

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final MasterMemberRepository masterMemberRepository;
    private final SlaveMemberRepository slaveMemberRepository;
    private final MasterCoinHoldingRepository masterCoinHoldingRepository;
    private final SlaveCoinHoldingRepository slaveCoinHoldingRepository;
    private final MessageSourceAccessor messageSourceAccessor;

    public void orderService() {

        //1. 잔액확인

        //2. 잔액에서 수수료 차감

        //3. CoinOrderDTO 입력

        //4. kafka send

    }
}

