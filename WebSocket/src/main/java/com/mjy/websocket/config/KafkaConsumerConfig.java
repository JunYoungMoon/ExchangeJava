package com.mjy.websocket.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.websocket.dto.CoinOrderDTO;
import com.mjy.websocket.dto.PriceVolumeDTO;
import com.mjy.websocket.util.CustomJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private final ObjectMapper objectMapper;

    public KafkaConsumerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ConsumerFactory<String, List<CoinOrderDTO>> matchListConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "coinOrderGroup");

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, new TypeReference<>() {
                }));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<CoinOrderDTO>> matchListKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<CoinOrderDTO>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(matchListConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "coinOrderGroup");

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, new TypeReference<>() {
                }));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, List<PriceVolumeDTO>>> priceVolumeMapKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, List<PriceVolumeDTO>>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(priceVolumeMapConsumerFactory());
        return factory;
    }
}
