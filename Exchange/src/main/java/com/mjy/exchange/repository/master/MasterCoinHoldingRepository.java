package com.mjy.exchange.repository.master;

import com.mjy.exchange.entity.CoinHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinHoldingRepository extends JpaRepository<CoinHolding, Long> {
}
