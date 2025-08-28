package com.voicecommand.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
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
     * 主AI服务凭证 - 用于Gemini、TTS、STT
     */
    @Bean
    @Primary
    public GoogleCredentials aiCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(aiCredentialsFile));
    }

    /**
     * 语音服务凭证
     */
    @Bean
    public GoogleCredentials speechCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(speechCredentialsFile));
    }

    /**
     * TTS服务凭证
     */
    @Bean
    public GoogleCredentials ttsCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(ttsCredentialsFile));
    }

    /**
     * 计算引擎凭证 - 用于其他Google Cloud服务
     */
    @Bean
    public GoogleCredentials computeCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(computeCredentialsFile));
    }

    /**
     * 备用凭证 - 故障转移使用
     */
    @Bean
    public GoogleCredentials backupCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(backupCredentialsFile));
    }
}
