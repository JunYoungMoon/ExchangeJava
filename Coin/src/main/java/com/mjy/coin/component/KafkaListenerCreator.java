package com.mjy.coin.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjy.coin.dto.CoinInfoDTO;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class KafkaListenerCreator {

    private static final String KAFKA_GROUP_ID = "coinOrderGroup";
    private static final AtomicLong endpointIdIndex = new AtomicLong(1);

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private KafkaListenerContainerFactory<?> kafkaListenerContainerFactory;

    @Autowired
    private RedisService redisService; // RedisService를 추가

    @Autowired
    private OrderMatcher priorityQueueManager; // PriorityQueueManager 주입

    private final MasterCoinOrderRepository masterCoinOrderRepository;

    public KafkaListenerCreator(MasterCoinOrderRepository masterCoinOrderRepository) {
        this.masterCoinOrderRepository = masterCoinOrderRepository;
    }

    @PostConstruct
    public void init() {
        // 환경 변수에서 COIN_TYPE을 가져옴
        String coinTypeEnv = System.getenv("COIN_TYPE");
        if (coinTypeEnv == null) {
            coinTypeEnv = "MAJOR"; // 기본값 설정
        }

        try {
            // Redis에서 해당 COIN_TYPE에 맞는 CoinInfo 리스트를 가져옴
            String jsonData = redisService.getValues(coinTypeEnv);
            CoinInfoDTO[] coinInfoList = new ObjectMapper().readValue(jsonData, CoinInfoDTO[].class);

            // 각 CoinInfo에 따라 토픽을 동적으로 등록
            for (CoinInfoDTO coinInfo : coinInfoList) {
                String topicName = coinInfo.getCoinName() + "-" + coinInfo.getMarketName();

                // 리스너 등록
                createAndRegisterListener(topicName);
            }
        } catch (IOException e) {
            // 예외 처리 로직 추가
            System.err.println("Failed to load CoinInfo from Redis: " + e.getMessage());
        }
    }

    private MethodKafkaListenerEndpoint<String, CoinOrderDTO> createKafkaListenerEndpoint(String topic) {
        MethodKafkaListenerEndpoint<String, CoinOrderDTO> kafkaListenerEndpoint = new MethodKafkaListenerEndpoint<>();
        kafkaListenerEndpoint.setId(generateListenerId());
        kafkaListenerEndpoint.setGroupId(KAFKA_GROUP_ID);
        kafkaListenerEndpoint.setAutoStartup(true);
        kafkaListenerEndpoint.setTopics(topic);
        kafkaListenerEndpoint.setMessageHandlerMethodFactory(new DefaultMessageHandlerMethodFactory());

        KafkaTemplateListener listener = new KafkaTemplateListener(priorityQueueManager, masterCoinOrderRepository);
        kafkaListenerEndpoint.setBean(listener);

        try {
            kafkaListenerEndpoint.setMethod(KafkaTemplateListener.class.getMethod("onMessage", ConsumerRecord.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Attempt to call a non-existent method " + e);
        }

        return kafkaListenerEndpoint;
    }

    private String generateListenerId() {
        return KAFKA_GROUP_ID + "-" + endpointIdIndex.getAndIncrement();
    }

    public void createAndRegisterListener(String topic) {
        MethodKafkaListenerEndpoint<String, CoinOrderDTO> listener = createKafkaListenerEndpoint(topic);
        kafkaListenerEndpointRegistry.registerListenerContainer(listener, kafkaListenerContainerFactory, true);
    }
}