package com.mjy.coin.config;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = {BatchConfig.class})
public class BatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job orderJob;

    @Test
    public void testOrderJob() throws Exception {
        // JobExecution 실행
        JobExecution jobExecution = jobLauncher.run(orderJob, new JobParameters());

        // JobExecution의 상태 확인
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus(), "Job should be completed");

        // 추가적인 검증 로직을 여기에 추가 가능
    }
}