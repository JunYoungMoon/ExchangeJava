package com.mjy.coin.batch;

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
    private final Job orderJob;

    @Autowired
    public BatchScheduler(JobLauncher jobLauncher, @Qualifier("orderJob") Job orderJob) {
        this.jobLauncher = jobLauncher;
        this.orderJob = orderJob;
    }

    @Scheduled(fixedRate = 600000) // 10분 = 600000밀리초
    public void runOrderJob() {
        try {
            jobLauncher.run(orderJob, new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis()) // 매번 새로운 파라미터 추가하여 job 재실행 가능하게 함
                    .toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}