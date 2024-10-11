package com.mjy.coin.batch;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CompletedOrderReader implements ItemReader<CoinOrderDTO> {

    private final RedisService redisService;  // RedisService 주입
    private final CoinInfoService coinInfoService;
    private List<String> uuidList;  // 파티션에서 전달받을 UUID 리스트
    private String marketKey;       // 파티션에서 전달받을 마켓 키
    private int currentIndex = 0;   // 현재 처리 중인 UUID 인덱스

    @Autowired
    public CompletedOrderReader(RedisService redisService, CoinInfoService coinInfoService) {
        this.redisService = redisService;
        this.coinInfoService = coinInfoService;
    }

    @Override
    public CoinOrderDTO read() {
        // 콘솔에 로그 출력: 어떤 스레드에서 어떤 UUID와 마켓 키를 처리하고 있는지 기록
        System.out.println("Thread: " + Thread.currentThread().getName() +
                " | Processing marketKey: " + marketKey +
                " | UUID List size: " + (uuidList != null ? uuidList.size() : 0));

        // ExecutionContext에서 uuidList와 marketKey를 가져옴
        StepContext stepContext = StepSynchronizationManager.getContext();
        if (stepContext != null && stepContext.getStepExecution() != null) {
            uuidList = (List<String>) stepContext.getStepExecution().getExecutionContext().get("uuidList");
            marketKey = (String) stepContext.getStepExecution().getExecutionContext().get("marketKey");
        }

        // UUID 리스트가 없거나 모두 처리한 경우 null 반환
        if (uuidList == null || currentIndex >= uuidList.size()) {
            return null;
        }

        // 현재 UUID 가져오기
        String uuid = uuidList.get(currentIndex);
        currentIndex++;

        // Redis에서 해당 UUID의 주문 데이터 가져오기
        String orderData = redisService.getHashOps(marketKey, uuid);  // marketKey를 사용하여 Redis에서 조회

        // orderData가 null이거나 빈 값인 경우 변환을 시도하지 않고 null 반환
        if (orderData == null || orderData.isEmpty()) {
            System.out.println("Thread: " + Thread.currentThread().getName() + " | Invalid data for UUID: " + uuid);
            return null;
        }

        try {
            // CoinOrderDTO로 변환 후 반환
            return redisService.convertStringToObject(orderData, CoinOrderDTO.class);
        } catch (Exception e) {
            System.out.println("Thread: " + Thread.currentThread().getName() + " | Error converting data for UUID: " + uuid + " | Data: " + orderData);
            e.printStackTrace(); // 에러 출력
            return null;
        }
    }
}