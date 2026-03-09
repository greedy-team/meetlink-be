package com.greedy.meetlink.common.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean
    public Executor candidateCalculationExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public TaskScheduler locationCandidateScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("location-debounce-");
        scheduler.initialize();
        return scheduler;
    }
}
