package com.mjy.coin.repository.master;

import com.mjy.coin.entity.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
}
