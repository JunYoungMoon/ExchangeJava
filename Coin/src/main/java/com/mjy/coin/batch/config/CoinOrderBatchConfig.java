package com.mjy.coin.batch.config;

import com.mjy.coin.dto.CoinDailyCloseDTO;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Configuration
@Profile("dev")
public class CoinOrderBatchConfig {

    private final RedisService redisService;
    private final DataSource dataSource;
    private final CoinInfoService coinInfoService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public CoinOrderBatchConfig(RedisService redisService,
                                DataSource dataSource,
                                CoinInfoService coinInfoService,
                                @Qualifier("jobRepository") JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        this.redisService = redisService;
        this.dataSource = dataSource;
        this.coinInfoService = coinInfoService;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean(name = "coinOrderJob")
    public Job coinOrderJob() {

        LocalDate date = LocalDate.parse("2024-11-07");

        List<String> keys = coinInfoService.getCoinMarketKeys();
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("coinFlow");

        for (String key : keys) {
            String[] parts = key.split("-");
            String coinName = parts[0];  // BTC, ETH..
            String marketName = parts[1]; // KRW, USDT..

            //코인마다 스텝 추가
            Step step = createCoinProcessingStep(coinName, marketName, date);
            flowBuilder.next(step);
        }

        Flow flow = flowBuilder.build();

        return new JobBuilder("dynamicCoinJob", jobRepository)
                .start(flow)
                .end()
                .build();
    }

    private Step createCoinProcessingStep(String coinName,
                                          String marketName,
                                          LocalDate yesterday) {

        JdbcCursorItemReader<CoinOrderDTO> reader = getCoinOrderDTOJdbcCursorItemReader(coinName, marketName, yesterday);

        ItemWriter<CoinOrderDTO> writer = itemWriter(coinName, marketName, yesterday);
        ItemProcessor<CoinOrderDTO, CoinOrderDTO> processor = itemProcessor();

        return new StepBuilder(coinName + "Step", jobRepository)
                .<CoinOrderDTO, CoinOrderDTO>chunk(1000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    private JdbcCursorItemReader<CoinOrderDTO> getCoinOrderDTOJdbcCursorItemReader(String coinName, String marketName, LocalDate yesterday) {
        JdbcCursorItemReader<CoinOrderDTO> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("SELECT * FROM CoinOrder WHERE coinName = ? AND marketName = ? AND DATE_FORMAT(matchedAt, '%Y-%m-%d') = ?");
        reader.setPreparedStatementSetter(preparedStatement -> {
            preparedStatement.setString(1, coinName);
            preparedStatement.setString(2, marketName);
            preparedStatement.setDate(3, Date.valueOf(yesterday));
        });
        reader.setRowMapper(new BeanPropertyRowMapper<>(CoinOrderDTO.class));
        return reader;
    }

    public ItemProcessor<CoinOrderDTO, CoinOrderDTO> itemProcessor() {
        return item -> item;
    }

    public ItemWriter<CoinOrderDTO> itemWriter(String coinName, String marketName, LocalDate yesterday) {
        return items -> {
            BigDecimal totalPrice = BigDecimal.ZERO;
            BigDecimal totalVolume = BigDecimal.ZERO;

            for (CoinOrderDTO item : items) {
                totalPrice = totalPrice.add(item.getExecutionPrice().multiply(item.getCoinAmount()));
                totalVolume = totalVolume.add(item.getCoinAmount());
            }

            String key = yesterday + ":" + coinName + "-" + marketName;
            redisService.incrementHashValue(key, "totalPrice", totalPrice.doubleValue(), Duration.ofDays(1));
            redisService.incrementHashValue(key, "totalVolume", totalVolume.doubleValue(), Duration.ofDays(1));

            System.out.println(items);
        };
    }
//
//    @Bean
//    public Step partitionStep(@Qualifier("JobRepository") JobRepository jobRepository,
//                              @Qualifier("partitioner") Partitioner partitioner,
//                              @Qualifier("coinOrderStep") Step coinOrderStep) {
//        return new StepBuilder("partitionStep", jobRepository)
//                .partitioner("coinOrderPartition", partitioner)
//                .step(coinOrderStep)
//                .taskExecutor(taskExecutor())
//                .build();
//    }
//
//    @Bean
//    public Step coinOrderStep(@Qualifier("JobRepository") JobRepository jobRepository,
//                              PlatformTransactionManager transactionManager,
//                              CoinOrderReader reader,
//                              CoinOrderProcessor processor,
//                              CoinOrderWriter writer) {
//        return new StepBuilder("coinOrderStep", jobRepository)
//                .<CoinOrderDTO, CoinOrderDTO>chunk(10, transactionManager)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build();
//    }
//
//    @Bean
//    public Step checkDataStep(@Qualifier("JobRepository") JobRepository jobRepository,
//                              PlatformTransactionManager transactionManager,
//                              CoinOrderService coinOrderService,
//                              CoinInfoService coinInfoService) {
//        return new StepBuilder("checkDataStep", jobRepository)
//                .tasklet((contribution, chunkContext) -> {
////                    LocalDate date = LocalDate.now(); // 오늘 일자 기준
////                    LocalDate date = LocalDate.now().minusDays(1); //어제 일자 기준
//                    LocalDate date = LocalDate.parse("2024-10-16");
////                    LocalDate date = LocalDate.parse("2024-10-27");
//
//                    List<String> keys = coinInfoService.getCoinMarketKeys();
//
//                    Map<String, List<CoinOrderSimpleDTO>> coinOrderPartitions = new HashMap<>();
//
//                    for (String key : keys) {
//                        String[] parts = key.split("-");
//                        String coinName = parts[0];  // BTC, ETH..
//                        String marketName = parts[1]; // KRW, USDT..
//
//                        List<CoinOrderSimpleDTO> chunkedCoinOrders = coinOrderService.getCoinOrderChunksBy1000(coinName, marketName, date);
//
//                        if (!chunkedCoinOrders.isEmpty()) {
//                            coinOrderPartitions.put(key, chunkedCoinOrders);
//                        }
//                    }
//
//                    // 데이터가 없으면 배치 작업을 종료
//                    if (coinOrderPartitions.isEmpty()) {
//                        contribution.setExitStatus(ExitStatus.FAILED);
//                        return RepeatStatus.FINISHED;
//                    }
//
//                    // 어제의 일일 종가가 CoinOrderDayHistory에 존재하는지 확인
//                    if (coinOrderService.hasClosingPriceForDate(date)) {
//                        contribution.setExitStatus(ExitStatus.FAILED);
//                        return RepeatStatus.FINISHED;
//                    }
//
//                    // JobExecutionContext에 공유 데이터 저장
//                    chunkContext.getStepContext().getStepExecution()
//                            .getJobExecution().getExecutionContext().put("coinOrderPartitions", coinOrderPartitions);
//                    chunkContext.getStepContext().getStepExecution()
//                            .getJobExecution().getExecutionContext().put("yesterday", date);
//
//                    contribution.setExitStatus(ExitStatus.COMPLETED);
//                    return RepeatStatus.FINISHED;
//                }, transactionManager)
//                .build();
//    }
//
//    @Bean
//    public Partitioner partitioner() {
//        return gridSize -> {
//            Map<String, ExecutionContext> partitions = new HashMap<>();
//
//            // JobExecutionContext에서 값을 읽어오기
//            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();
//
//            Map<String, List<CoinOrderSimpleDTO>> coinOrderPartitions =
//                    (Map<String, List<CoinOrderSimpleDTO>>) stepExecution.getJobExecution()
//                            .getExecutionContext()
//                            .get("coinOrderPartitions");
//
//            LocalDate yesterday = (LocalDate) stepExecution.getJobExecution()
//                    .getExecutionContext()
//                    .get("yesterday");
//
//            AtomicInteger partitionCounter = new AtomicInteger(1);
//
//            for (Map.Entry<String, List<CoinOrderSimpleDTO>> entry : coinOrderPartitions.entrySet()) {
//                for (CoinOrderSimpleDTO coinOrderSimpleDTO : entry.getValue()) {
//                    ExecutionContext context = new ExecutionContext();
//                    context.put("chunkIdx", coinOrderSimpleDTO.getIdx());
//                    context.put("coinName", coinOrderSimpleDTO.getCoinName());
//                    context.put("yesterday", yesterday);
//                    partitions.put("partition" + partitionCounter.getAndIncrement(), context);
//                }
//            }
////
//            return partitions;
//        };
//    }
//
//    @Bean
//    public Step mergeStep(@Qualifier("JobRepository") JobRepository jobRepository,
//                          RedisService redisService,
//                          CoinInfoService coinInfoService,
//                          MasterCoinOrderDayHistoryRepository masterCoinOrderDayHistoryRepository,
//                          PlatformTransactionManager transactionManager, CoinOrderService coinOrderService) {
//        return new StepBuilder("mergeStep", jobRepository).tasklet((contribution, chunkContext) -> {
//            System.out.println("mergeStep");
//
////            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();
////
////            LocalDate yesterday = (LocalDate) stepExecution.getJobExecution()
////                    .getExecutionContext()
////                    .get("yesterday");
////
////            List<String> keys = coinInfoService.getCoinMarketKeys();
////
////            for (String key : keys) {
////                String[] parts = key.split("-");
////                String coinName = parts[0];  // BTC, ETH..
////
////                Set<String> redisKeys = redisService.getKeys(yesterday + ":" + coinName + ":partition:*");
////                BigDecimal totalPrice = BigDecimal.ZERO;
////                BigDecimal totalVolume = BigDecimal.ZERO;
////
////                for (String redisKey : redisKeys) {
////                    BigDecimal price = new BigDecimal(redisService.getHashOps(redisKey, "totalPrice"));
////                    BigDecimal volume = new BigDecimal(redisService.getHashOps(redisKey, "totalVolume"));
////                    totalPrice = totalPrice.add(price);
////                    totalVolume = totalVolume.add(volume);
////                }
////
////                // 평균 가격 계산
////                BigDecimal averagePrice = totalPrice.divide(totalVolume, RoundingMode.HALF_UP);
////
////                //마지막 idx를 종가로 설정
////                BigDecimal closingPrice = coinOrderService.getLatestExecutionPriceByDate(coinName, yesterday);
////
////                //최종 결과를 DB에 저장
////                CoinOrderDayHistoryDTO history = new CoinOrderDayHistoryDTO();
////                history.setMarketName("KRW");
////                history.setCoinName(coinName);
////                history.setAveragePrice(averagePrice);
////                history.setTradingVolume(totalVolume);
////                history.setClosingPrice(closingPrice);
////                history.setTradingDate(yesterday);
////
////                // DB 저장
////                masterCoinOrderDayHistoryRepository.save(CoinOrderDayHistoryMapper.toEntity(history));
////            }
//
//            return RepeatStatus.FINISHED;
//        }, transactionManager).build();
//    }
//
//    @Bean
//    public TaskExecutor taskExecutor() {
//        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
//        taskExecutor.setCorePoolSize(5);
//        taskExecutor.setMaxPoolSize(10);
//        taskExecutor.setQueueCapacity(500);
//        return taskExecutor;
//    }
}
