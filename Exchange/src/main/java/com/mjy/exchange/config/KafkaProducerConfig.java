package com.mjy.exchange.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.exchange.dto.CoinOrder;
import com.mjy.exchange.util.CustomJsonSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private final ObjectMapper objectMapper;

    public KafkaProducerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ProducerFactory<String, CoinOrder> coinOrderProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                new CustomJsonSerializer<>(objectMapper, new TypeReference<>() {}));
    }

    @Bean(name = "coinOrderKafkaTemplate")
    public KafkaTemplate<String, CoinOrder> coinOrderKafkaTemplate() {
        return new KafkaTemplate<>(coinOrderProducerFactory());
    }
}
