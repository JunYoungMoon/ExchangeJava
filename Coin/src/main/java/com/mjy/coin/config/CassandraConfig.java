package com.mjy.coin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "cassandra.enabled", havingValue = "true")
public class CassandraConfig {
    // Cassandra 관련 설정
}