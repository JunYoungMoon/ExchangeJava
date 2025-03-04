package com.mjy.coin.repository.coin.slave;

import com.mjy.coin.entity.coin.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlaveCoinOrderRepository extends JpaRepository<CoinOrder, Long> {
    // 미체결 상태의 주문만 가져오기
    @Query("SELECT o FROM CoinOrder o WHERE o.orderStatus = 'PENDING'")
    List<CoinOrder> findPendingOrders();

    @Query("SELECT 1 FROM CoinOrder o WHERE o.marketName = :marketName and o.coinName = :coinName and o.createdAt = :createdAt")
    Optional<Integer> findByMarketNameAndCoinNameAndCreatedAt(
            @Param("marketName") String marketName, @Param("coinName") String coinName, @Param("createdAt") LocalDateTime createdAt);

    Optional<CoinOrder> findByUuid(
            String uuid);

    List<CoinOrder> findAllByUuidIn(List<String> uuids);
}
