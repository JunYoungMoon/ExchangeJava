package com.mjy.coin.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.util.CustomListJsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

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
    public ConsumerFactory<String, CoinOrderDTO> coinOrderConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "coinOrderGroup");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.TYPE_MAPPINGS, "coinOrder:com.mjy.coin.dto.CoinOrderDTO");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CoinOrderDTO> coinOrderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CoinOrderDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(coinOrderConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, List<CoinOrderDTO>> coinOrderListConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "coinOrderGroup");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
//        config.put(JsonDeserializer.TYPE_MAPPINGS, "coinOrderList:com.mjy.coin.dto.CoinOrderDTOList");

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new CustomListJsonDeserializer<>(objectMapper, new TypeReference<>() {
                }));

//        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, List<CoinOrderDTO>> coinOrderListKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, List<CoinOrderDTO>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(coinOrderListConsumerFactory());
        return factory;
    }
}
