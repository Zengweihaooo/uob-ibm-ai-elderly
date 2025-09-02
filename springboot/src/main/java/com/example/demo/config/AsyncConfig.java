package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Asynchronous configuration.
 * 
 * Defines thread pools used for asynchronous operations such as email sending.
 * Prevents email operations from blocking the main thread and improves overall responsiveness.
 * 
 * author AI Assistant
 * version 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dedicated thread pool for email sending.
     * 
     * Configuration notes:
     * - corePoolSize: number of core threads kept alive.
     * - maxPoolSize: maximum number of threads allowed in the pool.
     * - queueCapacity: capacity for queued tasks waiting for execution.
     * - threadNamePrefix: prefix added to thread names for easier debug/monitoring.
     * - keepAliveSeconds: idle time before non-core threads are terminated.
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
    // Core threads: 2 (sufficient for expected email concurrency)
        executor.setCorePoolSize(2);
        
    // Max threads: 5 (allows elasticity during peaks)
        executor.setMaxPoolSize(5);
        
    // Queue capacity: 100 tasks (avoid uncontrolled growth)
        executor.setQueueCapacity(100);
        
    // Thread name prefix
        executor.setThreadNamePrefix("email-");
        
    // Keep-alive for idle threads: 60 seconds
        executor.setKeepAliveSeconds(60);
        
    // Wait for tasks to finish on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
    // Await termination timeout: 30 seconds
        executor.setAwaitTerminationSeconds(30);
        
    // Initialize the executor
        executor.initialize();
        
        return executor;
    }

    /**
     * General-purpose async task thread pool.
     * 
     * Used for other asynchronous operations that do not need a dedicated configuration.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("task-");
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        return executor;
    }
}
