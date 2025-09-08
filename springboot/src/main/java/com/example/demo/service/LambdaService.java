package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;
import software.amazon.awssdk.core.SdkBytes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * AWS Lambda Service
 * Handles serverless function invocations for elderly companion application
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Service
@Profile("aws")
public class LambdaService {
    
    private static final Logger logger = Logger.getLogger(LambdaService.class.getName());
    
    @Autowired
    private LambdaClient lambdaClient;
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    // Lambda function names
    private static final String HEALTH_ANALYSIS_FUNCTION = "elderly-companion-health-analysis";
    private static final String EMERGENCY_PROCESSING_FUNCTION = "elderly-companion-emergency-processing";
    private static final String DATA_SYNC_FUNCTION = "elderly-companion-data-sync";
    
    /**
     * Invoke health analysis Lambda function
     */
    public String invokeHealthAnalysis(String healthData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("healthData", healthData);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("action", "analyze");
            
            return invokeFunction(HEALTH_ANALYSIS_FUNCTION, payload);
            
        } catch (Exception e) {
            logger.severe("Failed to invoke health analysis function: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Invoke emergency processing Lambda function
     */
    public String invokeEmergencyProcessing(String emergencyData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("emergencyData", emergencyData);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("action", "process_emergency");
            payload.put("priority", "high");
            
            return invokeFunction(EMERGENCY_PROCESSING_FUNCTION, payload);
            
        } catch (Exception e) {
            logger.severe("Failed to invoke emergency processing function: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Invoke data synchronization Lambda function
     */
    public String invokeDataSync(String syncData) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("syncData", syncData);
            payload.put("timestamp", System.currentTimeMillis());
            payload.put("action", "sync_data");
            
            return invokeFunction(DATA_SYNC_FUNCTION, payload);
            
        } catch (Exception e) {
            logger.severe("Failed to invoke data sync function: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Generic function invocation method
     */
    private String invokeFunction(String functionName, Map<String, Object> payload) {
        try {
            String jsonPayload = convertToJson(payload);
            SdkBytes payloadBytes = SdkBytes.fromString(jsonPayload, StandardCharsets.UTF_8);
            
            InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(payloadBytes)
                .build();
            
            InvokeResponse response = lambdaClient.invoke(request);
            
            // Read response
            SdkBytes responsePayload = response.payload();
            String responseString = responsePayload.asString(StandardCharsets.UTF_8);
            
            logger.info("Lambda function " + functionName + " invoked successfully");
            return responseString;
            
        } catch (Exception e) {
            logger.severe("Failed to invoke Lambda function " + functionName + ": " + e.getMessage());
            throw new RuntimeException("Lambda invocation failed", e);
        }
    }
    
    /**
     * Create Lambda function
     */
    public void createHealthAnalysisFunction() {
        try {
            // This would typically be done via CloudFormation or AWS CLI
            // For demo purposes, we'll just log the intention
            logger.info("Creating health analysis Lambda function: " + HEALTH_ANALYSIS_FUNCTION);
            
            // In a real implementation, you would:
            // 1. Package the function code
            // 2. Create the function via Lambda API
            // 3. Set up IAM roles and permissions
            // 4. Configure triggers
            
        } catch (Exception e) {
            logger.severe("Failed to create Lambda function: " + e.getMessage());
        }
    }
    
    /**
     * List available Lambda functions
     */
    public void listFunctions() {
        try {
            ListFunctionsRequest request = ListFunctionsRequest.builder().build();
            ListFunctionsResponse response = lambdaClient.listFunctions(request);
            
            logger.info("Available Lambda functions:");
            response.functions().forEach(function -> {
                logger.info("- " + function.functionName() + " (" + function.runtime() + ")");
            });
            
        } catch (Exception e) {
            logger.severe("Failed to list Lambda functions: " + e.getMessage());
        }
    }
    
    /**
     * Simple JSON conversion (in production, use a proper JSON library)
     */
    private String convertToJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}
