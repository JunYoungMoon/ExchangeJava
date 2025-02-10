//package com.mjy.coin.component;
//
//import com.mjy.coin.dto.CoinOrderDTO;
//import com.mjy.coin.service.LimitOrderService;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Profile;
//import org.springframework.kafka.listener.MessageListener;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class PendingOrderKafkaListener implements MessageListener<String, CoinOrderDTO> {
//
//    private final LimitOrderService limitOrderService;
//
//    @Autowired
//    public PendingOrderKafkaListener(LimitOrderService limitOrderService) {
//        this.limitOrderService = limitOrderService;
//    }
//
//    @Override
//    public void onMessage(ConsumerRecord<String, CoinOrderDTO> record) {
//        limitOrderService.processOrder(record.value());
//    }
//}