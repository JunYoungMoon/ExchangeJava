package com.mjy.coin.batch.config;

import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("dev")
public class BatchConfig {
    @Bean(name = "JobRepository")
    public JobRepository jobRepository(@Qualifier("coinMasterTransactionManager") PlatformTransactionManager transactionManager,
                                       @Qualifier("coinMasterDataSource") DataSource dataSource) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();

        // JdbcOperations 설정
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        factory.setJdbcOperations(jdbcTemplate);

        // ConversionService 설정
        ConfigurableConversionService conversionService = new DefaultConversionService();
        factory.setConversionService(conversionService);

        // Serializer 설정
        ExecutionContextSerializer serializer = new DefaultExecutionContextSerializer();
        factory.setSerializer(serializer);

        // incrementerFactory 설정
        DefaultDataFieldMaxValueIncrementerFactory incrementerFactory =
                new DefaultDataFieldMaxValueIncrementerFactory(dataSource);
        factory.setIncrementerFactory(incrementerFactory);

        // jobKeyGenerator 설정
        factory.setJobKeyGenerator(new DefaultJobKeyGenerator());

        // Database 유형 설정
        factory.setDatabaseType(DatabaseType.fromMetaData(dataSource).name());

        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.setMaxVarCharLength(500);
        factory.setTablePrefix("BATCH_");

        return factory.getObject();
    }
}
