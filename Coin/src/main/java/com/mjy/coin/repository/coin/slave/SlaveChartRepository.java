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

//    WITH RankedOrders AS (
//            SELECT
//        -- 동적으로 시간 간격을 맞추기 위한 로직
//                DATE_FORMAT(TIMESTAMPADD(MINUTE,
//            FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / :interval_minutes) * :interval_minutes,
//            '1970-01-01 00:00:00'), '%Y-%m-%d %H:%i') AS time_interval,
//    matchedAt,  -- matchedAt을 선택
//    executionPrice,
//    coinAmount,
//    ROW_NUMBER() OVER (PARTITION BY
//    TIMESTAMPADD(MINUTE,
//                 FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / :interval_minutes) * :interval_minutes,
//            '1970-01-01 00:00:00') ORDER BY matchedAt ASC) AS rn_asc,
//    ROW_NUMBER() OVER (PARTITION BY
//    TIMESTAMPADD(MINUTE,
//                 FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / :interval_minutes) * :interval_minutes,
//            '1970-01-01 00:00:00') ORDER BY matchedAt DESC) AS rn_desc
//    FROM CoinOrder
//    WHERE matchedAt BETWEEN '2024-10-18 00:00:00' AND '2024-10-20 23:59:59'
//    AND orderStatus = 'COMPLETED'
//)
//    SELECT
//            time_interval,
//    COUNT(*) AS order_count,
//    SUM(executionPrice * coinAmount) AS total_traded_value,
//    SUM(coinAmount) AS total_volume,
//    -- 시작가: rn_asc = 1인 레코드의 executionPrice
//    MAX(CASE WHEN rn_asc = 1 THEN executionPrice END) AS opening_price,
//    -- 종가: rn_desc = 1인 레코드의 executionPrice
//    MAX(CASE WHEN rn_desc = 1 THEN executionPrice END) AS closing_price,
//    -- 고가: 해당 시간 간격에서의 executionPrice 최대값
//    MAX(executionPrice) AS high_price,
//    -- 저가: 해당 시간 간격에서의 executionPrice 최소값
//    MIN(executionPrice) AS low_price,
//    -- 첫 번째 matchedAt의 유닉스 타임스탬프
//    MIN(CASE WHEN rn_asc = 1 THEN UNIX_TIMESTAMP(matchedAt) END) AS first_matched_at_unix,
//    -- 마지막 matchedAt의 유닉스 타임스탬프
//    MAX(CASE WHEN rn_desc = 1 THEN UNIX_TIMESTAMP(matchedAt) END) AS last_matched_at_unix
//    FROM RankedOrders
//    GROUP BY time_interval
//    ORDER BY time_interval;

}
