package com.mjy.exchange.repository.shard1.slave;

import com.mjy.exchange.entity.shard1.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
}
