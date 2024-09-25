package com.mjy.exchange.repository.shard2.master;

import com.mjy.exchange.entity.shard1.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
}
