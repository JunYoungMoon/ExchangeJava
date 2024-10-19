package com.mjy.coin.repository.coin.master;

import com.mjy.coin.entity.coin.CoinOrderDayHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinOrderDayHistoryRepository extends JpaRepository<CoinOrderDayHistory, Long> {
}
