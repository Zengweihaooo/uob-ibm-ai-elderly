package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for important date test page
 * 
 * @author Weihao Zeng
 * @version 1.0
 */
@Controller
public class ImportantDateTestController {

    /**
     * Display the important date test page
     * 
     * @return The test page template name
     */
    @GetMapping("/important-date-test")
    public String showTestPage() {
        return "importantDateTest";
    }
} 