package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.repository.master.MasterCoinHoldingRepository;
import com.mjy.exchange.repository.master.MasterMemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SlaveMemberRepositoryTest {

    @Autowired
    private SlaveMemberRepository slaveMemberRepository;

    @Autowired
    private MasterMemberRepository masterMemberRepository;

    @Autowired
    private MasterCoinHoldingRepository masterCoinHoldingRepository;

    @Test
    void testFindMemberWithCoinHoldingsByUuid() {
        // Given
        Member member = Member.builder()
                .uuid("test-uuid")
                .email("test@example.com")
                .name("Test User")
                .build();

        masterMemberRepository.save(member);

        CoinHolding holding1 = CoinHolding.builder()
                .member(member)
                .coinType("BTC")
                .usingAmount(BigDecimal.valueOf(0.5))
                .availableAmount(BigDecimal.valueOf(1.0))
                .walletAddress("wallet-123")
                .isFavorited(true)
                .build();

        masterCoinHoldingRepository.save(holding1);

        CoinHolding holding2 = CoinHolding.builder()
                .member(member)
                .coinType("ETH")
                .usingAmount(BigDecimal.valueOf(0.2))
                .availableAmount(BigDecimal.valueOf(0.8))
                .walletAddress("wallet-456")
                .isFavorited(false)
                .build();
        masterCoinHoldingRepository.save(holding2);

        // When: Repository 메서드 호출
        Optional<Member> foundMember = slaveMemberRepository.findMemberWithCoinHoldingsByUuid("test-uuid");

        System.out.println(foundMember);

    }
}