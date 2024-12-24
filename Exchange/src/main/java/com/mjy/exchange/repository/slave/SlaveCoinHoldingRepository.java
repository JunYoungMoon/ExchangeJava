package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.CoinHolding;
import com.mjy.exchange.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlaveCoinHoldingRepository extends JpaRepository<CoinHolding, Long> {
    Optional<CoinHolding> findByMemberUuidAndCoinType(String memberUuid, String coinType);
    List<CoinHolding> findByMember(Member member);
}
