package com.mjy.coin.service;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {
                "com.mjy.coin.config",
                "com.mjy.coin.service"
        },
        exclude = {KafkaAutoConfiguration.class}
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
