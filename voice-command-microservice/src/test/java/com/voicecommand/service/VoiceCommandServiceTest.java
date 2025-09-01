package com.voicecommand.service;

import com.voicecommand.service.impl.VoiceCommandServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class VoiceCommandServiceTest {
    
    @InjectMocks
    private VoiceCommandServiceImpl voiceCommandService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testIsFunctionCallIntent() throws Exception {
        // 使用反射来测试私有方法
        Method method = VoiceCommandServiceImpl.class.getDeclaredMethod("isFunctionCallIntent", String.class);
        method.setAccessible(true);
        
        // 测试功能调用意图
        assertTrue((Boolean) method.invoke(voiceCommandService, "发送邮件给张三"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "send email to john"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "查看日程"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "health check"));
        
        // 测试普通对话意图
        assertFalse((Boolean) method.invoke(voiceCommandService, "你好"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "今天天气怎么样"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "hello"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "how are you"));
    }
}
