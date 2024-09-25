package com.mjy.coin.repository.exchange.slave;

import com.mjy.coin.entity.exchange.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
