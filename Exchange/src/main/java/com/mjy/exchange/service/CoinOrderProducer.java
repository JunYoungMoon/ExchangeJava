package com.mjy.exchange.service;

import com.mjy.exchange.dto.CoinOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CoinOrderProducer {

    private final KafkaTemplate<String, CoinOrder> kafkaTemplate;

    @Autowired
    public CoinOrderProducer(KafkaTemplate<String, CoinOrder> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCoinOrder(String topic, CoinOrder coinOrder) {
        kafkaTemplate.send(topic, coinOrder);
    }
}