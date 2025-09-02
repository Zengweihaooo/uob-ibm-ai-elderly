package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

/**
 * AWS services configuration class.
 * Configures clients for various AWS services.
 * 
 * Author: Lepeng Zhou
 * Version: 1.0
 */
@Configuration
@Profile("aws") // Only loaded when the 'aws' profile is active
public class AWSConfig {

    @Value("${aws.access-key-id}")
    private String accessKeyId;

    @Value("${aws.secret-access-key}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.endpoint:}")
    private String endpoint;

    /**
     * Create AWS credentials provider.
     */
    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return StaticCredentialsProvider.create(awsCredentials);
    }

    /**
     * S3 client configuration.
     */
    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * DynamoDB client configuration.
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * SNS client configuration.
     */
    @Bean
    public SnsClient snsClient() {
        var builder = SnsClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * SES client configuration.
     */
    @Bean
    public SesClient sesClient() {
        var builder = SesClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * Lambda client configuration.
     */
    @Bean
    public LambdaClient lambdaClient() {
        var builder = LambdaClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * CloudWatch client configuration.
     */
    @Bean
    public CloudWatchClient cloudWatchClient() {
        var builder = CloudWatchClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }
}

