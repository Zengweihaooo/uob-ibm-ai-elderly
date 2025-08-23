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
 * AWS服务配置类
 * 配置各种AWS服务的客户端
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Configuration
@Profile("aws") // 只在aws profile激活时加载
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
     * 创建AWS凭证提供者
     */
    @Bean
    public StaticCredentialsProvider awsCredentialsProvider() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return StaticCredentialsProvider.create(awsCredentials);
    }

    /**
     * S3客户端配置
     */
    @Bean
    public S3Client s3Client() {
        S3Client.Builder builder = S3Client.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * DynamoDB客户端配置
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClient.Builder builder = DynamoDbClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * SNS客户端配置
     */
    @Bean
    public SnsClient snsClient() {
        SnsClient.Builder builder = SnsClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * SES客户端配置
     */
    @Bean
    public SesClient sesClient() {
        SesClient.Builder builder = SesClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * Lambda客户端配置
     */
    @Bean
    public LambdaClient lambdaClient() {
        LambdaClient.Builder builder = LambdaClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }

    /**
     * CloudWatch客户端配置
     */
    @Bean
    public CloudWatchClient cloudWatchClient() {
        CloudWatchClient.Builder builder = CloudWatchClient.builder()
                .credentialsProvider(awsCredentialsProvider())
                .region(Region.of(region));

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpoint));
        }

        return builder.build();
    }
}

