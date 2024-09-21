package com.member.exchange.component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class APIRateLimiter {
    private final LettuceBasedProxyManager<String> proxyManager;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
//    private final Cache<String, Bucket> buckets;

//    private static final Duration REFILL_DURATION = Duration.ofSeconds(5);
//    private static final int REFILL_AMOUNT = 4;

    private final Map<String, LocalDateTime> lastAccessTimes = new ConcurrentHashMap<>();
    private final Duration unusedExpirationDuration = Duration.ofMinutes(30); // 사용되지 않는 API 키의 만료 기간 설정

    public APIRateLimiter(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        // redis
        this.proxyManager = LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
                .build();

//        this.buckets = CacheBuilder.newBuilder()
//                .expireAfterAccess(Duration.ofMinutes(10))
//                .build();
    }

    private Bucket getOrCreateBucket(String apiKey, long limit, long period) /*throws ExecutionException*/ {
        lastAccessTimes.put(apiKey, LocalDateTime.now()); // API 호출 시간 기록

        //부재시 계산
        return buckets.computeIfAbsent(apiKey, key -> {
            //버킷 설정 생성
            BucketConfiguration configuration = createBucketConfiguration(limit, period);
            //설정을 토대로 버킷 생성
            return proxyManager.builder().build(key, configuration);
        });

//        return buckets.get(apiKey, () -> {
//            BucketConfiguration configuration = createBucketConfiguration(limit, period);
//            return proxyManager.builder().build(apiKey, configuration);
//        });
    }

    private BucketConfiguration createBucketConfiguration(long limit, long period) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.simple((int) limit, Duration.ofMillis(period)).withInitialTokens((int) limit))
                .build();
    }

    // 사용하지 않는 API 키에 대한 버킷을 정리
    private void cleanUpUnusedApiKeys() {
        LocalDateTime now = LocalDateTime.now();
        Set<String> unusedKeys = new HashSet<>();

        for (Map.Entry<String, LocalDateTime> entry : lastAccessTimes.entrySet()) {
            String apiKey = entry.getKey();
            LocalDateTime lastAccessTime = entry.getValue();
            if (now.minus(unusedExpirationDuration).isAfter(lastAccessTime)) {
                unusedKeys.add(apiKey);
            }
        }

        unusedKeys.forEach(key -> {
            buckets.remove(key);
            lastAccessTimes.remove(key);
        });
    }

    public boolean tryConsume(String apiKey, long limit, long period) {
        // 사용하지 않는 API 키에 대한 버킷을 정리
        cleanUpUnusedApiKeys();

        Bucket bucket = getOrCreateBucket(apiKey, limit, period);
        //이시점에서 redis 입력
        boolean consumed = bucket.tryConsume(1);
        log.info("API Key: {}, Consumed: {}, Time: {}", apiKey, consumed, LocalDateTime.now());

        return consumed;

//        try {
//            Bucket bucket = getOrCreateBucket(apiKey, limit, period);
//            boolean consumed = bucket.tryConsume(1);
//            log.info("API Key: {}, Consumed: {}, Time: {}", apiKey, consumed, java.time.LocalDateTime.now());
//            return consumed;
//        } catch (ExecutionException e) {
//            log.error("Error getting or creating bucket for API key: " + apiKey, e);
//            return false;
//        }
    }
}