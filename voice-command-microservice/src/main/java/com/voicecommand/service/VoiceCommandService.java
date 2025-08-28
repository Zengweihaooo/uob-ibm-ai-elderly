package com.voicecommand.service;

import com.voicecommand.model.VoiceCommandRequest;
import com.voicecommand.model.VoiceCommandResponse;
import com.voicecommand.model.CommandExecutionStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音命令服务接口
 * 
 * 处理语音命令的核心服务
 * 
 * @author AI Assistant
 * @version 1.0.0
 */
public interface VoiceCommandService {
    
    /**
     * 处理语音命令
     * 
     * @param audioFile 音频文件
     * @param languageCode 语言代码
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 处理结果
     */
    VoiceCommandResponse processVoiceCommand(MultipartFile audioFile, String languageCode, 
                                           String userId, String sessionId);
    
    /**
     * 处理文本命令
     * 
     * @param textCommand 文本命令
     * @param languageCode 语言代码
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 处理结果
     */
    VoiceCommandResponse processTextCommand(String textCommand, String languageCode, 
                                          String userId, String sessionId);
    
    /**
     * 获取命令执行状态
     * 
     * @param executionId 执行ID
     * @return 执行状态
     */
    CommandExecutionStatus getExecutionStatus(String executionId);
    
    /**
     * 取消命令执行
     * 
     * @param executionId 执行ID
     * @return 是否成功取消
     */
    boolean cancelExecution(String executionId);
}
