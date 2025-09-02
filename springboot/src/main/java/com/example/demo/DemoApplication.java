package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for the IBM AI Elderly Project
 * 
 * This application provides email management functionality for the elderly care project.
 * Database configuration is now enabled for family contacts and other data storage.
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
@MapperScan({"com.example.demo.mapper", "com.example.demo.repository"})
// Automatically call scheduled tasks, such as checking if users have submitted health data and sending reminder emails
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
