package com.mjy.coin.batch.config;

import com.mjy.coin.batch.CoinOrderProcessor;
import com.mjy.coin.batch.CoinOrderReader;
import com.mjy.coin.batch.CoinOrderWriter;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.dto.CoinOrderDayHistoryDTO;
import com.mjy.coin.dto.CoinOrderDayHistoryMapper;
import com.mjy.coin.dto.CoinOrderSimpleDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderDayHistoryRepository;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.CoinOrderService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class CoinOrderBatchConfig {
    @Bean
    public Job coinOrderJob(@Qualifier("JobRepository") JobRepository jobRepository,
                            Step checkDataStep,
                            Step partitionStep,
                            Step mergeStep) {
        return new JobBuilder("coinOrderJob", jobRepository)
                .start(checkDataStep)
                .on("FAILED").end()  // checkDataStep가 FAILED면 종료
                .from(checkDataStep).on("COMPLETED").to(partitionStep)  // checkDataStep가 성공 시 partitionStep으로 이동
                .from(partitionStep).on("COMPLETED").to(mergeStep)  // partitionStep이 성공한 경우 mergeStep으로 이동
                .end()
                .build();
    }

    @Bean
    public Step partitionStep(@Qualifier("JobRepository") JobRepository jobRepository,
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
                              CoinOrderService coinOrderService,
                              CoinInfoService coinInfoService) {
        return new StepBuilder("checkDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
//                    LocalDate date = LocalDate.now(); // 오늘 일자 기준
                    LocalDate date = LocalDate.now().minusDays(1); //어제 일자 기준

                    List<String> keys = coinInfoService.getCoinMarketKeys();

                    Map<String, List<CoinOrderSimpleDTO>> coinOrderPartitions = new HashMap<>();

                    for (String key : keys) {
                        String[] parts = key.split("-");
                        String coinName = parts[0];  // BTC, ETH..
                        String marketName = parts[1]; // KRW, USDT..

                        List<CoinOrderSimpleDTO> chunkedCoinOrders = coinOrderService.getCoinOrderChunksBy1000(coinName, marketName, date);

                        if (!chunkedCoinOrders.isEmpty()) {
                            coinOrderPartitions.put(key, chunkedCoinOrders);
                        }
                    }

                    // 데이터가 없으면 배치 작업을 종료
                    if (coinOrderPartitions.isEmpty()) {
                        contribution.setExitStatus(ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }

                    // 어제의 일일 종가가 CoinOrderDayHistory에 존재하는지 확인
                    if (coinOrderService.hasClosingPriceForDate(date)) {
                        contribution.setExitStatus(ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }

                    // JobExecutionContext에 공유 데이터 저장
                    chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext().put("coinOrderPartitions", coinOrderPartitions);
                    chunkContext.getStepContext().getStepExecution()
                            .getJobExecution().getExecutionContext().put("yesterday", date);

                    contribution.setExitStatus(ExitStatus.COMPLETED);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean(name = "CoinOrderPartitioner")
    public Partitioner partitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();

            // JobExecutionContext에서 값을 읽어오기
            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            Map<String, List<CoinOrderSimpleDTO>> coinOrderPartitions =
                    (Map<String, List<CoinOrderSimpleDTO>>) stepExecution.getJobExecution()
                            .getExecutionContext()
                            .get("coinOrderPartitions");

            LocalDate yesterday = (LocalDate) stepExecution.getJobExecution()
                    .getExecutionContext()
                    .get("yesterday");

            AtomicInteger partitionCounter = new AtomicInteger(1);

            for (Map.Entry<String, List<CoinOrderSimpleDTO>> entry : coinOrderPartitions.entrySet()) {
                for (CoinOrderSimpleDTO coinOrderSimpleDTO : entry.getValue()) {
                    ExecutionContext context = new ExecutionContext();
                    context.put("chunkIdx", coinOrderSimpleDTO.getIdx());
                    context.put("coinName", coinOrderSimpleDTO.getCoinName());
                    context.put("yesterday", yesterday);
                    partitions.put("partition" + partitionCounter.getAndIncrement(), context);
                }
            }

            return partitions;
        };
    }

    @Bean
    public Step mergeStep(JobRepository jobRepository,
                          RedisService redisService,
                          CoinInfoService coinInfoService,
                          MasterCoinOrderDayHistoryRepository masterCoinOrderDayHistoryRepository,
                          PlatformTransactionManager transactionManager, CoinOrderService coinOrderService) {
        return new StepBuilder("mergeStep", jobRepository).tasklet((contribution, chunkContext) -> {
            System.out.println("mergeStep");

            StepExecution stepExecution = StepSynchronizationManager.getContext().getStepExecution();

            LocalDate yesterday = (LocalDate) stepExecution.getJobExecution()
                    .getExecutionContext()
                    .get("yesterday");

            List<String> keys = coinInfoService.getCoinMarketKeys();

            for (String key : keys) {
                String[] parts = key.split("-");
                String coinName = parts[0];  // BTC, ETH..

                Set<String> redisKeys = redisService.getKeys(yesterday + ":" + coinName + ":partition:*");
                BigDecimal totalPrice = BigDecimal.ZERO;
                BigDecimal totalVolume = BigDecimal.ZERO;

                for (String redisKey : redisKeys) {
                    BigDecimal price = new BigDecimal(redisService.getHashOps(redisKey, "totalPrice"));
                    BigDecimal volume = new BigDecimal(redisService.getHashOps(redisKey, "totalVolume"));
                    totalPrice = totalPrice.add(price);
                    totalVolume = totalVolume.add(volume);
                }

                // 평균 가격 계산
                BigDecimal averagePrice = totalPrice.divide(totalVolume, RoundingMode.HALF_UP);

                //마지막 idx를 종가로 설정
                BigDecimal closingPrice = coinOrderService.getLatestExecutionPriceByDate(coinName, yesterday);

                //최종 결과를 DB에 저장
                CoinOrderDayHistoryDTO history = new CoinOrderDayHistoryDTO();
                history.setMarketName("KRW");
                history.setCoinName(coinName);
                history.setAveragePrice(averagePrice);
                history.setTradingVolume(totalVolume);
                history.setClosingPrice(closingPrice);
                history.setTradingDate(yesterday);

                // DB 저장
                masterCoinOrderDayHistoryRepository.save(CoinOrderDayHistoryMapper.toEntity(history));
            }

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
