package com.mjy.coin.config;

import com.mjy.coin.dto.CoinOrderDTO;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Configuration
//public class KafkaProducerConfig {
//
//    @Bean(name = "listCoinOrderProducerFactory")
//    public ProducerFactory<String, List<CoinOrderDTO>> producerFactory() {
//        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(configProps);
//    }
//
//    @Bean(name = "listCoinOrderKafkaTemplate")
//    public KafkaTemplate<String, List<CoinOrderDTO>> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
//}
