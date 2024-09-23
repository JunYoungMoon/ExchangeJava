package com.mjy.exchange.config.database;//package com.mjy.exchange.config.database;
//
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.jdbc.DataSourceBuilder;
//import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.orm.jpa.JpaTransactionManager;
//import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableTransactionManagement
//@EnableJpaRepositories(
//        basePackages = "com.mjy.exchange.repository.master",
//        entityManagerFactoryRef = "exchangeEntityManagerFactory",
//        transactionManagerRef = "exchangeTransactionManager"
//)
//public class ExchangeDataSourceConfig {
//
//    @Bean(name = "exchangeDataSource")
//    @Primary
//    @ConfigurationProperties(prefix = "spring.exchange.datasource.hikari")
//    public DataSource exchangeDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "exchangeEntityManagerFactory")
//    @Primary
//    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("exchangeDataSource") DataSource dataSource) {
//
//        Map<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.hbm2ddl.auto", "update");
//
//        return builder
//                .dataSource(dataSource)
//                .packages("com.mjy.exchange.entity")
//                .persistenceUnit("master")
//                .properties(properties)
//                .build();
//    }
//
//    @Bean(name = "exchangeTransactionManager")
//    @Primary
//    public PlatformTransactionManager transactionManager(
//            @Qualifier("exchangeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
//}
