package com.mjy.coin.batch.config;

import com.mjy.coin.batch.CoinOrderProcessor;
import com.mjy.coin.batch.CoinOrderReader;
import com.mjy.coin.batch.CoinOrderWriter;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CoinOrderService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.DefaultJobKeyGenerator;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.support.DefaultDataFieldMaxValueIncrementerFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class CoinOrderBatchConfig {

    @Bean(name = "CoinOrderJobRepository")
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
    public Job coinOrderJob(@Qualifier("CoinOrderJobRepository") JobRepository jobRepository,
                            Step checkDataStep,
                            Step partitionStep,
                            Step mergeStep) {
        return new JobBuilder("coinOrderJob", jobRepository)
                .start(checkDataStep)
                .next(partitionStep)
//                .next(mergeStep)
                .build();
    }

    @Bean
    public Step partitionStep(@Qualifier("CoinOrderJobRepository") JobRepository jobRepository,
                              @Qualifier("CoinOrderPartitioner") Partitioner partitioner,
                              Step coinOrderStep) {
        return new StepBuilder("partitionStep", jobRepository)
                .partitioner("coinOrderPartition", partitioner)
                .step(coinOrderStep)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step coinOrderStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              CoinOrderReader reader,
                              CoinOrderProcessor processor,
                              CoinOrderWriter writer) {
        return new StepBuilder("coinOrderStep", jobRepository)
                .<CoinOrderDTO, CoinOrderDTO>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step checkDataStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              CoinOrderService coinOrderService) {
        return new StepBuilder("checkDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {


                    LocalDate today = LocalDate.now(); // 오늘 일자 기준
                    Long[] minMaxIdx = coinOrderService.getMinMaxIdx(today); // 오늘 일자 기준

//                    LocalDate yesterday = LocalDate.now().minusDays(1);
//                    Long[] minMaxIdx = coinOrderService.getMinMaxIdx(yesterday); // 어제 일자 기준

                    // 데이터가 없으면 배치 작업을 종료
                    if (minMaxIdx[0] == 0 && minMaxIdx[1] == 0) {
                        contribution.setExitStatus(ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }

                    // 어제의 일일 종가가 CoinOrderDayHistory에 존재하는지 확인
                    if (coinOrderService.hasClosingPriceForDate(today)) {
                        contribution.setExitStatus(ExitStatus.FAILED); // 종가가 존재하면 배치 작업을 종료
                        return RepeatStatus.FINISHED;
                    }

                    // minMaxIdx를 partitioner으로 넘겨줘야함

                    contribution.setExitStatus(ExitStatus.COMPLETED); // 조건이 충족되면 다음으로 이동
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean(name = "CoinOrderPartitioner")
    public Partitioner partitioner(CoinOrderService coinOrderService) {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();

            List<Map<String, Long>> chunkPartitions = coinOrderService.partitionChunks(minMaxIdx[0], minMaxIdx[1], 1000);

            for (int i = 0; i < chunkPartitions.size(); i++) {
                ExecutionContext context = new ExecutionContext();
                context.putLong("minIdx", chunkPartitions.get(i).get("minIdx"));
                context.putLong("maxIdx", chunkPartitions.get(i).get("maxIdx"));
                partitions.put("partition" + i, context);
            }
            return partitions;
        };
    }

    @Bean
    public Step mergeStep(JobRepository jobRepository,
                          RedisService redisService,
                          PlatformTransactionManager transactionManager) {
        return new StepBuilder("mergeStep", jobRepository).tasklet((contribution, chunkContext) -> {
            System.out.println("Tasklet step executed");

            // RedisService를 사용한 리팩토링된 코드
            Set<String> keys = redisService.getKeys("partition:*");
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal totalVolume = BigDecimal.ZERO;

            for (String key : keys) {
                BigDecimal price = new BigDecimal(redisService.getHashOps(key, "totalPrice"));
                BigDecimal volume = new BigDecimal(redisService.getHashOps(key, "totalVolume"));

                totalPrice = totalPrice.add(price);
                totalVolume = totalVolume.add(volume);
            }

            // 평균 가격 계산
            BigDecimal averagePrice = totalPrice.divide(totalVolume, RoundingMode.HALF_UP);

            // 마지막 idx가 종가로 설정
//            BigDecimal closingPrice = // 마지막 처리된 CoinOrder의 executionPrice 가져오기 로직;

            // 최종 결과를 DB에 저장
//            CoinOrderDayHistory history = new CoinOrderDayHistory();
//            history.setMarketName("KRW");
//            history.setCoinName("BTC");
//            history.setAveragePrice(averagePrice);
//            history.setTradingVolume(totalVolume);
//            history.setClosingPrice(closingPrice);
//            history.setTradingDate(LocalDate.now());
//
//            // DB 저장
//            coinOrderDayHistoryRepository.save(history);
            return RepeatStatus.FINISHED;
        }, transactionManager).build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(500);
        return taskExecutor;
    }
}
