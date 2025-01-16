package com.mjy.coin.service;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mjy.coin.enums.OrderStatus.COMPLETED;
import static com.mjy.coin.enums.OrderStatus.PENDING;
import static com.mjy.coin.enums.OrderType.BUY;
import static com.mjy.coin.enums.OrderType.SELL;

@Component
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConvertService convertService;

    public RedisService(RedisTemplate<String, Object> redisTemplate, ConvertService convertService) {
        this.redisTemplate = redisTemplate;
        this.convertService = convertService;
    }

    public void setValues(String key, String data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();

        if (values.get(key) == null) {
            return "false";
        }
        return (String) values.get(key);
    }

    public Set<String> getKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public void expireValues(String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    public void setHashOps(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    // Hash 생성 후 Key-Value 값 삽입
    public void createHashAndSetValues(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        // Redis에 Hash와 값을 넣기
        hashOps.putAll(key, data);
    }

    // HSET을 사용하여 특정 필드의 값을 변경
    public void updateHashField(String key, String hashKey, String value) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        // Hash의 특정 필드 값 업데이트
        hashOps.put(key, hashKey, value);
    }

    public void incrementHashValue(String key, String hashKey, Double incrementBy, Duration ttlDuration) {
        HashOperations<String, Object, Double> values = redisTemplate.opsForHash();
        values.increment(key, hashKey, incrementBy);

        redisTemplate.expire(key, ttlDuration);
    }

    public String getHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String) redisTemplate.opsForHash().get(key, hashKey) : "";
    }

    public Map<String, String> getAllHashOps(String key) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        Map<Object, Object> entries = values.entries(key);

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put((String) entry.getKey(), (String) entry.getValue());
        }

        return result;
    }

    public void deleteHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    public boolean checkExistsValue(String value) {
        return !value.equals("false");
    }

    public Cursor<Map.Entry<String, String>> scanCursor(String key) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        // Redis의 스캔 기능 사용 (Cursor 생성)
        return hashOps.scan(key, ScanOptions.NONE);
    }

    public void updateOrderInRedis(CoinOrderDTO order) {
        try {
            // 1. Redis에서 기존 데이터 가져오기
            String orderData = getHashOps("PENDING:ORDER:" + order.getCoinName() + "-" + order.getMarketName(), order.getUuid());

            // 2. String 데이터를 Map으로 변환
            Map<String, String> orderDataMap = convertService.convertStringToMap(orderData);

            // 3. 필요한 데이터 업데이트
            orderDataMap.put("orderStatus", String.valueOf(order.getOrderStatus()));
            orderDataMap.put("matchedAt", String.valueOf(order.getMatchedAt()));
            orderDataMap.put("coinAmount", String.valueOf(order.getCoinAmount()));
            orderDataMap.put("matchIdx", String.valueOf(order.getMatchIdx()));
            orderDataMap.put("executionPrice", String.valueOf(order.getExecutionPrice()));

            // 4. Map 데이터를 다시 String으로 직렬화
            String updatedOrderData = convertService.convertMapToString(orderDataMap);

            // 5. 수정된 데이터를 Redis에 다시 저장 (Hash 구조 사용)
            setHashOps("PENDING:ORDER:" + order.getCoinName() + "-" + order.getMarketName(), Map.of(order.getUuid(), updatedOrderData));

        } catch (Exception e) {
            System.err.println("Failed to update order in Redis: " + e.getMessage());
        }
    }

    public void insertOrderInRedis(String key, OrderStatus orderStatus, CoinOrderDTO order) {
        try {
            // Redis에 저장할 주문 데이터를 HashMap으로 저장
            Map<String, String> orderDataMap = new HashMap<>();
            // 기본 데이터 추가
            orderDataMap.put("uuid", String.valueOf(order.getUuid()));
            orderDataMap.put("coinName", String.valueOf(order.getCoinName()));
            orderDataMap.put("marketName", String.valueOf(order.getMarketName()));
            orderDataMap.put("coinAmount", String.valueOf(order.getCoinAmount()));
            orderDataMap.put("orderPrice", String.valueOf(order.getOrderPrice()));
            orderDataMap.put("orderType", String.valueOf(order.getOrderType()));
            orderDataMap.put("fee", String.valueOf(order.getFee()));
            orderDataMap.put("createdAt", String.valueOf(LocalDateTime.now()));
            orderDataMap.put("memberIdx", String.valueOf(order.getMemberIdx()));
            orderDataMap.put("memberUuid", String.valueOf(order.getMemberUuid()));
            orderDataMap.put("orderStatus", String.valueOf(order.getOrderStatus()));

            if (orderStatus == PENDING) {
                if (order.getMatchedAt() != null) {
                    orderDataMap.put("matchedAt", String.valueOf(order.getMatchedAt()));
                }
                if (order.getMatchedAt() != null) {
                    orderDataMap.put("matchIdx", String.valueOf(order.getMatchIdx()));
                }
                if (order.getMatchedAt() != null) {
                    orderDataMap.put("executionPrice", String.valueOf(order.getExecutionPrice()));
                }
            } else if (orderStatus == COMPLETED) {
                orderDataMap.put("matchedAt", String.valueOf(order.getMatchedAt()));
                orderDataMap.put("matchIdx", String.valueOf(order.getMatchIdx()));
                orderDataMap.put("executionPrice", String.valueOf(order.getExecutionPrice()));
            }

            // 주문 데이터를 JSON 문자열로 변환
            String jsonOrderData = convertService.convertMapToString(orderDataMap);

            // Redis에 주문 데이터 저장 (Hash 구조 사용)
            setHashOps(orderStatus + ":ORDER:HASH:" + key, Map.of(order.getUuid(), jsonOrderData));

        } catch (Exception e) {
            System.err.println("Failed to insert order in Redis: " + e.getMessage());
        }
    }

    public void addOrderToZSet(String symbol, OrderStatus orderStatus, CoinOrderDTO order) {
        String zsetKey = orderStatus + ":ORDER:ZSET:" + symbol + ":" + order.getOrderType();
        String hashKey = orderStatus + ":ORDER:HASH:" + symbol + ":" + order.getOrderType();

        // 가격을 BigDecimal로 사용
        BigDecimal price = order.getOrderPrice();

        // 시간 정보 계산 (밀리초 단위의 현재 시간)
        long currentTimeMillis = System.currentTimeMillis();
        int timeComponent = (int) (currentTimeMillis % 10_000); // 4자리 시간 값 추출

        // 시간 값을 소수점 이하로 변환
        BigDecimal timeAsDecimal = new BigDecimal(timeComponent).divide(new BigDecimal(10_000));

        // 점수 생성: 가격 + 시간(소수점 이하 4자리로 표현)
        BigDecimal score = price.add(timeAsDecimal);

        if (order.getOrderType() == BUY) {
            // 매수는 높은 가격이 먼저 나오므로 부호 반전
            score = score.negate();
        }

        // Lua 스크립트 작성
        String luaScript =
                "local zsetKey = KEYS[1] " +
                        "local hashKey = KEYS[2] " +
                        "local uuid = ARGV[1] " +
                        "local score = ARGV[2] " +
                        "local amount = ARGV[3] " +
                        "redis.call('ZADD', zsetKey, score, uuid) " +
                        "redis.call('HSET', hashKey, uuid, amount) " +
                        "return 'OK'";

        // Lua 스크립트 실행
        redisTemplate.execute(new DefaultRedisScript<>(luaScript, String.class),
                Arrays.asList(zsetKey, hashKey),
                order.getUuid(),
                score.toString(),
                order.getCoinAmount().toString());
    }

    public void getOppositeOrder(String symbol, OrderStatus orderStatus, CoinOrderDTO order) {
        String oppositeType = order.getOrderType() == OrderType.BUY ? "SELL" : "BUY";

        String zsetKey = orderStatus + ":ORDER:ZSET:" + symbol + ":" + oppositeType;
        String hashKey = orderStatus + ":ORDER:HASH:" + symbol + ":" + oppositeType;

        String luaScript = """
            local topElement = redis.call('ZRANGE', KEYS[1], 0, 0, 'WITHSCORES')
            if not topElement[1] then
                return nil
            end
            local topValue = topElement[1]
            local topScore = tonumber(topElement[2])
            if topScore <= tonumber(ARGV[1]) then
                local hashValue = redis.call('HGET', KEYS[2], topValue)
                return {topValue, hashValue}
            else
                return nil
            end
        """;

        List<Object> result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, List.class),
                Arrays.asList(zsetKey, hashKey),
                String.valueOf(order.getOrderPrice())
        );

        System.out.println(result);
    }
}
