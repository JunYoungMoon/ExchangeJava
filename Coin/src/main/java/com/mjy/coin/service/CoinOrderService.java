package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderSimpleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoinOrderService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CoinOrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long[] getMinMaxIdx(LocalDate today) {
        String sql = """
                SELECT MIN(idx), MAX(idx) 
                FROM CoinOrder 
                WHERE matchedAt BETWEEN ? AND ?
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Long[]{
                rs.getLong(1), // MIN(idx)
                rs.getLong(2)  // MAX(idx)
        }, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public BigDecimal getLatestExecutionPriceByDate(String coinName, LocalDate matchedDate){
        String sql = """
                SELECT executionPrice
                FROM CoinOrder
                WHERE coinName = ?
                  AND date(matchedAt) = ?
                ORDER BY matchedAt DESC
                LIMIT 1
                """;

        return jdbcTemplate.queryForObject(
                sql,
                new Object[]{coinName, matchedDate},
                BigDecimal.class
        );
    }

    public List<CoinOrderSimpleDTO> getCoinOrderChunksBy1000(String coinName, String marketName, LocalDate matchedDate) {
        String sql = """
                WITH OrderedCoinOrders AS (
                    SELECT idx,
                           coinName,
                           matchedAt,
                           ROW_NUMBER() OVER (ORDER BY idx) AS row_num
                    FROM CoinOrder
                    WHERE coinName = ? 
                      AND marketName = ?  
                      AND DATE(matchedAt) = ? 
                )
                SELECT idx, coinName, matchedAt
                FROM OrderedCoinOrders
                WHERE row_num % 1000 = 1;
                """;

        RowMapper<CoinOrderSimpleDTO> rowMapper = new BeanPropertyRowMapper<>(CoinOrderSimpleDTO.class);

        return jdbcTemplate.query(sql, rowMapper, coinName, marketName, matchedDate);
    }

    public List<Map<String, Long>> partitionChunks(Long minIdx, Long maxIdx, int chunkSize) {
        List<Map<String, Long>> partitions = new ArrayList<>();
        long start = minIdx;
        while (start <= maxIdx) {
            long end = Math.min(start + chunkSize - 1, maxIdx); // 각 파티션의 최대 idx
            Map<String, Long> partition = new HashMap<>();
            partition.put("minIdx", start);
            partition.put("maxIdx", end);
            partitions.add(partition);
            start = end + 1;
        }
        return partitions;
    }

    public boolean hasClosingPriceForDate(LocalDate date) {
        String sql = """
                SELECT COUNT(*) 
                FROM CoinOrderDayHistory 
                WHERE tradingDate = ?
                """;

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date);
        return count != null && count > 0;
    }
}
