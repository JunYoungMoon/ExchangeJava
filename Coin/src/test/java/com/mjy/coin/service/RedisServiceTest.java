package com.mjy.coin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.enums.OrderStatus;
import com.mjy.coin.enums.OrderType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisService redisService;
    @Autowired
    private ConvertService convertService;
    @Autowired
    private ObjectMapper objectMapper;

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
}