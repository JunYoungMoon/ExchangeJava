package com.mjy.exchange.repository;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CoinHoldingRepositoryTest {

    @Autowired
    private CoinHoldingRepository coinHoldingRepository;


    @Test
    public void testFindMembersByEmail() {
        // 이메일로 회원 검색
        List<CoinHolding> coinHoldings = coinHoldingRepository.findCoinHoldingsByMemberUuid("cfccbb28-f07d-4e7c-8bd2-4cbd720aceab");

        // 결과 확인
        assertEquals(3, coinHoldings.size());
        assertEquals("BTC", coinHoldings.get(0).getCoinType());
        assertEquals("ETH", coinHoldings.get(1).getCoinType());
        assertEquals("KRW", coinHoldings.get(2).getCoinType());
    }
}