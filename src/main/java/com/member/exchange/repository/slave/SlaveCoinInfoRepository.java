package com.member.exchange.repository.slave;

import com.member.exchange.entity.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
