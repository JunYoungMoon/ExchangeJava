package com.mjy.coin.batch.schedule;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job coinOrderJob;

    @Autowired
    public BatchScheduler(JobLauncher jobLauncher, @Qualifier("coinOrderJob") Job orderJob) {
        this.jobLauncher = jobLauncher;
        this.coinOrderJob = orderJob;
    }

    @Scheduled(fixedRate = 86400000) // 1일 단위
    public void runOrderJob() {
        try {
            jobLauncher.run(coinOrderJob, new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis()) // 매번 새로운 파라미터 추가하여 job 재실행 가능하게 함
                    .toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}