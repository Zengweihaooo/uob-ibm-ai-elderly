package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the IBM AI Elderly Project
 * 
 * This application provides email management functionality for the elderly care project.
 * Currently configured to exclude database auto-configuration as database is not used yet.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@SpringBootApplication(
    exclude = { DataSourceAutoConfiguration.class }
) // Temporarily exclude database configuration - not using database yet
@EnableScheduling
// 自动调用定时任务，如定时检查用户是否提交健康数据，并发送提醒邮件
public class DemoApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
