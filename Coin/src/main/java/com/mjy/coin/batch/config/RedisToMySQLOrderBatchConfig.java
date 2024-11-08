package com.mjy.coin.batch.config;

import com.mjy.coin.batch.*;
import com.mjy.coin.dto.CoinOrderDTO;
import com.mjy.coin.repository.coin.master.MasterCoinOrderRepository;
import com.mjy.coin.service.CoinInfoService;
import com.mjy.coin.service.ConvertService;
import com.mjy.coin.service.RedisService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;

@Configuration
public class RedisToMySQLOrderBatchConfig {
    @Bean(name = "redisToMysqlJob")
    public Job redisToMysqlJob(@Qualifier("JobRepository") JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               CoinInfoService coinInfoService,
                               RedisService redisService,
                               MasterCoinOrderRepository masterCoinOrderRepository,
                               ConvertService convertService){

        List<String> keys = coinInfoService.getCoinMarketKeys();

        JobBuilder jobBuilder = new JobBuilder("redisToMysqlJob", jobRepository);
        FlowBuilder<SimpleFlow> flowBuilder = new FlowBuilder<>("redisToMysqlFlow");

        Step previousStep = null; // 마지막으로 연결한 스텝을 저장하기 위한 변수

        for (String key : keys) {
            Step step = new StepBuilder("redisToMysqlStep" + key, jobRepository)
                    .<Map.Entry<String, String>, CoinOrderDTO>chunk(1000, transactionManager)
                    .reader(new RedisToMySQLOrderReader(redisService, key))
                    .processor(new RedisToMySQLOrderProcessor(convertService))
                    .writer(new RedisToMySQLOrderWriter(redisService, masterCoinOrderRepository, key))
                    .build();


            if (previousStep == null) {
                flowBuilder.start(step); // 첫 번째 스텝이면 start로 연결
            } else {
                flowBuilder.next(step); // 이후 스텝은 next로 연결
            }

            previousStep = step; // 현재 스텝을 이전 스텝으로 저장
        }

        SimpleFlow flow = flowBuilder.build();
        return jobBuilder.start(flow).build().build();
    }
}
