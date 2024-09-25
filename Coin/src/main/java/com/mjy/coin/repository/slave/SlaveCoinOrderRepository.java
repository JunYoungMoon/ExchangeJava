package com.mjy.coin.repository.slave;

import com.mjy.coin.entity.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaveCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
}
