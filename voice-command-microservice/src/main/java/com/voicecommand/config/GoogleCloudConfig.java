package com.voicecommand.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "google.cloud.enabled", havingValue = "true", matchIfMissing = false)
public class GoogleCloudConfig {

    @Value("${google.cloud.ai.credentials-file}")
    private String aiCredentialsFile;

    @Value("${google.cloud.speech.credentials-file}")
    private String speechCredentialsFile;

    @Value("${google.cloud.tts.credentials-file}")
    private String ttsCredentialsFile;

    @Value("${google.cloud.compute.credentials-file}")
    private String computeCredentialsFile;

    @Value("${google.cloud.backup-credentials-file}")
    private String backupCredentialsFile;

    /**
     * Main AI service credentials - for Gemini, TTS, STT
     */
    @Bean
    @Primary
    public GoogleCredentials aiCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(aiCredentialsFile));
    }

    /**
     * Speech service credentials
     */
    @Bean
    public GoogleCredentials speechCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(speechCredentialsFile));
    }

    /**
     * TTS service credentials
     */
    @Bean
    public GoogleCredentials ttsCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(ttsCredentialsFile));
    }

    /**
     * Compute engine credentials - for other Google Cloud services
     */
    @Bean
    public GoogleCredentials computeCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(computeCredentialsFile));
    }

    /**
     * Backup credentials - for failover use
     */
    @Bean
    public GoogleCredentials backupCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(backupCredentialsFile));
    }
}
