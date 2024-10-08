package com.mjy.coin.repository.coin.master;

import com.mjy.coin.entity.coin.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterChartRepository extends JpaRepository<CoinOrder, Long> {
}
