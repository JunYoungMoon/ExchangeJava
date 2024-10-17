package com.mjy.coin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
        String sql = "SELECT MIN(idx), MAX(idx) FROM CoinOrder WHERE matchedAt BETWEEN ? AND ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Long[]{
                rs.getLong(1), // MIN(idx)
                rs.getLong(2)  // MAX(idx)
        }, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
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
        String sql = "SELECT COUNT(*) FROM CoinOrderDayHistory WHERE tradingDate = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date);
        return count != null && count > 0;
    }
}
