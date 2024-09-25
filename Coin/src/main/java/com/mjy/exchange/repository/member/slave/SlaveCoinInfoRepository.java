package com.mjy.exchange.repository.member.slave;

import com.mjy.exchange.entity.member.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
