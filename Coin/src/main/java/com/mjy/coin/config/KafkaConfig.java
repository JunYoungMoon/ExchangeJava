package com.mjy.coin.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mjy.coin.service.CoinInfoService;
import org.apache.kafka.clients.admin.NewTopic;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;

@EnableKafka
@Configuration
@Profile("dev")
public class KafkaConfig {
    private final CoinInfoService coinInfoService;

    public KafkaConfig(CoinInfoService coinInfoService) {
        this.coinInfoService = coinInfoService;
    }

    @Bean
    public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry() {
        return new KafkaListenerEndpointRegistry();
    }

    @Bean
    public NewTopic orderCompletedTopic() {
        // "Order-Completed" 토픽을 4개의 파티션과 1개의 복제본으로 생성
        return new NewTopic("Order-Completed", 4, (short) 1);
    }

    @Bean
    public KafkaAdmin.NewTopics dynamicCoinMarketTopics() throws JsonProcessingException {
        List<String> keys = coinInfoService.getCoinMarketKeys();
        // BTC-KRW, ETH-KRW 토픽을 1개의 파티션과 1개의 복제본으로 생성
        NewTopic[] topics = keys.stream()
                .map(key -> new NewTopic(key, 1, (short) 1))
                .toArray(NewTopic[]::new);

        return new KafkaAdmin.NewTopics(topics);
    }
}