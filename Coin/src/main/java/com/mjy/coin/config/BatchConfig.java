package com.mjy.coin.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mjy.coin.batch.CompletedOrderProcessor;
import com.mjy.coin.batch.CompletedOrderReader;
import com.mjy.coin.batch.CompletedOrderWriter;
import com.mjy.coin.batch.RedisHashPartitioner;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.repository.coin.slave.SlaveChartRepository;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.step.builder.PartitionStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;

import javax.sql.DataSource;
import java.util.List;


@Configuration
public class BatchConfig {
    private final RedisService redisService;  // RedisService 주입

    private final CoinInfoService coinInfoService;

    public BatchConfig(CoinInfoService coinInfoService, RedisService redisService) {
        this.coinInfoService = coinInfoService;
        this.redisService = redisService;
    }

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

//    @Bean
//    public Job orderJob(@Qualifier("OrderJobRepository") JobRepository jobRepository, Step orderStep) {
//        return new JobBuilder("orderJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
//                .flow(orderStep)
//                .end()
//                .build();
//    }
//
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

    @Bean
    public Job orderJob(@Qualifier("OrderJobRepository") JobRepository jobRepository, Step partitionStep) {
        return new JobBuilder("orderJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(partitionStep)
                .build();
    }

    @Bean
    public Step partitionStep(@Qualifier("OrderJobRepository") JobRepository jobRepository, Step orderStep) throws JsonProcessingException {
        List<String> marketKeys = coinInfoService.getCoinMarketKeys();

        return new StepBuilder("partitionStep", jobRepository)
                .partitioner("slaveStep", new RedisHashPartitioner(redisService, marketKeys))
                .step(orderStep)
                .taskExecutor(taskExecutor())  // taskExecutor를 연결하여 병렬 처리
                .build();
    }

//    @Bean
//    public PartitionHandler partitionHandler() {
//        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
//        handler.setTaskExecutor(taskExecutor());
//        handler.setStep(orderStep());  // Order step 설정
//        return handler;
//    }
//
//    @Bean
//    public SimpleAsyncTaskExecutor taskExecutor() {
//        return new SimpleAsyncTaskExecutor();
//    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(500);
        return taskExecutor;
    }
}