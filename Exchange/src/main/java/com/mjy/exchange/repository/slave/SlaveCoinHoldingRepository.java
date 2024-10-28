package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.CoinHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinHoldingRepository extends JpaRepository<CoinHolding, Long> {
}
