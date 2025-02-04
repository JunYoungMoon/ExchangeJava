package com.mjy.exchange.etc;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class NPlusOneQueryTest {

    @Autowired
    private EntityManager entityManager;

    // Eager Loading, Lazy Loading 문제 발생 테스트 (N+1 문제)
    @Test
    @Transactional
    public void testEagerLoading() {
//        // Eager 로딩으로 조회 (Member 조회 시 CoinHolding도 같이 로딩)
//        TypedQuery<CoinHolding> query = entityManager.createQuery("SELECT m FROM CoinHolding m", CoinHolding.class);
//        List<CoinHolding> coinHoldings = query.getResultList();
//
//        // 각 Member에 대해 coinHoldings가 eager로 로딩되는지 확인
//        for (CoinHolding coinHolding : coinHoldings) {
//            System.out.println("Member UUID: " + coinHolding.getUuid() + ", Coin Holders: " + member.getCoinHoldings().size());
//        }
//
//        // 결과 확인 (성공적으로 Eager 로딩이 되었다면 CoinHolding 목록을 확인할 수 있음)
//        assertEquals(3, members.size()); // 예시: members 목록에 3명이 있다고 가정
    }

    // 3. N+1 문제 해결을 위한 Fetch Join 사용
    @Test
    @Transactional
    public void testJoinFetch() {
        // Join Fetch를 사용하여 한 번의 쿼리로 Member와 CoinHolding을 함께 조회
//        TypedQuery<Member> query = entityManager.createQuery("SELECT m FROM Member m JOIN FETCH m.coinHoldings", Member.class);
//        List<Member> members = query.getResultList();
//
//        // 각 Member에 대해 coinHoldings가 제대로 로딩되었는지 확인
//        for (Member member : members) {
//            System.out.println("Member UUID: " + member.getUuid() + ", Coin Holders Count: " + member.getCoinHoldings().size());
//        }
//
//        // 결과 확인 (성공적으로 Fetch Join이 되었다면, 쿼리 수는 1로 제한됨)
//        assertEquals(3, members.size()); // 예시: members 목록에 3명이 있다고 가정
    }
}
