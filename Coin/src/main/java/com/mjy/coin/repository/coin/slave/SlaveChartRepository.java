package com.mjy.coin.repository.coin.slave;

import com.mjy.coin.dto.CandleDTO;
import com.mjy.coin.entity.coin.CoinOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlaveChartRepository extends JpaRepository<CoinOrder, Long> {
    @Query(value = "SELECT " +
            "SUBSTRING_INDEX(GROUP_CONCAT(c.executionPrice ORDER BY c.matchedAt, c.id), ',', 1) AS openPrice, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(c.executionPrice ORDER BY c.matchedAt, c.id), ',', -1) AS closePrice, " +
            "MAX(c.executionPrice) AS highPrice, " +
            "MIN(c.executionPrice) AS lowPrice, " +
            "SUM(c.coinAmount * c.executionPrice) AS volume, " +
            "FLOOR(UNIX_TIMESTAMP(c.matchedAt)) AS timestamp " +
            "FROM CoinOrder c " +
            "WHERE c.orderStatus = 'COMPLETED' " +
            "AND c.coinName = :coinName " +
            "AND c.marketName = :marketName " +
            "AND c.matchedAt BETWEEN :fromDate AND :toDate " +
            "GROUP BY FLOOR(:intervalSql)",
            nativeQuery = true)
    List<CandleDTO[]> getCandleData(
            @Param("coinName") String coinName,
            @Param("marketName") String marketName,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate,
            @Param("intervalSql") String intervalSql
    );
}
