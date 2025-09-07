package com.example.demo.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;

/**
 * Home controller to serve the main index.html file
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Resource> home() {
        try {
            // Get the project root directory (parent of springboot directory)
            String projectRoot = System.getProperty("user.dir");
            File projectRootDir = new File(projectRoot);
            File parentDir = projectRootDir.getParentFile();
            File indexFile = new File(parentDir, "index.html");
            
            System.out.println("HomeController - Project root: " + projectRoot);
            System.out.println("HomeController - Parent dir: " + parentDir.getAbsolutePath());
            System.out.println("HomeController - Index file path: " + indexFile.getAbsolutePath());
            System.out.println("HomeController - Index file exists: " + indexFile.exists());
            
            if (indexFile.exists()) {
                Resource resource = new FileSystemResource(indexFile);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                        .body(resource);
            } else {
                System.out.println("HomeController - Index file not found!");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("HomeController - Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/index.html")
    public ResponseEntity<Resource> index() {
        return home();
    }
}
