package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步配置类
 * 
 * 配置线程池用于邮件发送等异步操作
 * 避免邮件发送阻塞主线程，提高系统响应性能
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 邮件发送专用线程池
     * 
     * 配置说明：
     * - corePoolSize: 核心线程数，保持活跃的线程数量
     * - maxPoolSize: 最大线程数，线程池能创建的最大线程数量
     * - queueCapacity: 队列容量，等待执行的任务数量
     * - threadNamePrefix: 线程名前缀，便于调试和监控
     * - keepAliveSeconds: 空闲线程存活时间
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：2个（考虑到邮件发送的并发需求）
        executor.setCorePoolSize(2);
        
        // 最大线程数：5个（峰值时可以创建更多线程）
        executor.setMaxPoolSize(5);
        
        // 队列容量：100个任务（防止内存溢出）
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("email-");
        
        // 空闲线程存活时间：60秒
        executor.setKeepAliveSeconds(60);
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：30秒
        executor.setAwaitTerminationSeconds(30);
        
        // 初始化线程池
        executor.initialize();
        
        return executor;
    }

    /**
     * 通用异步任务线程池
     * 
     * 用于其他不需要特殊配置的异步操作
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
