package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


@Component
@StepScope
public class CoinOrderReader implements ItemReader<CoinOrderDTO> {

    private final JdbcTemplate jdbcTemplate;
    private final Long chunkIdx;
    private final String coinName;
    private final LocalDate yesterday;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private boolean initialized = false;


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
    public CoinOrderDTO read() throws SQLException {
//        if (!initialized) {
//            String sql = """
//                SELECT * FROM CoinOrder
//                WHERE coinName = ?
//                  AND idx >= ?
//                  AND DATE(matchedAt) = ?
//            """;
//
//            Connection connection = jdbcTemplate.getDataSource().getConnection();
//            preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//
//            preparedStatement.setString(1, coinName);
//            preparedStatement.setLong(2, chunkIdx);
//            preparedStatement.setObject(3, yesterday);
//
//            resultSet = preparedStatement.executeQuery();
//            initialized = true;
//        }
//
//        // 결과를 순차적으로 읽어옴
//        if (resultSet.next()) {
//            return mapRow(resultSet); // 매번 새 객체로 반환
//        } else {
//            resultSet.close();
//            preparedStatement.close();
//            return null;
//        }

        CoinOrderDTO coinOrderDTO = new CoinOrderDTO();  // 매번 새 객체를 생성하여 반환

        coinOrderDTO.setExecutionPrice(BigDecimal.valueOf(5000));
        coinOrderDTO.setCoinAmount(BigDecimal.valueOf(10));

        return coinOrderDTO;
    }

    private CoinOrderDTO mapRow(ResultSet rs) throws SQLException {
        CoinOrderDTO coinOrderDTO = new CoinOrderDTO();  // 매번 새 객체를 생성하여 반환

        coinOrderDTO.setIdx(rs.getLong("idx"));
        coinOrderDTO.setMemberIdx(rs.getLong("memberIdx"));
        coinOrderDTO.setMemberUuid(rs.getString("memberUuid"));
        coinOrderDTO.setMarketName(rs.getString("marketName"));
        coinOrderDTO.setCoinName(rs.getString("coinName"));
        coinOrderDTO.setCoinAmount(rs.getBigDecimal("coinAmount"));
        coinOrderDTO.setOrderPrice(rs.getBigDecimal("orderPrice"));
        coinOrderDTO.setExecutionPrice(rs.getBigDecimal("executionPrice"));
        coinOrderDTO.setOrderType(OrderType.valueOf(rs.getString("orderType"))); // Enum 매핑
        coinOrderDTO.setOrderStatus(OrderStatus.valueOf(rs.getString("orderStatus"))); // Enum 매핑
        coinOrderDTO.setFee(rs.getBigDecimal("fee"));
        coinOrderDTO.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        coinOrderDTO.setMatchedAt(rs.getTimestamp("matchedAt") != null ? rs.getTimestamp("matchedAt").toLocalDateTime() : null);
        coinOrderDTO.setMatchIdx(rs.getString("matchIdx"));
        coinOrderDTO.setUuid(rs.getString("uuid"));

        return coinOrderDTO;
    }
}