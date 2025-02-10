package com.mjy.coin.component;

import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.LimitOrderService;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Component
@DependsOn("kafkaAdmin")
public class PendingOrderKafkaListenerCreator {

    private static final String KAFKA_GROUP_ID = "coinOrderGroup";
    private static final AtomicLong endpointIdIndex = new AtomicLong(1);

    private final CoinInfoService coinInfoService;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaListenerContainerFactory<?> coinOrderKafkaListenerContainerFactory;
    private final LimitOrderService limitOrderService;

    public PendingOrderKafkaListenerCreator(
            LimitOrderService limitOrderService,
            CoinInfoService coinInfoService,
            @Qualifier("kafkaListenerEndpointRegistry") KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry,
            @Qualifier("coinOrderKafkaListenerContainerFactory") KafkaListenerContainerFactory<?> coinOrderKafkaListenerContainerFactory) {
        this.limitOrderService = limitOrderService;
        this.coinInfoService = coinInfoService;
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
        this.coinOrderKafkaListenerContainerFactory = coinOrderKafkaListenerContainerFactory;
    }

    @PostConstruct
    public void init() {
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

        PendingOrderKafkaListener listener = new PendingOrderKafkaListener(limitOrderService);
        kafkaListenerEndpoint.setBean(listener);

        try {
            kafkaListenerEndpoint.setMethod(PendingOrderKafkaListener.class.getMethod("onMessage", ConsumerRecord.class));
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
            kafkaListenerEndpointRegistry.registerListenerContainer(listener, coinOrderKafkaListenerContainerFactory, true);
        } else {
            System.out.println("Listener for topic " + topic + " already exists.");
        }
    }
}