package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class CoinOrderServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CoinOrderService coinOrderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMinMaxIdx() {
        // 주어진 날짜
        LocalDate today = LocalDate.of(2024, 10, 16);

        // 모킹된 결과: MIN(idx) = 1, MAX(idx) = 100
        Long[] expected = new Long[]{1L, 100L};

        // SQL 쿼리와 파라미터에 맞춘 모킹 설정
        String sql = "SELECT MIN(idx), MAX(idx) FROM CoinOrder WHERE matchedAt BETWEEN ? AND ?";
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        when(jdbcTemplate.queryForObject(eq(sql), any(RowMapper.class), eq(startOfDay), eq(endOfDay)))
                .thenReturn(expected);

        // 실제 메서드 호출
        Long[] result = coinOrderService.getMinMaxIdx(today);

        // 결과 검증
        assertArrayEquals(expected, result);
    }

    @Test
    public void testPartitionChunks() {
        // given
        Long minIdx = 1L;
        Long maxIdx = 10L;
        int chunkSize = 3;

        // 예상되는 결과값을 미리 정의
        List<Map<String, Long>> expectedPartitions = List.of(
                Map.of("minIdx", 1L, "maxIdx", 3L),
                Map.of("minIdx", 4L, "maxIdx", 6L),
                Map.of("minIdx", 7L, "maxIdx", 9L),
                Map.of("minIdx", 10L, "maxIdx", 10L)
        );

        // when
        List<Map<String, Long>> actualPartitions = coinOrderService.partitionChunks(minIdx, maxIdx, chunkSize);

        // then
        assertEquals(expectedPartitions.size(), actualPartitions.size(), "Partition size should match");
        for (int i = 0; i < expectedPartitions.size(); i++) {
            assertEquals(expectedPartitions.get(i), actualPartitions.get(i), "Each partition should match expected");
        }
    }

    @Test
    public void BigDecimalTest(){
        BigDecimal a = BigDecimal.valueOf(3);
        BigDecimal b = BigDecimal.valueOf(6);

        System.out.println(a.subtract(b).compareTo(BigDecimal.ZERO) > 0);
    }
}