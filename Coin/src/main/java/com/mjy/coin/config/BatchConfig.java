package com.mjy.coin.config;

import com.mjy.coin.batch.CompletedOrderProcessor;
import com.mjy.coin.batch.CompletedOrderReader;
import com.mjy.coin.batch.CompletedOrderWriter;
import com.mjy.coin.dto.CoinOrderDTO;
import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;

import javax.sql.DataSource;


@Configuration
public class BatchConfig {

    @Bean(name = "OrderJobRepository")
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

    @Bean
    public Job orderJob(@Qualifier("OrderJobRepository") JobRepository jobRepository, Step orderStep) {
        return new JobBuilder("orderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(orderStep)
                .end()
                .build();
    }

    @Bean
    public Step orderStep(JobRepository jobRepository,
                          CompletedOrderReader reader,
                          CompletedOrderProcessor processor,
                          CompletedOrderWriter writer,
                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("orderStep", jobRepository)
                .<CoinOrderDTO, CoinOrderDTO>chunk(10, transactionManager) // 한 번에 10개씩 처리
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

}