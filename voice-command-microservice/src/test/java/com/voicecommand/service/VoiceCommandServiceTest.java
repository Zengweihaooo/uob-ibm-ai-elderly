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
        // Use reflection to test private methods
        Method method = VoiceCommandServiceImpl.class.getDeclaredMethod("isFunctionCallIntent", String.class);
        method.setAccessible(true);
        
        // Test function call intents
        assertTrue((Boolean) method.invoke(voiceCommandService, "send email to zhangsan"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "send email to john"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "check schedule"));
        assertTrue((Boolean) method.invoke(voiceCommandService, "health check"));
        
        // Test regular conversation intents
        assertFalse((Boolean) method.invoke(voiceCommandService, "hello"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "how is the weather today"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "hello"));
        assertFalse((Boolean) method.invoke(voiceCommandService, "how are you"));
    }
}
