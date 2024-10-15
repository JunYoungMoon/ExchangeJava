package com.mjy.coin.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinInfoDTO;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.repository.coin.slave.SlaveCoinOrderRepository;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@DependsOn("kafkaAdmin")
public class KafkaListenerCreator {

    private static final String KAFKA_GROUP_ID = "coinOrderGroup";
    private static final AtomicLong endpointIdIndex = new AtomicLong(1);

    private final CoinInfoService coinInfoService;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaListenerContainerFactory<?> kafkaListenerContainerFactory;
    private final OrderProcessor orderProcessor;

    public KafkaListenerCreator(OrderProcessor orderProcessor, CoinInfoService coinInfoService, KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry, KafkaListenerContainerFactory<?> kafkaListenerContainerFactory) {
        this.orderProcessor = orderProcessor;
        this.coinInfoService = coinInfoService;
        this.kafkaListenerContainerFactory = kafkaListenerContainerFactory;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        List<String> keys = coinInfoService.getCoinMarketKeys();

        for (String key : keys) {
            createAndRegisterListener(key);
        }
    }

    private MethodKafkaListenerEndpoint<String, CoinOrderDTO> createKafkaListenerEndpoint(String topic) {
        MethodKafkaListenerEndpoint<String, CoinOrderDTO> kafkaListenerEndpoint = new MethodKafkaListenerEndpoint<>();
        kafkaListenerEndpoint.setId(generateListenerId());
        kafkaListenerEndpoint.setGroupId(KAFKA_GROUP_ID);
        kafkaListenerEndpoint.setAutoStartup(true);
        kafkaListenerEndpoint.setTopics(topic);
        kafkaListenerEndpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());

        PendingOrderListener listener = new PendingOrderListener(orderProcessor);
        kafkaListenerEndpoint.setBean(listener);

        try {
            kafkaListenerEndpoint.setMethod(PendingOrderListener.class.getMethod("onMessage", ConsumerRecord.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Attempt to call a non-existent method " + e);
        }

        return kafkaListenerEndpoint;
    }

    private String generateListenerId() {
        return KAFKA_GROUP_ID + "-" + endpointIdIndex.getAndIncrement();
    }

    public void createAndRegisterListener(String topic) {
        if (kafkaListenerEndpointRegistry.getListenerContainers().stream().noneMatch(container -> container.getListenerId().equals(topic))) {
            MethodKafkaListenerEndpoint<String, CoinOrderDTO> listener = createKafkaListenerEndpoint(topic);
            kafkaListenerEndpointRegistry.registerListenerContainer(listener, kafkaListenerContainerFactory, true);
        } else {
            System.out.println("Listener for topic " + topic + " already exists.");
        }
    }
}