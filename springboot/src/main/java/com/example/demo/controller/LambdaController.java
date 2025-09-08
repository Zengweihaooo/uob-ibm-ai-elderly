package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import com.example.demo.service.LambdaService;

import java.util.HashMap;
import java.util.Map;

/**
 * Lambda Function Controller
 * Provides endpoints for Lambda function management and invocation
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@RestController
@RequestMapping("/api/lambda")
@Profile("aws")
public class LambdaController {
    
    @Autowired
    private LambdaService lambdaService;
    
    /**
     * Invoke health analysis function
     */
    @PostMapping("/health-analysis")
    public Map<String, Object> invokeHealthAnalysis(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String healthData = (String) request.get("healthData");
            String result = lambdaService.invokeHealthAnalysis(healthData);
            
            response.put("success", true);
            response.put("message", "Health analysis completed");
            response.put("result", result);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Health analysis failed: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Invoke emergency processing function
     */
    @PostMapping("/emergency-processing")
    public Map<String, Object> invokeEmergencyProcessing(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String emergencyData = (String) request.get("emergencyData");
            String result = lambdaService.invokeEmergencyProcessing(emergencyData);
            
            response.put("success", true);
            response.put("message", "Emergency processing completed");
            response.put("result", result);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Emergency processing failed: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Invoke data sync function
     */
    @PostMapping("/data-sync")
    public Map<String, Object> invokeDataSync(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String syncData = (String) request.get("syncData");
            String result = lambdaService.invokeDataSync(syncData);
            
            response.put("success", true);
            response.put("message", "Data sync completed");
            response.put("result", result);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Data sync failed: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * List available Lambda functions
     */
    @GetMapping("/functions")
    public Map<String, Object> listFunctions() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            lambdaService.listFunctions();
            response.put("success", true);
            response.put("message", "Functions listed successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to list functions: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Create health analysis function
     */
    @PostMapping("/create-health-function")
    public Map<String, Object> createHealthFunction() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            lambdaService.createHealthAnalysisFunction();
            response.put("success", true);
            response.put("message", "Health analysis function creation initiated");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create function: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Lambda service health check
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test function listing
            lambdaService.listFunctions();
            
            response.put("success", true);
            response.put("message", "Lambda service is healthy");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lambda service error: " + e.getMessage());
        }
        
        return response;
    }
}
