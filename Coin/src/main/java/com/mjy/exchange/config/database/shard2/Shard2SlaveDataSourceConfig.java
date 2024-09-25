package com.mjy.exchange.config.database.shard2;

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
        basePackages = "com.mjy.exchange.repository.shard2.slave",
        entityManagerFactoryRef = "shard2SlaveEntityManagerFactory",
        transactionManagerRef = "shard2SlaveTransactionManager"
)
public class Shard2SlaveDataSourceConfig {

    @Bean(name = "shard2SlaveDataSource")
    @ConfigurationProperties(prefix = "spring.shard2.slave.datasource.hikari")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "shard2SlaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shard2SlaveDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.mjy.exchange.entity.shard2")
                .persistenceUnit("slave")
                .build();
    }

    @Bean(name = "shard2SlaveTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("shard2SlaveEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
