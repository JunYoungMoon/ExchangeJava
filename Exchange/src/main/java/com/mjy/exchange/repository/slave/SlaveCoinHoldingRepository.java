package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.CoinHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaveCoinHoldingRepository extends JpaRepository<CoinHolding, Long> {
    Optional<CoinHolding> findByMemberIdxAndCoinType(Long memberIdx, String coinType);
}
