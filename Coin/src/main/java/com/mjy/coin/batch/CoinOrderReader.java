package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;


@Component
@StepScope
public class CoinOrderReader implements ItemReader<CoinOrderDTO> {

    private final JdbcTemplate jdbcTemplate;
    private List<CoinOrderDTO> coinOrders;
    private int nextIndex = 0;
    private final Long chunkIdx;
    private final String coinName;
    private final LocalDate yesterday;

    public CoinOrderReader(JdbcTemplate jdbcTemplate,
                           @Value("#{stepExecutionContext['chunkIdx']}") Long chunkIdx,
                           @Value("#{stepExecutionContext['coinName']}") String coinName,
                           @Value("#{stepExecutionContext['yesterday']}") LocalDate yesterday) {
        this.jdbcTemplate = jdbcTemplate;
        this.chunkIdx = chunkIdx;
        this.coinName = coinName;
        this.yesterday = yesterday;
    }

    @Override
    public CoinOrderDTO read() {
        String sql = """
                    SELECT * FROM CoinOrder
                    WHERE coinName = ?
                      AND idx >= ?
                      AND DATE(matchedAt) = ?
                    LIMIT 0, 1000;
                """;

        RowMapper<CoinOrderDTO> rowMapper = new BeanPropertyRowMapper<>(CoinOrderDTO.class);

        coinOrders = jdbcTemplate.query(sql, rowMapper, coinName, chunkIdx, yesterday);
        if (nextIndex < coinOrders.size()) {
            return coinOrders.get(nextIndex++);
        } else {
            return null;
        }
    }
}