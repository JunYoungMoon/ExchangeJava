package com.mjy.coin.repository.coin.slave;

import com.mjy.coin.entity.coin.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaveCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
    // 미체결 상태의 주문만 가져오기
    @Query("SELECT o FROM CoinOrder o WHERE o.orderStatus = 'PENDING'")
    List<CoinOrder> findPendingOrders();
}
