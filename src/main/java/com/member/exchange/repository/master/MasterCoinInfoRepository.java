package com.member.exchange.repository.master;

import com.member.exchange.entity.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
