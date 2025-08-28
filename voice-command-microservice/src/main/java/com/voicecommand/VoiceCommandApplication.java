package com.voicecommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 语音命令微服务主应用类
 * 
 * 这是一个独立的微服务，专门处理AI语音命令分析
 * 可以独立部署和扩展，不依赖主项目的springboot目录
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
public class VoiceCommandApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VoiceCommandApplication.class, args);
    }
}
