package com.mjy.coin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.entity.cassandra.PendingOrder;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import com.mjy.coin.repository.coin.cassandra.PendingOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;
    @Autowired
    private ConvertService convertService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PendingOrderRepository pendingOrderRepository;

    @Test
    public void testInsertOrderInRedis() throws Exception {
        //given
        String key = "testKey";
        String hashKey = "testUuid";
        OrderStatus orderStatus = OrderStatus.PENDING;

        CoinOrderDTO order = new CoinOrderDTO();
        order.setUuid(hashKey);
        order.setCoinName("BTC");
        order.setMarketName("KRW");
        order.setCoinAmount(BigDecimal.valueOf(0.1));
        order.setOrderPrice(BigDecimal.valueOf(50000));
        order.setOrderType(OrderType.BUY);
        order.setFee(BigDecimal.valueOf(0.001));
        order.setMemberIdx(123L);
        order.setMemberUuid("memberUuid123");
        order.setOrderStatus(OrderStatus.PENDING);

        //when
        redisService.insertOrderInRedis(key, orderStatus, order);

        //then
        String orderData = redisService.getHashOps("PENDING:ORDER:" + key, hashKey);
        Map<String, String> orderDataMap = convertService.convertStringToMap(orderData);

        assertThat(orderDataMap).containsEntry("orderType", "BUY");
        assertThat(orderDataMap).containsEntry("createdAt", "2025-01-04T22:41:48.610319439");
        assertThat(orderDataMap).containsEntry("marketName", "KRW");
        assertThat(orderDataMap).containsEntry("coinAmount", "0.1");
        //..
    }

    @Test
    public void testUpdateHashField() throws Exception {
        //given
        String key = "testKey";
        String hashKey = "testUuid";

        CoinOrderDTO order = new CoinOrderDTO();
        order.setCoinName("ETH");
        order.setMarketName("USDT");

        String orderString = objectMapper.writeValueAsString(order);

        //when
        redisService.updateHashField("PENDING:ORDER:" + key, hashKey, orderString);

        //then
        String orderData = redisService.getHashOps("PENDING:ORDER:" + key, hashKey);
        Map<String, String> orderDataMap = convertService.convertStringToMap(orderData);

        assertThat(orderDataMap).containsEntry("coinName", "ETH");
        assertThat(orderDataMap).containsEntry("marketName", "USDT");
    }

    @Async
    public CompletableFuture<Void> saveToRedis(String keyPrefix, OrderStatus orderStatus, CoinOrderDTO order) {
        redisService.addOrderToZSetByPrice(keyPrefix, orderStatus, order);
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> saveToCassandra(PendingOrder pendingOrder) {
        pendingOrderRepository.save(pendingOrder);
        return CompletableFuture.completedFuture(null);
    }

    @Test
    public void testInsertLargeDataInRedis() {
        String keyPrefix = "testKey";
        OrderStatus orderStatus = OrderStatus.PENDING;
        Random random = new Random();


        for (int i = 0; i < 3_000_000; i++) { // 300만 건 삽입
            long idx = 1 + random.nextInt(10);
            String hashKey = "orderUuid-" + i;

            CoinOrderDTO order = new CoinOrderDTO();
            order.setUuid(hashKey);
            order.setCoinName("BTC");
            order.setMarketName("KRW");
            order.setCoinAmount(BigDecimal.valueOf(0.1));

            // 가격을 5000~6000 사이의 랜덤 값으로 설정
            BigDecimal randomPrice = BigDecimal.valueOf(5000 + random.nextInt(1001)); // 5000 ~ 6000 사이
            order.setOrderPrice(randomPrice);

            order.setOrderType(OrderType.BUY);
            order.setFee(BigDecimal.valueOf(0.001));
            order.setMemberIdx(idx);
            order.setMemberUuid("memberUuid" + idx);
            order.setCreatedAt(LocalDateTime.now());

            // 비동기로 Redis와 Cassandra 작업 추가
//            saveToRedis(keyPrefix, orderStatus, order);
//            redisService.addOrderToZSetByPrice(keyPrefix, orderStatus, order);
            saveToCassandra(convert(order));

            // 로그를 주기적으로 출력
            if (i % 10_000 == 0) {
                System.out.println("Inserted " + i + " records.");
            }
        }

        System.out.println("Completed insertion of large data into Redis and Cassandra.");
    }


    public static PendingOrder convert(CoinOrderDTO coinOrderDTO) {
        PendingOrder pendingOrder = new PendingOrder();
        pendingOrder.setOrderUuid(coinOrderDTO.getUuid());
        pendingOrder.setCoinName(coinOrderDTO.getCoinName());
        pendingOrder.setMarketName(coinOrderDTO.getMarketName());
        pendingOrder.setCoinAmount(coinOrderDTO.getCoinAmount());
        pendingOrder.setOrderPrice(coinOrderDTO.getOrderPrice());
        pendingOrder.setOrderType(coinOrderDTO.getOrderType());
        pendingOrder.setFee(coinOrderDTO.getFee());
        pendingOrder.setMemberIdx(coinOrderDTO.getMemberIdx());
        pendingOrder.setMemberUuid(coinOrderDTO.getMemberUuid());
        pendingOrder.setCreatedAt(coinOrderDTO.getCreatedAt());
        return pendingOrder;
    }

}