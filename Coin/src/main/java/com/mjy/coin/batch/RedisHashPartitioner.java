package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.redis.core.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisHashPartitioner implements Partitioner {

    private final RedisService redisService;  // RedisService 주입
    private final List<String> marketKeys;     // 동적으로 가져온 마켓 키 리스트

    public RedisHashPartitioner(RedisService redisService, List<String> marketKeys) {
        this.redisService = redisService;
        this.marketKeys = marketKeys;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();

//        // 각 마켓 키에 대한 UUID 리스트를 저장할 리스트
//        List<String> allUuids = new ArrayList<>();
//
//        // 각 마켓 키에 대해 UUID 가져오기
//        for (String marketKey : marketKeys) {
//            try (Cursor<Map.Entry<String, String>> cursor = redisService.scanCursor(marketKey)) {
//                while (cursor.hasNext()) {
//                    Map.Entry<String, String> entry = cursor.next();
//
//                    CoinOrderDTO order = redisService.convertStringToObject(entry.getValue(), CoinOrderDTO.class);
//
//                    if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus().name())) {
//                        allUuids.add(entry.getKey());  // UUID 추가
//                    }
//                }
//            }
//        }
//
//        if (allUuids.isEmpty()) {
//            return result;  // 빈 Map 반환 -> 배치 작업이 실행되지 않음
//        }
//
//        // UUID를 gridSize 만큼 분할
//        int partitionSize = allUuids.size() / gridSize;
//        int currentIndex = 0;
//
//        for (int i = 0; i < gridSize; i++) {
//            ExecutionContext context = new ExecutionContext();
//
//            // 마지막 파티션은 나머지 모든 항목 처리
//            int endIndex = (i == gridSize - 1) ? allUuids.size() : currentIndex + partitionSize;
//            List<String> partitionedUuids = new ArrayList<>(allUuids.subList(currentIndex, endIndex));
//
//            // 각 파티션에 마켓 키를 추가
//            context.put("marketKey", marketKeys.get(i % marketKeys.size()));  // 라운드 로빈 방식으로 마켓 키 설정
//            context.put("uuidList", partitionedUuids);
//
//            result.put("partition" + i, context);
//
//            currentIndex = endIndex;
//        }

        return result;
    }
}
