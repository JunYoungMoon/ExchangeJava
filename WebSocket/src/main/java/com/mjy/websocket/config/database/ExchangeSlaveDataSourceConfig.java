package com.mjy.websocket.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.mjy.websocket.repository.exchange",
        entityManagerFactoryRef = "exchangeSlaveEntityManagerFactory",
        transactionManagerRef = "exchangeSlaveTransactionManager"
)
public class ExchangeSlaveDataSourceConfig {

    @Bean(name = "exchangeSlaveDataSource")
    @ConfigurationProperties(prefix = "spring.exchange.slave.datasource.hikari")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "exchangeSlaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("exchangeSlaveDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.mjy.websocket.entity.exchange")
                .persistenceUnit("slave")
                .build();
    }

    @Bean(name = "exchangeSlaveTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("exchangeSlaveEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
