package com.mjy.member.repository.slave;

import com.mjy.member.entity.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
}
