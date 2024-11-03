package com.mjy.websocket.repository.exchange.slave;

import com.mjy.coin.entity.exchange.CoinInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaveCoinInfoRepository extends JpaRepository<CoinInfo, Long> {
    List<CoinInfo> findByCoinType(CoinInfo.CoinType coinType);
}
