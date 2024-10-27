package com.mjy.coin.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.PriceVolumeDTO;
import com.mjy.coin.util.CustomListJsonSerializer;
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
    public ProducerFactory<String, List<CoinOrderDTO>> coinOrderListProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // List<CoinOrderDTO> 타입의 직렬화 설정
        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                new CustomListJsonSerializer<>(objectMapper, new TypeReference<>() {}));

//        key-value 방식
//        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        config.put(JsonSerializer.TYPE_MAPPINGS, "coinOrderList:com.mjy.coin.dto.CoinOrderDTOList");

//        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "coinOrderListKafkaTemplate")
    public KafkaTemplate<String, List<CoinOrderDTO>> coinOrderListKafkaTemplate() {
        return new KafkaTemplate<>(coinOrderListProducerFactory());
    }


    @Bean
    public ProducerFactory<String, List<PriceVolumeDTO>> priceVolumeListProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        return new DefaultKafkaProducerFactory<>(config,
                new StringSerializer(),
                new CustomListJsonSerializer<>(objectMapper, new TypeReference<>() {}));
    }

    @Bean(name = "priceVolumeListKafkaTemplate")
    public KafkaTemplate<String, List<PriceVolumeDTO>> priceVolumeListKafkaTemplate() {
        return new KafkaTemplate<>(priceVolumeListProducerFactory());
    }
}
