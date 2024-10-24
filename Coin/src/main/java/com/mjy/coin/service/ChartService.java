package com.mjy.coin.service;

import com.mjy.coin.dto.CandleDTO;
import com.mjy.coin.dto.ChartDataRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChartService {

    private JdbcTemplate jdbcTemplate;


    public List<CandleDTO[]> getChartData(ChartDataRequest chartDataRequest) {

        String symbol = chartDataRequest.getSymbol();
        String[] symbolArray = symbol.split("_");
        String coinName = symbolArray[0].toLowerCase();
        String marketName = symbolArray[1].toLowerCase();

        String resolution = chartDataRequest.getResolution();
        long fromTimestamp = chartDataRequest.getFrom();
        long toTimestamp = chartDataRequest.getTo();

        String minutes = convertToMinutes(resolution);

        String fromDate = convertTimestampToDate(fromTimestamp);
        String toDate = convertTimestampToDate(toTimestamp);

        return null;
    }

    private String convertToMinutes(String resolution) {
        return switch (resolution) {
            case "1", "3", "5", "15", "30", "60" -> resolution; // 기존 분 단위
            case "d", "1d" -> "1440"; // 1일
            case "2d" -> "2880"; // 2일
            case "3d" -> "4320"; // 3일
            case "w", "1w" -> "10080"; // 1주
            case "3w" -> "30240"; // 3주
            case "m", "1m" -> "43200"; // 1개월
            default -> throw new IllegalArgumentException("Invalid interval: " + resolution);
        };
    }

    // 타임스탬프를 날짜로 변환하는 함수
    private String convertTimestampToDate(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
