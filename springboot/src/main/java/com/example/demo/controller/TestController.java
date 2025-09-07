package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple test controller to verify Spring Boot is running
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Spring Boot is running!";
    }

    @GetMapping("/health")
    public String health() {
        return "Application is healthy!";
    }
}
