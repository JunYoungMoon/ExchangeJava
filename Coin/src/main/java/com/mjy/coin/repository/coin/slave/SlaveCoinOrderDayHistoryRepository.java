package com.mjy.coin.repository.coin.slave;

import com.mjy.coin.entity.coin.CoinOrderDayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinOrderDayHistoryRepository extends JpaRepository<CoinOrderDayHistory, Long> {

}
