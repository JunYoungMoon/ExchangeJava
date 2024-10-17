package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@StepScope
public class CoinOrderReader implements ItemReader<CoinOrderDTO> {

    private final JdbcTemplate jdbcTemplate;
    private List<CoinOrderDTO> coinOrders;
    private int nextIndex = 0;
    private final Long minIdx;
    private final Long maxIdx;

    public CoinOrderReader(JdbcTemplate jdbcTemplate,
                           @Value("#{stepExecutionContext['minIdx']}") Long minIdx,
                           @Value("#{stepExecutionContext['maxIdx']}") Long maxIdx) {
        this.jdbcTemplate = jdbcTemplate;
        this.minIdx = minIdx;
        this.maxIdx = maxIdx;
    }

    @Override
    public CoinOrderDTO read(){
        // 처음 호출 시 데이터베이스에서 데이터를 가져온다
        if (coinOrders == null) {
            String sql = "SELECT * FROM CoinOrder WHERE idx >= ? AND idx <= ? ORDER BY idx";

            RowMapper<CoinOrderDTO> rowMapper = new BeanPropertyRowMapper<>(CoinOrderDTO.class);

            coinOrders = jdbcTemplate.query(sql, rowMapper, minIdx, maxIdx);
        }

        if (nextIndex < coinOrders.size()) {
            return coinOrders.get(nextIndex++);
        } else {
            return null;
        }
    }
}