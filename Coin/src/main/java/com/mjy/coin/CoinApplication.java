package com.mjy.coin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;

@SpringBootApplication
public class CoinApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoinApplication.class, args);
    }
}
