package com.mjy.exchange.repository;

import com.mjy.exchange.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;


    @Test
    public void testFindMembersByEmail() {
        // 이메일로 회원 검색
        List<Member> members = memberRepository.findMembersByEmail("test@naver.com");

        // 결과 확인
        assertEquals(1, members.size());  // 하나의 회원만 있어야 함
        assertEquals("test@example.com", members.get(0).getEmail());
    }
}