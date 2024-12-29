package com.mjy.exchange.repository;

import com.mjy.exchange.entity.Member;
import com.mjy.exchange.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class MemberRepository {
    private final JPAQueryFactory queryFactory;

    public MemberRepository(@Qualifier("masterJPAQueryFactory") JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Member> findMembersByEmail(String email) {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member)
                .where(member.email.eq(email))
                .fetch();
    }
}
