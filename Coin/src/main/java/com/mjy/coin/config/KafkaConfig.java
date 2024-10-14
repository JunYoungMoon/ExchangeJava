package com.mjy.coin.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Bean
    public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry() {
        return new KafkaListenerEndpointRegistry();
    }

//    @Bean
//    public NewTopic orderCompletedTopic() {
//        // "Order-Completed" 토픽을 4개의 파티션과 3개의 복제본으로 생성
//        return new NewTopic("Order-Completed", 4, (short) 3);
//    }
}