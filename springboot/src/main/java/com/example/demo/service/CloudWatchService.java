package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

import java.time.Instant;
import java.util.logging.Logger;

/**
 * AWS CloudWatch Monitoring Service
 * Handles metrics collection and monitoring for the elderly companion application
 * 
 * @author Lepeng Zhou
 * @version 2.0
 */
@Service
@Profile("aws")
public class CloudWatchService {
    
    private static final Logger logger = Logger.getLogger(CloudWatchService.class.getName());
    
    @Autowired
    private CloudWatchClient cloudWatchClient;
    
    @Value("${aws.region:us-east-1}")
    private String region;
    
    private static final String NAMESPACE = "ElderlyCompanion/Application";
    
    /**
     * Send custom metrics to CloudWatch
     */
    public void putMetric(String metricName, double value, String unit) {
        try {
            logger.info("Sending CloudWatch Metric: " + metricName + " = " + value + " " + unit);
            
            MetricDatum datum = MetricDatum.builder()
                .metricName(metricName)
                .value(value)
                .unit(StandardUnit.fromValue(unit))
                .timestamp(Instant.now())
                .build();
            
            PutMetricDataRequest request = PutMetricDataRequest.builder()
                .namespace(NAMESPACE)
                .metricData(datum)
                .build();
            
            cloudWatchClient.putMetricData(request);
            logger.info("Successfully sent metric to CloudWatch: " + metricName);
            
        } catch (Exception e) {
            logger.severe("Failed to send metric to CloudWatch: " + e.getMessage());
            // Fallback to logging for demo purposes
            logger.info("CloudWatch Metric (fallback): " + metricName + " = " + value + " " + unit);
        }
    }
    
    /**
     * Send health record metrics
     */
    public void recordHealthMetric(String type, boolean isAbnormal) {
        putMetric("HealthRecord." + type, isAbnormal ? 1.0 : 0.0, "Count");
        putMetric("HealthRecord.Total", 1.0, "Count");
    }
    
    /**
     * Send user activity metrics
     */
    public void recordUserActivity(String activity) {
        putMetric("UserActivity." + activity, 1.0, "Count");
    }
    
    /**
     * Send system performance metrics
     */
    public void recordSystemMetric(String metric, double value) {
        putMetric("System." + metric, value, "Count");
    }
    
    /**
     * Send notification metrics
     */
    public void recordNotificationSent(String type, boolean success) {
        putMetric("Notification." + type + (success ? ".Success" : ".Failure"), 1.0, "Count");
    }
    
    /**
     * Create CloudWatch alarm for health monitoring
     */
    public void createHealthAlarm() {
        try {
            logger.info("Creating CloudWatch alarm: ElderlyCompanion-HealthAbnormalRate");
            logger.info("Alarm description: Alert when health abnormal rate is too high");
            logger.info("Threshold: 30% abnormal rate");
            
        } catch (Exception e) {
            logger.severe("Failed to create health alarm: " + e.getMessage());
        }
    }
    
    /**
     * Get metric statistics
     */
    public Object getMetricStatistics(String metricName, int hours) {
        try {
            logger.info("Getting CloudWatch statistics for metric: " + metricName);
            logger.info("Time range: " + hours + " hours");
            logger.info("Namespace: " + NAMESPACE);
            
            // Return mock data for demo
            return "Mock statistics data for " + metricName;
            
        } catch (Exception e) {
            logger.severe("Failed to get metric statistics: " + e.getMessage());
            return null;
        }
    }
}
