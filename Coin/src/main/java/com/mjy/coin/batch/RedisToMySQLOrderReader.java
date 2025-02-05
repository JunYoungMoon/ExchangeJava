//package com.mjy.coin.batch;
//
//import com.mjy.coin.service.RedisService;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.Cursor;
//import org.springframework.stereotype.Component;
//
//import java.util.AbstractMap;
//import java.util.Map;
//
//@Component
//@StepScope
//public class RedisToMySQLOrderReader implements ItemReader<Map.Entry<String, String>> {
//
//    private final RedisService redisService;
//    private final String redisKey;
//    private Cursor<Map.Entry<String, String>> cursor;
//
//    public RedisToMySQLOrderReader(RedisService redisService, @Value("#{jobParameters['redisKey']}") String redisKey) {
//        this.redisService = redisService;
//        this.redisKey = redisKey;
//    }
//
//    @Override
//    public Map.Entry<String, String> read(){
//        if (cursor == null) {
//            // Cursor가 null인 경우, 새로운 Cursor를 생성하여 스캔 시작
//            cursor = redisService.scanCursor("COMPLETED:ORDER:" + redisKey);
//        }
//
//        if (cursor.hasNext()) {
//            Map.Entry<String, String> entry = cursor.next();
//            return new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
//        } else {
//            // Cursor가 끝에 도달하면 종료 처리
//            cursor.close(); // Cursor 리소스 정리
//            cursor = null;  // Cursor 상태 초기화
//            return null; // 더 이상 데이터가 없음을 알림
//        }
//    }
//}
