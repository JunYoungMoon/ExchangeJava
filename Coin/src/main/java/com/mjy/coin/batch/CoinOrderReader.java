//package com.mjy.coin.batch;
//
//import com.mjy.coin.dto.CoinOrderDTO;
//import com.mjy.coin.enums.OrderStatus;
//import com.mjy.coin.enums.OrderType;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.PreparedStatementSetter;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.util.Iterator;
//import java.util.List;
//
//
//@Component
//@StepScope
//public class CoinOrderReader implements ItemReader<CoinOrderDTO> {
//
//    private final JdbcTemplate jdbcTemplate;
//    private final Long chunkIdx;
//    private final String coinName;
//    private final LocalDate yesterday;
//    private Iterator<CoinOrderDTO> iterator;
//    private int readCount = 0; // 읽은 횟수
//    private final int maxReadCount = 10; // 최대 읽기 횟수
//
//    public CoinOrderReader(JdbcTemplate jdbcTemplate,
//                           @Value("#{stepExecutionContext['chunkIdx']}") Long chunkIdx,
//                           @Value("#{stepExecutionContext['coinName']}") String coinName,
//                           @Value("#{stepExecutionContext['yesterday']}") LocalDate yesterday) {
//        this.jdbcTemplate = jdbcTemplate;
//        this.chunkIdx = chunkIdx;
//        this.coinName = coinName;
//        this.yesterday = yesterday;
//    }
//
//
//    @Override
//    public CoinOrderDTO read() throws SQLException {
//        System.out.println(chunkIdx);
//        // 데이터가 없으면 null 반환
//        if (iterator == null || !iterator.hasNext()) {
//            System.out.println("1");
//
//            // 쿼리로 데이터를 한 번에 가져오기
//            String sql = "SELECT * FROM CoinOrder WHERE idx > ? AND DATE(matchedAt) = ? LIMIT 1000";
//            List<CoinOrderDTO> coinOrderList = jdbcTemplate.query(sql, new Object[]{chunkIdx, yesterday},
//                    new BeanPropertyRowMapper<>(CoinOrderDTO.class));
//
//            // Iterator로 데이터를 순차적으로 반환
//            iterator = coinOrderList.iterator();
//        }
//
//        // iterator에서 하나씩 반환
//        if (iterator.hasNext()) {
//            return iterator.next();
//        }
//
//        return null;  // 더 이상 데이터가 없으면 null 반환
////        System.out.println("CoinOrderReader.read" + chunkIdx);
////
////        if (readCount >= maxReadCount) {
////            return null; // 최대 횟수 도달 시 종료
////        }
////
////        readCount++; // 읽은 횟수 증가
////
////
////        CoinOrderDTO coinOrderDTO = new CoinOrderDTO();  // 매번 새 객체를 생성하여 반환
////
////        coinOrderDTO.setExecutionPrice(BigDecimal.valueOf(5000));
////        coinOrderDTO.setQuantity(BigDecimal.valueOf(10));
////
////        return coinOrderDTO;
//    }
//
//    private CoinOrderDTO mapRow(ResultSet rs) throws SQLException {
//        CoinOrderDTO coinOrderDTO = new CoinOrderDTO();  // 매번 새 객체를 생성하여 반환
//
//        coinOrderDTO.setIdx(rs.getLong("idx"));
//        coinOrderDTO.setMemberIdx(rs.getLong("memberIdx"));
//        coinOrderDTO.setMemberUuid(rs.getString("memberUuid"));
//        coinOrderDTO.setMarketName(rs.getString("marketName"));
//        coinOrderDTO.setCoinName(rs.getString("coinName"));
//        coinOrderDTO.setQuantity(rs.getBigDecimal("coinAmount"));
//        coinOrderDTO.setOrderPrice(rs.getBigDecimal("orderPrice"));
//        coinOrderDTO.setExecutionPrice(rs.getBigDecimal("executionPrice"));
//        coinOrderDTO.setOrderType(OrderType.valueOf(rs.getString("orderType"))); // Enum 매핑
//        coinOrderDTO.setOrderStatus(OrderStatus.valueOf(rs.getString("orderStatus"))); // Enum 매핑
//        coinOrderDTO.setFee(rs.getBigDecimal("fee"));
//        coinOrderDTO.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
//        coinOrderDTO.setMatchedAt(rs.getTimestamp("matchedAt") != null ? rs.getTimestamp("matchedAt").toLocalDateTime() : null);
//        coinOrderDTO.setMatchIdx(rs.getString("matchIdx"));
//        coinOrderDTO.setUuid(rs.getString("uuid"));
//
//        return coinOrderDTO;
//    }
//}