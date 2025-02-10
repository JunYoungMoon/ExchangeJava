package com.mjy.coin.service.config.database;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.mjy.coin.repository.coin.slave",
        entityManagerFactoryRef = "coinSlaveEntityManagerFactory",
        transactionManagerRef = "coinSlaveTransactionManager"
)
public class CoinSlaveDataSourceConfig {

    @Bean(name = "coinSlaveDataSource")
    @ConfigurationProperties(prefix = "spring.coin.slave.datasource.hikari")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "coinSlaveEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("coinSlaveDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.mjy.coin.entity.coin")
                .persistenceUnit("slave")
                .build();
    }

    @Bean(name = "coinSlaveTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("coinSlaveEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean(name = "slaveJdbcTemplate")
    public JdbcTemplate slaveJdbcTemplate(@Qualifier("coinSlaveDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
