package com.mjy.coin.service;

import com.mjy.coin.dto.CandleDTO;
import com.mjy.coin.dto.ChartDataRequest;
import com.mjy.coin.repository.coin.slave.SlaveChartRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChartService {
    private final SlaveChartRepository slaveChartRepository;

    public ChartService(SlaveChartRepository slaveChartRepository) {
        this.slaveChartRepository = slaveChartRepository;
    }

    public List<CandleDTO[]> getChartData(ChartDataRequest chartDataRequest) {

        String symbol = chartDataRequest.getSymbol();
        String[] symbolArray = symbol.split("_");
        String coinName = symbolArray[0].toLowerCase();
        String marketName = symbolArray[1].toLowerCase();

        String interval = chartDataRequest.getInterval();
        long fromTimestamp = chartDataRequest.getFrom();
        long toTimestamp = chartDataRequest.getTo();

        String intervalSql = getIntervalSql(interval);

        // UNIX timestamp to readable date format
        String fromDate = convertTimestampToDate(fromTimestamp);
        String toDate = convertTimestampToDate(toTimestamp);

        return slaveChartRepository.getCandleData(coinName, marketName, fromDate, toDate, intervalSql);
    }

    // interval에 따른 SQL을 반환하는 함수
    private String getIntervalSql(String interval) {
        return switch (interval) {
            case "1" -> "UNIX_TIMESTAMP(c.matchedAt)/60";
            case "3" -> "UNIX_TIMESTAMP(c.matchedAt)/60/3";
            case "5" -> "UNIX_TIMESTAMP(c.matchedAt)/60/5";
            case "15" -> "UNIX_TIMESTAMP(c.matchedAt)/60/15";
            case "30" -> "UNIX_TIMESTAMP(c.matchedAt)/60/30";
            case "60" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60";
            case "d", "1d" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/24";
            case "2d" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/(24*2)";
            case "3d" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/(24*3)";
            case "w", "1w" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/(24*7)";
            case "3w" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/(24*21)";
            case "m", "1m" -> "UNIX_TIMESTAMP(c.matchedAt)/60/60/(24*30)";
            default -> throw new IllegalArgumentException("Invalid interval: " + interval);
        };
    }

    // 타임스탬프를 날짜로 변환하는 함수
    private String convertTimestampToDate(long timestamp) {
        return Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
