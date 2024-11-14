package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
    Optional<CoinInfo> findByMarketNameAndCoinName(String MarketName, String CoinName);
}
