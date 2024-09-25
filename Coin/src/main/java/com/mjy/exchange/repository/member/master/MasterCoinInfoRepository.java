package com.mjy.exchange.repository.member.master;

import com.mjy.exchange.entity.member.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
