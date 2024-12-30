package com.mjy.exchange.repository;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import com.mjy.exchange.entity.QCoinHolding;
import com.mjy.exchange.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CoinHoldingRepository {
    private final JPAQueryFactory queryFactory;

    public CoinHoldingRepository(@Qualifier("masterJPAQueryFactory") JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<CoinHolding> findByMemberUuid(String memberUuid) {
        QCoinHolding coinHolding = QCoinHolding.coinHolding;

        return queryFactory.selectFrom(coinHolding)
                .join(coinHolding.member)
                .where(coinHolding.member.uuid.eq(memberUuid))
                .fetch();
    }
}
