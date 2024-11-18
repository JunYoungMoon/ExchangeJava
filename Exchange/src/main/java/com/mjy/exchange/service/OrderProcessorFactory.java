package com.mjy.exchange.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderProcessorFactory {

    private final List<OrderProcessor> orderProcessors;
    private final Map<String, OrderProcessor> processorMap = new HashMap<>();

    public OrderProcessorFactory(List<OrderProcessor> orderProcessors) {
        this.orderProcessors = orderProcessors;
    }

    @PostConstruct
    public void init() {
        // OrderProcessor 구현체를 주문 타입별로 매핑
        for (OrderProcessor processor : orderProcessors) {
            processorMap.put(processor.getOrderType().toUpperCase(), processor);
        }
    }

    public OrderProcessor getProcessor(String orderType) {
        OrderProcessor processor = processorMap.get(orderType.toUpperCase());
        if (processor == null) {
            throw new IllegalArgumentException("유효하지 않은 주문 타입입니다: " + orderType);
        }
        return processor;
    }
}