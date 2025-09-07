package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web configuration for static resource handling
 * 
 * This configuration allows Spring Boot to serve static files from the project root directory
 * in addition to the default static resource locations.
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the project root directory (parent of springboot directory)
        String projectRoot = System.getProperty("user.dir");
        File projectRootDir = new File(projectRoot);
        File parentDir = projectRootDir.getParentFile();
        
        System.out.println("Project root directory: " + projectRootDir.getAbsolutePath());
        System.out.println("Parent directory: " + parentDir.getAbsolutePath());
        System.out.println("Index.html exists: " + new File(parentDir, "index.html").exists());
        
        // Handle src directory for CSS, JS, and other assets
        registry.addResourceHandler("/src/**")
                .addResourceLocations("file:" + parentDir.getAbsolutePath() + "/src/")
                .setCachePeriod(0);
    }
}
