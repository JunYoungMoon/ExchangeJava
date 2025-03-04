//package com.mjy.coin.batch.config;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Profile;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@EnableScheduling
//@Profile("dev")
//public class BatchScheduler {
//
//    private final JobLauncher jobLauncher;
////    private final Job coinOrderJob;
//    private final Job redisToMysqlJob;
//
//    @Autowired
//    public BatchScheduler(JobLauncher jobLauncher,
////                          @Qualifier("coinOrderJob") Job coinOrderJob,
//                          @Qualifier("redisToMysqlJob") Job redisToMysqlJob) {
//        this.jobLauncher = jobLauncher;
////        this.coinOrderJob = coinOrderJob;
//        this.redisToMysqlJob = redisToMysqlJob;
//    }
//
////    @Bean
////    public ApplicationRunner runAtStartup() {
////        return args -> runCoinOrderJob();
////    }
////
////    @Scheduled(cron = "0 0 2 * * ?") // 매일 오전 2시에 실행
////    public void runCoinOrderJob() {
////        try {
////            jobLauncher.run(coinOrderJob, new JobParametersBuilder()
////                    .addLong("run.id", System.currentTimeMillis())
////                    .toJobParameters());
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
//
//
//    @Scheduled(fixedRate = 60000 * 5) // 5분마다 실행
//    public void runRedisToMysqlJob() {
//        try {
//            jobLauncher.run(redisToMysqlJob, new JobParametersBuilder()
//                    .addLong("run.id", System.currentTimeMillis())
//                    .toJobParameters());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}