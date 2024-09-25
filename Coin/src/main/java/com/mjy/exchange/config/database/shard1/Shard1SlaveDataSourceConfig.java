package com.mjy.exchange.config.database.shard1;

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
        basePackages = "com.mjy.exchange.repository.shard1.slave",
        entityManagerFactoryRef = "shard1SlaveEntityManagerFactory",
        transactionManagerRef = "shard1SlaveTransactionManager"
)
public class Shard1SlaveDataSourceConfig {

    @Bean(name = "shard1SlaveDataSource")
    @ConfigurationProperties(prefix = "spring.shard1.slave.datasource.hikari")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "shard1SlaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("shard1SlaveDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.mjy.exchange.entity.shard1")
                .persistenceUnit("slave")
                .build();
    }

    @Bean(name = "shard1SlaveTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("shard1SlaveEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
