//package com.mjy.coin.repository.coin.slave;
//
//import com.mjy.coin.dto.CandleDTO;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class SlaveChartRepository {
//
//    private final JdbcTemplate slaveJdbcTemplate;
//
//    public SlaveChartRepository(@Qualifier("slaveJdbcTemplate") JdbcTemplate slaveJdbcTemplate) {
//        this.slaveJdbcTemplate = slaveJdbcTemplate;
//    }
//
//    public List<CandleDTO> getChartData(String coinName, String marketName, long fromTimestamp, long toTimestamp, String minutes) {
//        String sql = """
//            WITH RankedOrders AS (
//                SELECT
//                    DATE_FORMAT(TIMESTAMPADD(MINUTE,
//                        FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / ? ) * ?,
//                        '1970-01-01 00:00:00'), '%Y-%m-%d %H:%i') AS time_interval,
//                    matchedAt,
//                    executionPrice,
//                    coinAmount,
//                    ROW_NUMBER() OVER (PARTITION BY
//                        TIMESTAMPADD(MINUTE,
//                            FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / ?) * ?,
//                            '1970-01-01 00:00:00') ORDER BY matchedAt ASC) AS rn_asc,
//                    ROW_NUMBER() OVER (PARTITION BY
//                        TIMESTAMPADD(MINUTE,
//                            FLOOR(TIMESTAMPDIFF(MINUTE, '1970-01-01 00:00:00', matchedAt) / ?) * ?,
//                            '1970-01-01 00:00:00') ORDER BY matchedAt DESC) AS rn_desc
//                FROM CoinOrder
//                WHERE matchedAt BETWEEN FROM_UNIXTIME(?)
//                AND FROM_UNIXTIME(?)
//                AND orderStatus = 'COMPLETED'
//                AND coinName = ?
//                AND marketName = ?
//            )
//            SELECT
//                time_interval,
//                COUNT(*) AS order_count,
//                SUM(executionPrice * coinAmount) AS total_traded_value,
//                SUM(coinAmount) AS total_volume,
//                MAX(CASE WHEN rn_asc = 1 THEN executionPrice END) AS opening_price,
//                MAX(CASE WHEN rn_desc = 1 THEN executionPrice END) AS closing_price,
//                MAX(executionPrice) AS high_price,
//                MIN(executionPrice) AS low_price,
//                MIN(CASE WHEN rn_asc = 1 THEN UNIX_TIMESTAMP(matchedAt) END) AS first_matched_at_unix,
//                MAX(CASE WHEN rn_desc = 1 THEN UNIX_TIMESTAMP(matchedAt) END) AS last_matched_at_unix
//            FROM RankedOrders
//            GROUP BY time_interval
//            ORDER BY time_interval
//            """;
//
//        return slaveJdbcTemplate.query(sql, new Object[]{minutes, minutes, minutes, minutes, minutes, minutes, fromTimestamp, toTimestamp, coinName, marketName},
//                new BeanPropertyRowMapper<>(CandleDTO.class));
//    }
//}
