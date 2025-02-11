package com.mjy.coin.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.util.CustomJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("dev")
public class KafkaConsumerConfig {
    private final ObjectMapper objectMapper;
    public KafkaConsumerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ConsumerFactory<String, CoinOrderDTO> coinOrderConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, "coinOrderGroup");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        config.put(JsonDeserializer.TYPE_MAPPINGS, "coinOrder:com.mjy.coin.dto.CoinOrderDTO");

//        return new DefaultKafkaConsumerFactory<>(config);

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new CustomJsonDeserializer<>(objectMapper, new TypeReference<>() {
                }));
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CoinOrderDTO> coinOrderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CoinOrderDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(coinOrderConsumerFactory());
        return factory;
    }
}
